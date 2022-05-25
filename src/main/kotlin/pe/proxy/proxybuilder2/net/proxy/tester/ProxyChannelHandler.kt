package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import org.slf4j.LoggerFactory
import pe.proxy.proxybuilder2.monitor.EndpointMonitor

/**
 * ProxyChannelHandler
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
class ProxyChannelHandler(private val encoderDecoder : ProxyChannelEncoderDecoder)
    : SimpleChannelInboundHandler<String>() {

    private val logger = LoggerFactory.getLogger(ProxyChannelHandler::class.java)

    override fun channelUnregistered(ctx : ChannelHandlerContext) { //Finished
        if(encoderDecoder.proxy.ip == "0.0.0.0") {
            EndpointMonitor.connected.set(encoderDecoder.proxy.response.cleanSocket == true)
        } else {
            ProxyConnect.testedProxies.offer(encoderDecoder.proxy)
        }
        super.channelUnregistered(ctx)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if(evt is IdleStateEvent) {
            val state = evt.state()
            if(state == IdleState.ALL_IDLE) {
                logger.info("State is idle, closing connection")
                ctx.close()
            }
        } else
            super.userEventTriggered(ctx, evt)
    }

    override fun channelActive(ctx : ChannelHandlerContext) {
        encoderDecoder.encode(ctx)
    }

    override fun channelRead(ctx : ChannelHandlerContext, msg : Any) {
        if(msg is ByteBuf)
            encoderDecoder.decode(ctx, msg)
        flushAndClose(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        super.channelRead(ctx, msg)
    }

    override fun channelInactive(ctx : ChannelHandlerContext) {
        flushAndClose(ctx)
    }

    @Deprecated("TODO -> This will be deprecated in a future Netty build")
    override fun exceptionCaught(ctx : ChannelHandlerContext, cause : Throwable) {
        flushAndClose(ctx)
        logger.warn(cause.localizedMessage)
    }

    private fun flushAndClose(ctx : ChannelHandlerContext) {
        val channel = ctx.channel()
        if (channel.isActive)
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
        else
            ctx.close()
    }

}