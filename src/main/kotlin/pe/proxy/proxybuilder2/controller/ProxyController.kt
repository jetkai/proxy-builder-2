package pe.proxy.proxybuilder2.controller

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvFactory
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pe.proxy.proxybuilder2.database.EntityForPublicView
import pe.proxy.proxybuilder2.database.EntityForPublicViewForCSV
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.net.proxy.data.SupplierProxyListData
import pe.proxy.proxybuilder2.util.CustomCacheManager
import pe.proxy.proxybuilder2.util.Utils
import pe.proxy.proxybuilder2.util.writer.CustomFileWriter

/**
 * ProxyController Class
 *
 * Used as a RestController for API Queries
 * @see onRunningCheck
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
@RestController
@RequestMapping("/api/v1/")
class ProxyController(private val repository : ProxyRepository, cacheManager : CacheManager) {

    private val logger = LoggerFactory.getLogger(ProxyController::class.java)

    private val cache = CustomCacheManager(cacheManager)

    /**
     * Running Check
     * @since version 1.0
     * @return This function will return a JSON response with the current running data
     */
    @RequestMapping(value = ["running"], produces = ["application/json"], method = [RequestMethod.GET])
    private fun onRunningCheck() : ResponseEntity<Any> = ResponseEntity<Any>("HELLO", HttpStatus.OK)

    @RequestMapping(value = ["proxies"], produces = ["application/json"], method = [RequestMethod.GET])
    private fun onRequestForOnlineProxies(
        @RequestParam(value = "protocols", required = false, defaultValue = "any") protocols : List<String>,
        @RequestParam(value = "countries", required = false, defaultValue = "any") countries : List<String>,
        @RequestParam(value = "format", required = false, defaultValue = "txt") format : String,
        @RequestParam(value = "amount", required = false, defaultValue = "0") amount : Int,
        @RequestParam(value = "stable", required = false, defaultValue = "false") stable : Boolean,
        @RequestParam(value = "type", required = false, defaultValue = "basic") type : String
    ) : ResponseEntity<Any> {

        //Grab the latest list of proxies
        //Clone list so that we do not modify the existing list in the cache
        var cachedProxies : MutableList<EntityForPublicView>? = getProxiesFromCache().toMutableList()

        val allowedTypes = arrayOf("classic", "basic", "advanced")
        if(!allowedTypes.contains(type))
            return ResponseEntity<Any>("Bad type.", HttpStatus.OK)

        //HANDLE -> Basic & Advanced Format (MutableList<EntityForPublicView>)
        if(!cachedProxies.isNullOrEmpty()) {

            //Only get protocols specified
            if (protocols.isNotEmpty() && !protocols.contains("any")) {
                cachedProxies = protocols.flatMap { prot ->
                    cachedProxies!!.filter { prox ->
                        prox.protocols?.any { it.type == prot } == true
                    }
                }.toMutableList()
            }

            //Filter only the countries the user has specified
            if (countries.isNotEmpty() && !countries.contains("any"))
                cachedProxies = countries.flatMap { country ->
                    cachedProxies!!.filter { prox ->
                        if (country.length == 2) //ISOCode is 2 chars long
                            prox.location?.isocode?.lowercase() == country.lowercase()
                        else
                            prox.location?.country?.lowercase() == country.lowercase()
                    }
                }.toMutableList()

            //Shuffle them up, so we do not provide in ip-order
            cachedProxies.shuffle()

            //Sort by most stable proxies (by highest uptime)
            if (stable)
                cachedProxies = cachedProxies.sortedBy { it.endpoints?.ora_UK?.uptime?.replace("%", "")?.toDouble() }
                    .asReversed().toMutableList()

            //Only provide the max amount of proxies the user requests
            var maxAmount = amount
            if (maxAmount > cachedProxies.size)
                maxAmount = cachedProxies.size
            if (maxAmount > 0)
                cachedProxies = cachedProxies.subList(0, maxAmount)

            //Reduce fields to be more basic
            if(type == "basic") {
                cachedProxies.forEach {
                    it.ping = it.endpoints?.let { endpoint -> Utils.lowestPing(endpoint) }
                    it.credentials = null
                    it.endpoints = null
                    it.dateAdded = null
                    it.lastSuccess = null
                    it.lastTested = null
                    it.location = null
                    it.detection = null
                    it.provider = null
                    it.protocols?.forEach { prot ->
                        prot.autoRead = null
                        prot.tls = null
                    }
                }
            }

            //Finally, produce the finished output
            //Do not allow classic view or TXT pass through this format, more stuff needs to be done for classic & txt
            if(type != "classic" && format.uppercase() != "TXT") {
                val responseBody = formatEntityListToString(cachedProxies, format.uppercase())
                return ResponseEntity<Any>(responseBody, HttpStatus.OK)
            }
        }

        //HANDLE -> Classic Format (SupplierProxyListData) & TXT Format
        //We will also be working off the Advanced Cached Data, as Classic Format does not have location/speed info
        if(!cachedProxies.isNullOrEmpty()) {

            //Fresh list to build
            val classicProxies = SupplierProxyListData(
                mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()
            )

            //Linking protocols to a string, so it's easier to loop through the lists
            val linkProtocols = arrayOf(
                arrayOf("http", classicProxies.http),
                arrayOf("https", classicProxies.https),
                arrayOf("socks4", classicProxies.socks4),
                arrayOf("socks5", classicProxies.socks5)
            )

            for(proxy in cachedProxies) {
                proxy.protocols?.forEach { proxyProtocol ->
                    val proxyList = linkProtocols.filter { it[0] == proxyProtocol.type }[0][1] as? MutableList<String>
                    proxyList?.add("${proxy.ip}:${proxyProtocol.port}")
                }
            }

            for(linkProtocol in linkProtocols) {
                val protocol = linkProtocol[0]
                val proxyList = linkProtocol[1] as? MutableList<String>
                if(!protocols.contains(protocol))
                    proxyList?.clear()
            }

            val amountToRemove = classicProxies.size() - amount
            classicProxies.removeAtRandom(amountToRemove)

            //Finally, produce the finished output
            val responseBody = formatEntityListToString(classicProxies, format.uppercase())
            return ResponseEntity<Any>(responseBody, HttpStatus.OK)
        }

        //TODO - Add database to track if this is being used
        logger.info("Produced response")

        return ResponseEntity<Any>("Empty.", HttpStatus.NO_CONTENT)
    }

    //Returns the file format requested (as a string)
    //THIS IS FOR ADVANCED VIEW
    private fun formatEntityListToString(proxies: List<EntityForPublicView>, format: String) : String {
        val extension = try { CustomFileWriter.FileExtension.valueOf(format)
        } catch (e : IllegalArgumentException) {
            logger.error(e.message)
            CustomFileWriter.FileExtension.valueOf("TXT")
        }

        val mapper = getMapper(extension)
            ?.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            ?.enable(SerializationFeature.INDENT_OUTPUT)

        if(extension == CustomFileWriter.FileExtension.CSV && mapper is CsvMapper) {
            val entity = EntityForPublicViewForCSV().convert(proxies)
            val schema: CsvSchema = mapper.schemaFor(EntityForPublicViewForCSV::class.java)
                .withHeader().sortedBy(* EntityForPublicViewForCSV().order(CustomFileWriter.ViewType.ADVANCED))
            return mapper.writerFor(List::class.java).with(schema).writeValueAsString(entity)
        }

        return mapper?.writeValueAsString(proxies) ?: ""
    }

    //Returns the file format requested (as a string)
    //THIS IS FOR CLASSIC VIEW
    private fun formatEntityListToString(proxies : SupplierProxyListData, format : String) : String {
        val extension = try { CustomFileWriter.FileExtension.valueOf(format)
        } catch (e : IllegalArgumentException) {
            logger.error(e.message)
            CustomFileWriter.FileExtension.valueOf("TXT")
        }

        if(extension == CustomFileWriter.FileExtension.TXT) {
            val allProxies = (proxies.http + proxies.https + proxies.socks4 + proxies.socks5).shuffled()
            return allProxies.joinToString(separator = "\n") { it }
        }

        val mapper = getMapper(extension)
            ?.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            ?.enable(SerializationFeature.INDENT_OUTPUT)

        if(extension == CustomFileWriter.FileExtension.CSV && mapper is CsvMapper)
            return "TODO"

        return mapper?.writeValueAsString(proxies) ?: ""
    }

    private fun getProxiesFromCache() : MutableList<EntityForPublicView> {
        val cachedProxies = cache.getCachedProxies()

        //Return if proxies from cache, if they are already cached
        if(!cachedProxies.isNullOrEmpty())
            return cachedProxies

        val lastOnlineSince = Utils.timestampMinus(9000) //Within the past 90 minutes
        val repoResults = repository.findByLastSuccessAfter(lastOnlineSince)

        val proxies = mutableListOf<EntityForPublicView>()

        //Map the objects to mutableLists
        repoResults.mapTo(proxies) { EntityForPublicView().advanced(it) }

        //Add to cache if doesn't already exist in cache
        cache.getCache().getCache("ProxyController_proxies")?.put("advanced", proxies)

        return proxies
    }

    private fun getMapper(extension : CustomFileWriter.FileExtension) : ObjectMapper? {
        return when (extension) {
            CustomFileWriter.FileExtension.YAML -> { ObjectMapper(YAMLFactory()) }
            CustomFileWriter.FileExtension.JSON -> { ObjectMapper(JsonFactory()) }
            CustomFileWriter.FileExtension.XML -> { XmlMapper(XmlFactory()) }
            CustomFileWriter.FileExtension.CSV -> { CsvMapper(CsvFactory()) }
            else -> null
        }
    }

}