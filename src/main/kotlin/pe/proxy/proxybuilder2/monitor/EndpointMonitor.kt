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

/**
 * EndpointMonitor
 *
 * @author Kai
 * @version 1.0, 25/05/2022
 */
@Component
class EndpointMonitor(val config : ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(EndpointMonitor::class.java)

    private val executor : ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    private val workerGroup = NioEventLoopGroup()

    companion object {
        val connected = AtomicBoolean(false)
    }

    private val pause = Tasks.thread.endpointMonitor?.pause!!

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if(config.enabledThreads.endpointMonitor)
            executor.scheduleAtFixedRate( { initialize() }, 0, 1, TimeUnit.HOURS )
    }

    fun initialize() {
        if(pause.get())
            return logger.info("Thread paused")

        for (endpoint in config.endpointServers) {
            if (endpoint.name.startsWith("!"))
                continue
            val connect = connect(endpoint) //ProxyConfig.EndpointServer("aws_na", "123.123.123.123", 43594)
            if(!connect) {
                sendText(endpoint)
            } else {
                connected.set(false)
                logger.info("Connected successfully to endpoint -> $endpoint")
            }
            Thread.sleep(30000L) //Wait 30 seconds before looping to next endpoint
        }
        logger.info("Finished this task")
    }

    fun sendText(endpoint : ProxyConfig.EndpointServer) {
        val t = config.twilio

        Twilio.init(t.sid, t.token)
        val message : Message = Message.creator(
            PhoneNumber(t.phoneNumberTo),
            PhoneNumber(t.phoneNumberFrom),
            "$endpoint is currently down.")
            .create()

        logger.info("Unable to connect to endpoint -> $endpoint | ${message.status}")
    }

    fun connect(endpoint : ProxyConfig.EndpointServer) : Boolean {
        try {
            Bootstrap().group(workerGroup)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.timeout)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(channel: SocketChannel) {
                        val direct = ProxyChannelData(
                            "0.0.0.0", 80, "", "", "", true,
                            endpoint, ProxyChannelResponseData()
                        )
                        val encoderDecoder = ProxyChannelEncoderDecoder(direct)
                        val handler = ProxyChannelHandler(encoderDecoder)
                        channel.pipeline()
                            .addLast(LoggingHandler(LogLevel.DEBUG))
                            .addLast(IdleStateHandler(0, 0, 10))
                            .addLast(handler)
                    }
                })
                .connect(InetSocketAddress(endpoint.ip, endpoint.port))
                .channel().closeFuture().sync()

            //Sleep for 5s, in-case waiting for connected boolean to bet set
            Thread.sleep(5000L)
            return connected.get()
        } catch (e: Exception) {
            logger.error(e.localizedMessage)
        }
        return false
    }

}