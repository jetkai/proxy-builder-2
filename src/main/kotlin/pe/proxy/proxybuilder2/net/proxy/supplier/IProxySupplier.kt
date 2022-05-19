package pe.proxy.proxybuilder2.net.proxy.supplier

import pe.proxy.proxybuilder2.net.proxy.data.SupplierProxyListData

interface IProxySupplier {

    val proxies : SupplierProxyListData
    fun request() : IProxySupplier
    fun parse()

}