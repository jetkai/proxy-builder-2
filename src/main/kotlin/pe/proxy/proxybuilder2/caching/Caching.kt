package pe.proxy.proxybuilder2.caching

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

//TODO Caffeine Caching - Currently using Spring Caching
class Caching {

    //@Bean
    fun config(): Caffeine<Any, Any> {
        return Caffeine.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS)
    }

}