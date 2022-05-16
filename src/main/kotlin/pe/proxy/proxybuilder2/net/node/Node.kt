package pe.proxy.proxybuilder2.net.node

/**
 * @author Kai
 */
open class Node {

    var prev : Node? = null
    var next : Node? = null

    fun unlink() {
        if (next != null) {
            next!!.prev = prev
            prev!!.next = next
            prev = null
            next = null
        }
    }

}