package pe.proxy.proxybuilder2.util

import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.database.EntityForPublicView

@Component
class CustomCacheManager(private val cache : CacheManager) {

    //Gets the Advanced Proxies from the cache
    fun getCachedProxies() : MutableList<EntityForPublicView>? {
        val cacheEntry = cache.getCache("ProxyController_proxies")?.get("advanced")
        val valueWrapper = cacheEntry?.get() as? MutableList<*>
        return valueWrapper?.filterIsInstance<EntityForPublicView>() as? MutableList<EntityForPublicView>
    }

    fun getCache() : CacheManager {
        return cache
    }

}