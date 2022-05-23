package pe.proxy.proxybuilder2.net.proxy.supplier

import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyListData

/**
 * IProxySupplier
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
interface IProxySupplier {

    val data : FinalProxyListData
    fun request() : IProxySupplier
    fun parse()

}