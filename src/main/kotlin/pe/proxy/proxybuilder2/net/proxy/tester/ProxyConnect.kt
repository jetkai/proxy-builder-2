package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.resolver.NoopAddressResolverGroup
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.monitor.ChecksMonitor
import pe.proxy.proxybuilder2.net.proxy.data.SimpleProxyDataList
import pe.proxy.proxybuilder2.net.proxy.data.SimpleProxyDataType
import pe.proxy.proxybuilder2.net.proxy.supplier.DatabaseProxySupplier
import pe.proxy.proxybuilder2.net.proxy.supplier.LocalProxySupplier
import pe.proxy.proxybuilder2.net.proxy.supplier.MainProxySupplier
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Tasks
import pe.proxy.proxybuilder2.util.Utils
import java.net.InetSocketAddress
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * ProxyConnect
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
@Component
class ProxyConnect(val repository : ProxyRepository, final val config : ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ProxyConnect::class.java)

    private val workerGroup = NioEventLoopGroup(config.threads)

    private val executor : ExecutorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors() + 1
    )

    companion object { //TODO - CHANGE THIS
        val testedProxies = ConcurrentLinkedQueue<ProxyChannelData>()
    }

    private val running = Tasks.thread.proxyConnect?.running!!
    private val pause = Tasks.thread.proxyConnect?.pause!!
    private var retryAttempts = 0

    override fun onApplicationEvent(event : ApplicationReadyEvent) {
        if(config.enabledThreads.proxyConnect && !running.get())
            executor.submit { initialize() }
    }

    private fun initialize() {
        running.set(true)
        val supplierProxyListData = SimpleProxyDataList(mutableMapOf())
        //Load proxy from JSON supplier
        val proxySupplier = MainProxySupplier(supplierProxyListData, config)
        //Load local proxies from "./proxies/*.json"
        LocalProxySupplier(supplierProxyListData, config).request().parse()

        //If the supplier is down, skip the supplier and try proxies that only exist within the database
        if(retryAttempts < 4) {
            try {
                proxySupplier
                    .request()  //Requests proxies from the web
                    .parse()    //Attempt to parse the proxies from the web
            } catch (e : Exception) { //Attempt to re-run again after 30 seconds if error is captured
                retryAttempts++
                logger.error(e.localizedMessage)
                Thread.sleep(30000L)
                initialize()
                return
            }
        }
        retryAttempts = 0

        logger.info("Filtered out the dead proxies, total before: " + supplierProxyListData.proxies.size)

        //Query the database to see if the collected proxies have already been tested and have been dead
        //for over 6 months, this reduces the time to test confirmed dead proxies
        val proxyIps = mutableListOf<String>()
        supplierProxyListData.proxies.mapTo(proxyIps) { it.value.ip }

        val timeNow : LocalDateTime = LocalDateTime.now()
        val sixMonthsAgoTimestamp : Timestamp = Timestamp.valueOf(timeNow.minusMonths(6L))
        val oneWeekAgoTimestamp : Timestamp = Timestamp.valueOf(timeNow.minusWeeks(1L))
        val repositoryOfDeadProxiesInLast6Months = repository.findByIpInAndLastSuccessBefore(proxyIps, sixMonthsAgoTimestamp)
        val repositoryOfProxiesThatNeverWorked = repository.findByLastSuccessIsNullAndLastTestedBefore(oneWeekAgoTimestamp)

        val deadProxyIps = mutableMapOf<String, String>()

        //Remove proxies that have not connected within the past 6 months
        repositoryOfDeadProxiesInLast6Months
            .map { it.ip.toString() }
            .forEach { deadProxyIps[it] = it }

        //Removes proxies that have never connected and have been tested for over a week
        repositoryOfProxiesThatNeverWorked
            .map { it.ip.toString() }
            .forEach { deadProxyIps[it] = it }

        supplierProxyListData.proxies
            .filter { !deadProxyIps[it.value.ip].isNullOrEmpty() }
            .forEach { supplierProxyListData.proxies.remove(it.key) }

        logger.info("One example from the repository: ${repositoryOfDeadProxiesInLast6Months[0].ip}")
        logger.info("Filtered out the dead proxies, total after: " + supplierProxyListData.proxies.size)

        //Load proxies that we've already tested successfully, within the database
        //(Last successful connection within past 6 months)
        DatabaseProxySupplier(supplierProxyListData, repository).request().parse()

        val proxies = Utils.distinctBadIps(supplierProxyListData.proxies.values)
            .distinctBy { listOf(it.ip, it.port, it.protocol) }

        logger.info("Loaded ${proxies.size}")

        if(proxies.isNotEmpty())
            prepare(proxies)
    }

    //Shuffle proxies & allocate the endpoint server for it to connect to
    //We want to shuffle them as some proxies only allow one open connection
    //Also try with both Auto Read disabled & enabled
    private fun prepare(proxies : List<SimpleProxyDataType>) {
        val proxyDataList = mutableListOf<ProxyChannelData>()

        for(proxy in proxies) {
            val endpointServers = config.endpointServers
            for (endpointServer in endpointServers) {
                if(endpointServer.name.startsWith("!"))
                    continue
                proxyDataList.add(ProxyChannelData(proxy.ip, proxy.port, proxy.protocol, "", "",
                    false, endpointServer, ProxyChannelResponseData()))
                proxyDataList.add(ProxyChannelData(proxy.ip, proxy.port, proxy.protocol, "", "",
                    true, endpointServer, ProxyChannelResponseData()))
            }
        }
        //Garbage collect before testing the proxies
        System.gc()

        //Shuffled the list so that the proxies are randomized
        //This avoids testing the same server on a different port at the same time -
        //which can lead to being blocked by the public proxy
        val listSize = proxyDataList.size
        proxyDataList.shuffled().forEachIndexed { index, proxyData ->
            when { //Every 100 proxies, update the checks per second thread
                index % 100 == 0 -> { ChecksMonitor.currentIndex = index; ChecksMonitor.proxyListSize = listSize }
            }
            connect(proxyData)
        }

        running.set(false)

        logger.info("Completed ProxyConnect Task")
    }

    private fun connect(proxyData : ProxyChannelData) {
        val endpoint = proxyData.endpointServer ?: return
        val awaitTime = (if(pause.get()) 30000 else config.connectAwait)

        Bootstrap().group(workerGroup)
            .channel(NioSocketChannel::class.java)
            .resolver(NoopAddressResolverGroup.INSTANCE)
            .option(ChannelOption.AUTO_READ, proxyData.autoRead)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.timeout)
            .handler(ProxyChannelInitializer(proxyData))
            .connect(InetSocketAddress(endpoint.ip, endpoint.port))
            .channel().closeFuture().awaitUninterruptibly(awaitTime)
    }

}