package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyListData
import pe.proxy.proxybuilder2.net.proxy.supplier.MainProxySupplier
import pe.proxy.proxybuilder2.util.Utils
import pe.proxy.proxybuilder2.util.YamlProperties
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Component
class ProxyConnect(val proxyRepository : ProxyRepository,
                   val apiConfig : YamlProperties) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ProxyConnect::class.java)

   private val bossGroup = NioEventLoopGroup()

    private val executor : ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    override fun onApplicationEvent(event : ApplicationReadyEvent) {
        executor.schedule({ ->
            initialize()
        }, 0, TimeUnit.SECONDS)
    }

    private fun initialize() {

        val supplierProxyListData = FinalProxyListData(mutableListOf())
        val proxySupplier = MainProxySupplier(supplierProxyListData, apiConfig)

        proxySupplier
            .request()  //Requests proxies from the web
            .parse()    //Attempt to parse the proxies from the web

        for(proxy in proxySupplier.finalProxyList.proxies) {
            val endpointServers = apiConfig.endpointServers
            for(endpointServer in endpointServers) {
                if(endpointServer.name.startsWith("!"))
                    continue
                val proxyChannelData = ProxyChannelData(proxy.ip, proxy.port, proxy.protocol,
                    "", "", Utils.getLocalDateNowAsTimestamp(), endpointServer
                )
                connect(proxyChannelData)
            }
        }

        logger.info("Done.")
    }

    private fun connect(proxyChannelData : ProxyChannelData) {
        val endpoint = proxyChannelData.endpointServer

        logger.info("Proxy -> ${proxyChannelData.ip}:${proxyChannelData.port} " +
                "| Endpoint -> ${endpoint.ip}:${endpoint.port}")

            val bootstrap = Bootstrap()
            bootstrap.group(bossGroup)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(ProxyChannelInitializer(proxyChannelData, proxyRepository))
                .connect(InetSocketAddress(endpoint.ip, endpoint.port)).await(50L)
    }
}