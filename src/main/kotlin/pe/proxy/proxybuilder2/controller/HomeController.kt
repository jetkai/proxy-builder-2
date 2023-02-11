package pe.proxy.proxybuilder2.controller

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class HomeController {

    @RequestMapping(value = ["/"], produces = ["application/json"], method = [RequestMethod.GET])
    private fun onHomeResponse(request : HttpServletRequest) : ResponseEntity<HomeResponseEntity> {
        val entity = HomeResponseEntity(request)
        return ResponseEntity<HomeResponseEntity>(entity, HttpStatus.OK)
    }

}

/**
 * HomeResponseEntity
 *
 * @author Kai
 * @version 1.0, 12/06/2022
 */
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL) //Ignore nulls
@Suppress("unused") //All val's being used
internal class HomeResponseEntity(private val request : HttpServletRequest) {

    val remoteAddr : String = request.remoteAddr
    val protocol : String = request.protocol
    val contentLength = request.contentLength
    val contentType : String = request.contentType
    val cookies : Array<Cookie> = request.cookies
    val queryString : String = request.queryString
    val locale : Locale = request.locale
    val timestamp = Date().time
    val headers : Map<String, String>
        get() {
            val map : MutableMap<String, String> = HashMap()
            val headers = request.headerNames.asIterator()
            while (headers.hasNext()) {
                val headerName = headers.next()
                map[headerName] = request.getHeader(headerName)
            }
            return map
        }

}