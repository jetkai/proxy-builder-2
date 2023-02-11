package pe.proxy.proxybuilder2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import pe.proxy.proxybuilder2.util.ProxyConfig

/**
 * ProxyBuilder2Application
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */

@EnableCaching
@SpringBootApplication(exclude = [(UserDetailsServiceAutoConfiguration::class), (ErrorMvcAutoConfiguration::class)])
@EnableConfigurationProperties(ProxyConfig::class)
class ProxyBuilder2Application

fun main(args: Array<String>) {
    runApplication<ProxyBuilder2Application>(*args)
}
