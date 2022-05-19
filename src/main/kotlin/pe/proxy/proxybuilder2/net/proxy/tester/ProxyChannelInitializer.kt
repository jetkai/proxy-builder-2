package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.proxy.HttpProxyHandler
import io.netty.handler.proxy.ProxyHandler
import io.netty.handler.proxy.Socks4ProxyHandler
import io.netty.handler.proxy.Socks5ProxyHandler
import java.net.InetSocketAddress

class ProxyChannelInitializer(val proxy : ProxyChannelData) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel : SocketChannel) {
        val proxyHandler = proxyHandler()
        if(proxyHandler == null) { //Proxy protocol not specified
            channel.close()
        } else {
            channel.pipeline()
                .addFirst(proxyHandler)
                .addLast(ProxyChannelHandler(ProxyChannelEncoderDecoder()))
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