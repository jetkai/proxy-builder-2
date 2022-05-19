package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.database.ProxyRepository
import java.net.InetSocketAddress

@Component
class ProxyConnect(val proxyRepository : ProxyRepository) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        initialize()
    }

    private fun initialize() {
        val bossGroup = NioEventLoopGroup(1)

        val bootstrap = Bootstrap()
        bootstrap.group(bossGroup)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.AUTO_CLOSE, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .handler(
                ProxyChannelInitializer(
                    ProxyChannelData(
                        "127.0.0.1", 12345, "socks5",
                        "username", "password"
                    ), proxyRepository
                )
            )
            .connect(InetSocketAddress("127.1.1.1", 43594))
    }
}