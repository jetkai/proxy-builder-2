package pe.proxy.proxybuilder2.net.node

/**
 * @author Kai
 */
class NodeList {

    private val head : Node = Node()
    private var current : Node? = null

    init {
        head.prev = head
        head.next = head
    }

    fun popHead(): Node? {
        val node = head.prev
        return if (node === head) {
            null
        } else {
            node!!.unlink()
            node
        }
    }

    val next: Node?
        get() {
            val node = current
            if (node === head) {
                current = null
                return null
            }
            current = node!!.next
            return node
        }

}