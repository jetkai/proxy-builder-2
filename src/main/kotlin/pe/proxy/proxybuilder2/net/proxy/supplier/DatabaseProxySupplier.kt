package pe.proxy.proxybuilder2.net.proxy.supplier

import org.slf4j.LoggerFactory
import pe.proxy.proxybuilder2.database.EntityForPublicView
import pe.proxy.proxybuilder2.database.ProxyEntity
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyDataType
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyListData

/**
 * DatabaseProxySupplier
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
class DatabaseProxySupplier(override val data: FinalProxyListData,
                            private val repository: ProxyRepository) : IProxySupplier {

    private val logger = LoggerFactory.getLogger(DatabaseProxySupplier::class.java)

    private var proxyEntities : List<ProxyEntity>?=null

    override fun request() : DatabaseProxySupplier {
        proxyEntities = repository.findAll().toList()
        return this
    }

    //TODO Modify parsing for CustomProxySupplier
    override fun parse() {

        val proxies = mutableListOf<EntityForPublicView>()
        proxyEntities?.mapTo(proxies) { EntityForPublicView().advanced(it) }

        val protocols = listOf("http", "https", "socks4", "socks5")
        for(protocolName in protocols) {
            proxies.flatMap { prox ->
                prox.protocols
                    ?.map { repo -> prox to repo }
                    ?.filter { it.second.type == protocolName }!!
            }.distinctBy { listOf(it.first.ip, it.first.port) }.forEach {
                data.proxies.add(FinalProxyDataType(protocolName, it.first.ip!!, it.first.port!!))
            }
        }

        logger.info(
            "Parsing complete -> " +
                    "[HTTP:${this.data.size("http")} | HTTPS:${this.data.size("https")} | " +
                    "SOCKS4:${this.data.size("socks4")} | SOCKS5:${this.data.size("socks5")}]"
        )
    }

}