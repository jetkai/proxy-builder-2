package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.proxy.HttpProxyHandler
import io.netty.handler.proxy.ProxyHandler
import io.netty.handler.proxy.Socks4ProxyHandler
import io.netty.handler.proxy.Socks5ProxyHandler
import io.netty.handler.timeout.IdleStateHandler
import pe.proxy.proxybuilder2.database.ProxyRepository
import java.net.InetSocketAddress

class ProxyChannelInitializer(private val proxy : ProxyChannelData,
                              private val proxyRepository: ProxyRepository) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel : SocketChannel) {
        val proxyHandler = proxyHandler()
        if(proxyHandler == null) { //Proxy protocol not specified
            channel.close()
        } else {
            val pipeline = channel.pipeline()
            pipeline.addFirst(proxyHandler)
                .addLast(IdleStateHandler(5,5,5))

           /* if(proxyHandler is HttpProxyHandler) { //TODO
                pipeline.addLast(HttpRequestEncoder())
                    .addLast(HttpObjectAggregator(8192))
                    .addLast(HttpResponseDecoder())
            }*/

            val encoderDecoder = ProxyChannelEncoderDecoder(proxyRepository, proxy)
            val handler = ProxyChannelHandler(encoderDecoder)
            pipeline.addLast(handler)
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