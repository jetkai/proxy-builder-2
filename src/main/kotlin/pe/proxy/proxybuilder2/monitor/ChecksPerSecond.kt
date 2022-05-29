package pe.proxy.proxybuilder2.monitor

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyChannelData
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyChannelEncoderDecoder
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyChannelHandler
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyChannelResponseData
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Tasks
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@Component
class ChecksPerSecond(val config : ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ChecksPerSecond::class.java)

    private val executor : ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    companion object { var proxyListSize = 0; var currentIndex = 0 }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
            executor.scheduleAtFixedRate( { initialize() }, 0, 1, TimeUnit.SECONDS )
    }

    private var checksPerSecond = 0
    private var currentTime = 0
    private var remainingTime = "00:00:00"

    fun initialize() {
        val remaining = proxyListSize - currentIndex

        currentTime++

        if (currentIndex == 0)
            return

        checksPerSecond = currentIndex / currentTime

        val remainingAmount: Int = remaining / checksPerSecond

        remainingTime = String.format(
            "%d:%02d:%02d", remainingAmount / 3600, remainingAmount % 3600 / 60, remainingAmount % 60)

        logger.info("Time Remaining[$remainingTime] CPS[$checksPerSecond], ListSize[$proxyListSize]," +
                " Tested[$currentIndex], Remaining[$remaining]")
    }

}