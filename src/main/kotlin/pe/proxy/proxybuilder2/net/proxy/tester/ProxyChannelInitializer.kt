package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.proxy.HttpProxyHandler
import io.netty.handler.proxy.ProxyHandler
import io.netty.handler.proxy.Socks4ProxyHandler
import io.netty.handler.proxy.Socks5ProxyHandler
import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

/**
 * ProxyChannelInitializer
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
class ProxyChannelInitializer(private val proxy : ProxyChannelData) : ChannelInitializer<SocketChannel>() {

    private val logger = LoggerFactory.getLogger(ProxyChannelInitializer::class.java)

    override fun initChannel(channel : SocketChannel) {
        val proxyHandler = proxyHandler()
        if (proxyHandler == null) {
            logger.error("Protocol not specified")
            channel.close()
        } else {
            proxyHandler.setConnectTimeoutMillis(10000L)
            val encoderDecoder = ProxyChannelEncoderDecoder(proxy)
            val handler = ProxyChannelHandler(encoderDecoder)

            channel.pipeline()
                .addLast(LoggingHandler(LogLevel.DEBUG))
                .addLast(IdleStateHandler(0, 0, 10))
                .addLast(proxyHandler)
                .addLast(handler)

            /* if(proxyHandler is HttpProxyHandler) { //TODO
            pipeline.addLast(HttpRequestEncoder())
                .addLast(HttpObjectAggregator(8192))
                .addLast(HttpResponseDecoder())
            }*/
        }
    }

    private fun proxyHandler() : ProxyHandler? {
        val proxyAddress = InetSocketAddress(proxy.ip, proxy.port)
        return when (proxy.type) {
            "socks4" -> { Socks4ProxyHandler(proxyAddress, proxy.username) }
            "socks5" -> { Socks5ProxyHandler(proxyAddress, proxy.username, proxy.password) }
            "http", "https" -> { HttpProxyHandler(proxyAddress, proxy.username, proxy.password) }
            else -> null
        }
    }

}