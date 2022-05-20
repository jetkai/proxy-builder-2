package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ProxyChannelHandler(private val proxyChannelEncoderDecoder : ProxyChannelEncoderDecoder)
    : SimpleChannelInboundHandler<String>() {

    override fun channelActive(ctx : ChannelHandlerContext) {
        proxyChannelEncoderDecoder.encode(ctx)
    }

    override fun channelRead(ctx : ChannelHandlerContext, msg : Any) {
        if(msg is ByteBuf)
            proxyChannelEncoderDecoder.decode(ctx, msg)
        flushAndClose(ctx.channel())
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        super.channelRead(ctx, msg)
    }

    override fun channelInactive(ctx : ChannelHandlerContext) {
        flushAndClose(ctx.channel())
    }

    @Deprecated("TODO -> This will be deprecated in a future Netty build")
    override fun exceptionCaught(ctx : ChannelHandlerContext, cause : Throwable) {
        cause.printStackTrace()
        flushAndClose(ctx.channel())
    }

    private fun flushAndClose(channel : Channel) {
        if (channel.isActive)
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
    }

}