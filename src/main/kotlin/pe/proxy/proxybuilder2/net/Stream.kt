package pe.proxy.proxybuilder2.net

import pe.proxy.proxybuilder2.net.node.NodeList

/**
 * @author Kai
 */
@Suppress("CAST_NEVER_SUCCEEDS")
class Stream {

    var buffer : ByteArray = ByteArray(0)
    private var offset = 0

    companion object {

        private val nodeList: NodeList = NodeList()
        private var count = 0

        fun create(size : Int): Stream {
            synchronized(nodeList) {
                var stream: Stream? = null
                if (count > 0) {
                    count--
                    stream = nodeList.popHead() as Stream
                }
                if (stream != null) {
                    stream.offset = 0
                    return stream
                }
            }
            val stream = Stream()
            stream.offset = 0
            stream.buffer = ByteArray(size)
            return stream
        }
    }

    fun writeByte(i : Int) {
        buffer[this.offset++] = i.toByte()
    }

}