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
class RunningStatus(val repository : ProxyRepository) {

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
    fun onRequestForOnlineProxies(@RequestParam(value = "size", required = false)
                                  size : Boolean) : ResponseEntity<Any> {
        val lastOnlineSince = Utils.timestampMinus(30) //Within the past 30 minutes
        val repoResults = repository.findByLastSuccessAfter(lastOnlineSince)

        if(size)
            return ResponseEntity<Any>(repoResults.size, HttpStatus.OK)

        val proxies = mutableListOf<EntityForPublicView>()

        //Temp deserializer (for testing) - this is currently hybrid with KTX & Jackson - Bad
        //Jackson doesn't parse KTX string decode properly, not sure how to parse KTX JsonElement to Jackson ATM
        repoResults.mapTo(proxies) { EntityForPublicView().basic(it) }

        return ResponseEntity<Any>(proxies, HttpStatus.OK)
    }

}