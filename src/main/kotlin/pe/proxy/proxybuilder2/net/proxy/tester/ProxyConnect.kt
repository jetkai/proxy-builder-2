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
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyDataType
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyListData
import pe.proxy.proxybuilder2.net.proxy.supplier.CustomProxySupplier
import pe.proxy.proxybuilder2.util.ProxyConfig
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
class ProxyConnect(val proxyConfig: ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ProxyConnect::class.java)

    private val workerGroup = NioEventLoopGroup()

    private val executor : ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    companion object { //TODO - CHANGE THIS
        val testedProxies = ConcurrentLinkedQueue<ProxyChannelData>()
    }

    override fun onApplicationEvent(event : ApplicationReadyEvent) {
        executor.scheduleAtFixedRate({ initialize() }, 0, 90, TimeUnit.MINUTES)
    }

    private fun initialize() {
        val supplierProxyListData = FinalProxyListData(mutableListOf())
        val proxySupplier = CustomProxySupplier(supplierProxyListData, proxyConfig)

        proxySupplier
            .request()  //Requests proxies from the web
            .parse()    //Attempt to parse the proxies from the web

        val proxies =
            //Utils.sortByIp(
            Utils.removeBadIps(proxySupplier.data.proxies
                .filter { it.protocol == "socks5" }.toMutableList()
                // )
            ).shuffled()

        logger.info("Loaded ${proxies.size}")

        loop(proxies)
    }

    fun loop(proxies : List<FinalProxyDataType>) {
        for (proxy in proxies) {
            val endpointServers = proxyConfig.endpointServers
            for (endpointServer in endpointServers) {
                if (endpointServer.name.startsWith("!"))
                    continue
                val proxyChannelData = ProxyChannelData(
                    proxy.ip, proxy.port, proxy.protocol, "", "",
                    endpointServer, ProxyChannelResponseData()
                )
                connect(proxyChannelData, workerGroup)
            }
        }
        logger.info("Completed ProxyConnect Task")
    }

    private fun connect(proxyChannelData : ProxyChannelData, workerGroup : NioEventLoopGroup) {
        val endpoint = proxyChannelData.endpointServer

        Bootstrap().group(workerGroup)
            .channel(NioSocketChannel::class.java)
            .resolver(NoopAddressResolverGroup.INSTANCE)
            .option(ChannelOption.AUTO_READ, false)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, proxyConfig.timeout)
            .handler(ProxyChannelInitializer(proxyChannelData))
            .connect(InetSocketAddress(endpoint.ip, endpoint.port))
            .channel().closeFuture().awaitUninterruptibly(proxyConfig.connectAwait)
    }

}