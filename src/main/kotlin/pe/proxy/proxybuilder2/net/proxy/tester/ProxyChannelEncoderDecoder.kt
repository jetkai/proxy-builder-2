package pe.proxy.proxybuilder2.net.proxy.tester

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory
import pe.proxy.proxybuilder2.database.ProxyInteraction
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.util.Utils

class ProxyChannelEncoderDecoder(private val proxyRepository : ProxyRepository, val proxy : ProxyChannelData) {

    private val logger = LoggerFactory.getLogger(ProxyChannelEncoderDecoder::class.java)

    fun encode(ctx : ChannelHandlerContext) {
        val buffer = Unpooled.buffer(2)
        buffer.writeByte(14)
        buffer.writeByte(0)
        ctx.channel().writeAndFlush(buffer);
    }

    fun decode(ctx : ChannelHandlerContext, buffer : ByteBuf) {
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

        val remoteIp = Utils.splitRemoteIp(ctx.channel().remoteAddress().toString())
        val interaction = ProxyInteraction(proxyRepository)

        if(Utils.ipMatch(proxy.ip, remoteIp)) {
            val entity = interaction.getProxyEntity(proxy)
            interaction.updateEntity(entity, proxy)

            logger.info("Count -> ${proxyRepository.count()}")
        } else {
            logger.warn("IP did not match ${proxy.ip} -> $remoteIp")
        }

        buffer.release()
    }

}