package pe.proxy.proxybuilder2.net.proxy.supplier

import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyListData

interface IProxySupplier {

    val finalProxyList : FinalProxyListData
    fun request() : IProxySupplier
    fun parse()

}