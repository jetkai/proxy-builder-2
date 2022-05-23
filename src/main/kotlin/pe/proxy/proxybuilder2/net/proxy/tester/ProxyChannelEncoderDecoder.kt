package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory
import pe.proxy.proxybuilder2.util.Utils

/**
 * ProxyChannelEncoderDecoder
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
class ProxyChannelEncoderDecoder(val proxy : ProxyChannelData) {

    private val logger = LoggerFactory.getLogger(ProxyChannelEncoderDecoder::class.java)

    fun encode(ctx : ChannelHandlerContext) {
        proxy.response.startTime = Utils.timestampNow()
        //Sending two bytes to the endpoint, 14 & 0
        val buffer = Unpooled.buffer(2)
        buffer.writeByte(14)
        buffer.writeByte(0)
        ctx.channel().writeAndFlush(buffer)
    }

    fun decode(ctx : ChannelHandlerContext, buffer : ByteBuf) {
        //These are the Integers that we are expecting to come from the endpoint
        val values = intArrayOf(0xFFFFFF, 0x000000, 0xFF00FF, 0x00FF00, 0xFFFF00, 0x00FFFF)
        val readable = buffer.isReadable

        //Ensure that the Integers match, if so then set readable to $true
        if(buffer.readableBytes() == values.size * 4) {
            for ((index, value) in values.withIndex()) {
                if(!buffer.isReadable)
                    break

                val nextInt = buffer.readInt()
                if(value != nextInt)
                    break

                if (values.size - 1 == index)
                    proxy.response.cleanSocket = true
            }
        }

        val remoteIp = Utils.splitRemoteIp(ctx.channel().remoteAddress().toString())

        //Check if data is readable & ips match, update database entry if proxy is working
        if(readable) {
            if (Utils.ipMatch(proxy.ip, remoteIp)) {
                proxy.response.readable = true
                proxy.response.remoteIp = remoteIp
                proxy.response.connected = true
                proxy.response.endTime = Utils.timestampNow()
            } else {
                logger.warn("Proxy ip does not match ${proxy.ip} -> $remoteIp")
            }
        } else {
            logger.warn("Response data does not match -> ${proxy.remoteAddress()}")
        }

        buffer.release()
    }

}