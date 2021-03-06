package pe.proxy.proxybuilder2.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pe.proxy.proxybuilder2.database.EntityForPublicView
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.util.Utils

/**
 * RunningStatus Class
 *
 * Used as a RestController for API Queries
 * @see onRunningCheck
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
@RestController
@RequestMapping("/api/v1/")
class RunningStatus(private val repository : ProxyRepository) {

    /**
     * Running Check
     * @since version 1.0
     * @return This function will return a JSON response with the current running data
     */
    @RequestMapping(value = ["running"], produces = ["application/json"], method = [RequestMethod.GET])
    fun onRunningCheck() : ResponseEntity<Any> {
        return ResponseEntity<Any>("HELLO", HttpStatus.OK)
    }

    @RequestMapping(value = ["online"], produces = ["application/json"], method = [RequestMethod.GET])
    fun onRequestForOnlineProxies(@RequestParam(value = "size", required = false) size : Boolean,
                                  @RequestParam(value = "fast", required = false) fast : Boolean,
                                  @RequestParam(value = "maxsize", required = false) maxSize : Int
    ): ResponseEntity<Any> {

        val lastOnlineSince = Utils.timestampMinus(90) //Within the past 90 minutes
        val repoResults = repository.findByLastSuccessAfter(lastOnlineSince)

        if(size)
            return ResponseEntity<Any>(repoResults.size, HttpStatus.OK)

        var proxies = mutableListOf<EntityForPublicView>()
        
        repoResults.mapTo(proxies) { EntityForPublicView().advanced(it) }

        if(fast) {
            var fastProxies = (proxies.map {
                    listOf(
                        it.endpoints?.ora_JP, it.endpoints?.ora_UK,
                        it.endpoints?.aws_NA, it.endpoints?.ms_HK
                    )
                }.flatMap { endpoints ->
                    endpoints.filter {
                        it?.uptime?.replace("%", "")?.toDouble()!! > 80
                                && it.connections?.success!! > 10
                    }
                })

            if(maxSize > 0)
                fastProxies = fastProxies.subList(0, maxSize - 1)

            return ResponseEntity<Any>(fastProxies, HttpStatus.OK)
        }

        if(maxSize > 0)
            proxies = proxies.subList(0, maxSize - 1)

        return ResponseEntity<Any>(proxies, HttpStatus.OK)
    }

}