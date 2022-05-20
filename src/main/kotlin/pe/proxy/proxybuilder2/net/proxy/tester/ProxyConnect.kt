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
import pe.proxy.proxybuilder2.net.proxy.data.SupplierProxyListData
import pe.proxy.proxybuilder2.net.proxy.supplier.CustomProxySupplier
import pe.proxy.proxybuilder2.util.Utils
import pe.proxy.proxybuilder2.util.YamlProperties
import java.net.InetSocketAddress

@Component
class ProxyConnect(val proxyRepository : ProxyRepository,
                   val apiConfig : YamlProperties) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ProxyConnect::class.java)

   private val bossGroup = NioEventLoopGroup(250)

    override fun onApplicationEvent(event : ApplicationReadyEvent) {
        initialize()
    }

    private fun initialize() {

        val supplierProxyListData = SupplierProxyListData(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())
        val customProxySupplier = CustomProxySupplier(supplierProxyListData, apiConfig)

        customProxySupplier
            .request()  //Requests proxies from the web
            .parse()    //Attempt to parse the proxies from the web


  /*      for(proxy in customProxySupplier.proxies.https) {
            val ip = proxy.split(":")[0]
            val port = proxy.split(":")[1]
            val proxyChannelData = ProxyChannelData(ip, port.toInt(), "http", "", "")
            connect(proxyChannelData)
        }*/

        val proxyChannelData = ProxyChannelData("80.90.80.54", 8080,
            "http", "", "", Utils.getLocalDateNowAsTimestamp(), "aws_NA")
        connect(proxyChannelData)

        logger.info("Done.")
    }

    private fun connect(proxyChannelData : ProxyChannelData) {
        logger.info("Connecting to proxy ${proxyChannelData.ip}:${proxyChannelData.port}")

        val bootstrap = Bootstrap()
        bootstrap.group(bossGroup)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.AUTO_CLOSE, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .handler(ProxyChannelInitializer(proxyChannelData, proxyRepository))
            .connect(InetSocketAddress("132.145.126.3", 43594))
    }
}