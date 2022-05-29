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
import pe.proxy.proxybuilder2.monitor.ChecksPerSecond
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyDataType
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyListData
import pe.proxy.proxybuilder2.net.proxy.supplier.LocalProxySupplier
import pe.proxy.proxybuilder2.net.proxy.supplier.MainProxySupplier
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Tasks
import pe.proxy.proxybuilder2.util.Utils
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * ProxyConnect
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
@Component
class ProxyConnect(val config : ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ProxyConnect::class.java)

    private val workerGroup = NioEventLoopGroup()

    private val executor : ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    companion object { //TODO - CHANGE THIS
        val testedProxies = ConcurrentLinkedQueue<ProxyChannelData>()
    }

    private val running = Tasks.thread.proxyConnect?.running!!
    private val pause = Tasks.thread.proxyConnect?.pause!!

    override fun onApplicationEvent(event : ApplicationReadyEvent) {
        if(config.enabledThreads.proxyConnect && !running.get())
            executor.scheduleAtFixedRate({ initialize() }, 0, 60, TimeUnit.MINUTES)
    }

    private fun initialize() {
        running.set(true)

        val supplierProxyListData = FinalProxyListData(mutableListOf())
        val proxySupplier = MainProxySupplier(supplierProxyListData, config)
        //Force load local proxies from proxies/*
        LocalProxySupplier(supplierProxyListData, config).request().parse()

        try {
            proxySupplier
                .request()  //Requests proxies from the web
                .parse()    //Attempt to parse the proxies from the web
        } catch (e : Exception) { //Attempt to re-run again after 30 seconds if error is captured
            logger.error(e.localizedMessage)
            Thread.sleep(30000L)
            initialize()
        } catch (t : Throwable) { //Attempt to re-run again after 30 seconds if error is captured
            logger.error(t.localizedMessage)
            Thread.sleep(30000L)
            initialize()
        }

        val proxies =
            //Utils.sortByIp(
            Utils.removeBadIps(supplierProxyListData.proxies
                //.filter { it.protocol == "socks5" }.toMutableList()
                // )
            ).distinctBy { listOf(it.ip, it.port, it.protocol) }

        logger.info("Loaded ${proxies.size}")

        if(proxies.isNotEmpty())
            prepare(proxies)
    }

    //Shuffle proxies & allocate the endpoint server for it to connect to
    //We want to shuffle them as some proxies only allow one open connection
    //Also try with both Auto Read disabled & enabled
    fun prepare(proxies : List<FinalProxyDataType>) {
        val proxyDataList = mutableListOf<ProxyChannelData>()

        for(proxy in proxies) {
            val endpointServers = config.endpointServers
            for (endpointServer in endpointServers) {
                if(endpointServer.name.startsWith("!"))
                    continue
                proxyDataList.add(ProxyChannelData(proxy.ip, proxy.port, proxy.protocol, "", "",
                    false, endpointServer, ProxyChannelResponseData()))
                proxyDataList.add(ProxyChannelData(proxy.ip, proxy.port, proxy.protocol, "", "",
                    false, endpointServer, ProxyChannelResponseData()))
            }
        }

        val listSize = proxyDataList.size
        proxyDataList.shuffled().forEachIndexed { index, proxyData ->
            when { //Every 100 proxies, update the checks per second thread
                index % 100 == 0 -> { ChecksPerSecond.currentIndex = index; ChecksPerSecond.proxyListSize = listSize }
            }
            connect(proxyData)
        }

        running.set(false)
        logger.info("Completed ProxyConnect Task")
    }

    fun connect(proxyData : ProxyChannelData) {
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