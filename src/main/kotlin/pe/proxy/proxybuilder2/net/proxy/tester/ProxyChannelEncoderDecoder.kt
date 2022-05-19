package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory

class ProxyChannelEncoderDecoder {

    private val logger = LoggerFactory.getLogger(ProxyChannelEncoderDecoder::class.java)

    fun encode(ctx : ChannelHandlerContext) {
        val buffer = Unpooled.buffer(2)
        buffer.writeByte(14)
        buffer.writeByte(0)
        ctx.channel().writeAndFlush(buffer)
    }

    fun decode(buffer : ByteBuf) {
        val values = intArrayOf(0xFFFFFF, 0x000000, 0xFF00FF, 0x00FF00, 0xFFFF00, 0x00FFFF)

        var valid = true
        for (value in values) {
            val nextInt = buffer.readInt()
            if(value != nextInt) {
                valid = false;
                break
            }
        }
        if(valid)
            logger.info("Connected!")

        buffer.release()
    }

}