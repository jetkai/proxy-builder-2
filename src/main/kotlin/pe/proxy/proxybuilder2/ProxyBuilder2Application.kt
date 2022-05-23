package pe.proxy.proxybuilder2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import pe.proxy.proxybuilder2.util.ProxyConfig

/**
 * ProxyBuilder2Application
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
@SpringBootApplication
@EnableConfigurationProperties(ProxyConfig::class)
class ProxyBuilder2Application

fun main(args: Array<String>) {
    runApplication<ProxyBuilder2Application>(*args)
}
