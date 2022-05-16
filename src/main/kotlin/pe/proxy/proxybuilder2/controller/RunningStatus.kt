package pe.proxy.proxybuilder2.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import pe.proxy.proxybuilder2.database.ProxyRepository

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
class RunningStatus(proxyRepository : ProxyRepository) {

    /**
     * Running Check
     * @since version 1.0
     * @return This function will return a JSON response with the current running data
     */
    @RequestMapping(value = ["/api/proxype/running"], produces = ["application/json"], method = [RequestMethod.GET])
    fun onRunningCheck() : ResponseEntity<Any> {

        return ResponseEntity<Any>("<JSON DATA HERE SAMPLE>", HttpStatus.OK)
    }

}