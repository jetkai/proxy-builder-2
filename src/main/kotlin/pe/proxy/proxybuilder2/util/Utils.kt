package pe.proxy.proxybuilder2.util

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken
import pe.proxy.proxybuilder2.net.proxy.data.FinalProxyDataType
import pe.proxy.proxybuilder2.net.proxy.data.PerformanceConnectData
import pe.proxy.proxybuilder2.net.proxy.proxycheckio.LocationData
import pe.proxy.proxybuilder2.net.proxy.proxycheckio.OperatorData
import pe.proxy.proxybuilder2.net.proxy.proxycheckio.PoliciesData
import pe.proxy.proxybuilder2.net.proxy.proxycheckio.RiskData
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.regex.Pattern
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

/**
 * Utils
 *
 * Miscellaneous functions used for date/time/math etc
 *
 * @author Kai
 * @version 1.0, 16/05/2022
 */
object Utils {

    val IS_WINDOWS = System.getProperty("os.name").startsWith("Windows")

    fun removeBadIps(proxies : MutableList<FinalProxyDataType>) : MutableList<FinalProxyDataType> {
        val pattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$")
        proxies.removeIf { !pattern.matcher(it.ip).matches() }
        return proxies
    }

    //Checks if proxy ip matches from start to end
    fun ipMatch(proxyIp : String, remoteIp : String?) : Boolean = remoteIp != null && remoteIp == proxyIp

    //remoteAddress = /0.0.0.0:8080
    fun splitRemoteIp(remoteAddress : String) : String? {
        if (!remoteAddress.contains(":"))
            return null
        return remoteAddress.split(":")[0].replace("/", "")
    }

    private fun formatIp(remoteAddress : String): String? {
        if(!remoteAddress.contains(":"))
            return null

        var ip = ""
        val port = remoteAddress.split(":")[1]

        if (remoteAddress.contains(":"))
            ip = replaceBadValues( remoteAddress.split(":").toTypedArray()[0])

        val ipArray = ip.split(".").toTypedArray()
        for (i in 0..3) {
            if (ipArray[i].startsWith("0") && ipArray[i] != "0")
                ipArray[i] = ipArray[i].substring(1)
        }
        return ipArray.joinToString(separator = ".").plus(":").plus(port)
    }

    private fun replaceBadValues(remoteAddress : String) : String =
        remoteAddress.replace("\n", "").replace("\r", "")

    fun sortByIp(proxyArray : MutableList<FinalProxyDataType>) : List<FinalProxyDataType> {
        val comparator : Comparator<FinalProxyDataType> = Comparator {
                ip1, ip2 -> toNumeric(ip1.ip).compareTo(toNumeric(ip2.ip))
        }
        return proxyArray.sortedWith(comparator)
    }

    fun sortByIp2(proxyArray : List<String>) : List<String> {
        val comparator : Comparator<String> = Comparator {
                ip1, ip2 -> toNumeric(ip1).compareTo(toNumeric(ip2))
        }
        return proxyArray.sortedWith(comparator)
    }

    private fun toNumeric(ip : String) : Long {
        if (!ip.contains("."))
            return 0L

        val finalIp = ip.split(".")

        return ((finalIp[0].toLong() shl 24) + (finalIp[1].toLong() shl 16) +
                (finalIp[2].toLong() shl 8) + finalIp[3].toLong())
    }

    private fun filterProxies(unfilteredProxyList : MutableList<String>) : MutableList<String> {
        val filteredProxyList = mutableListOf<String>()
        unfilteredProxyList.filter {
            it.contains(":")
        }.forEach {
            val nextProxy = formatIp(it)
            if(nextProxy != null)
                filteredProxyList.add(nextProxy)
        }
        return filteredProxyList.distinct().toMutableList()
    }

    fun lowestPing(connectionData: PerformanceConnectData) : Long {
        val pingArray = listOf(
            connectionData.aws_NA?.ping!!, connectionData.ms_HK?.ping!!,
            connectionData.ora_JP?.ping!!, connectionData.ora_UK?.ping!!,
            connectionData.ovh_FR?.ping!!
        )
        return pingArray.filter { it != 0L }.minOf { it }
    }

    //Custom Serializer - What could go wrong :)
    fun serialize(json : String) : List<*> {
        val entries = deserialize(json)

        //DO NOT CHANGE THE ORDER -> PoliciesData() has to be last in index
        val clazzes = listOf(LocationData(), RiskData(), OperatorData(), PoliciesData())

        for(clazz in clazzes) run {
            clazz::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
                .forEach {
                    if(entries.containsKey(it.name)) {
                        val value = entries.getValue(it.name)
                        it.setter.call(clazz, value)
                    }
                }
        }

        return clazzes
    }

    //Custom Deserializer - What could go wrong :)
    fun deserialize(json : String) : LinkedHashMap<String, Any> {
        val map = LinkedHashMap<String, Any>()
        val factory = JsonFactory()
        val jsonParser = factory.createParser(json)

        while (!jsonParser.isClosed) {
            val nextToken = jsonParser.nextToken()
            val name = jsonParser.currentName
            var value : Any? = null

            when (nextToken) {
                JsonToken.VALUE_STRING -> {
                    value = jsonParser.valueAsString
                    //Parse yes & no as true/false Booleans
                    if(value == "yes" || value == "no") {
                        value = (value == "yes")
                    }
                }
                JsonToken.VALUE_NUMBER_INT -> {
                    value = if(name == "longitude" || name == "latitude")
                        jsonParser.floatValue //Happens when longitude is 32 and not 32.841
                    else
                        jsonParser.valueAsInt
                }
                JsonToken.VALUE_TRUE, JsonToken.VALUE_FALSE -> { value = jsonParser.valueAsBoolean }
                JsonToken.VALUE_NUMBER_FLOAT -> { value = jsonParser.floatValue }
                else -> {}
            }

            if(name != null && value != null)
                map[name] = value
        }

        return map
    }

    fun timestampNow() : Timestamp = Timestamp.valueOf(LocalDateTime.now())

    fun timestampMinus(minusMinutes : Long) : Timestamp =
        Timestamp.valueOf(LocalDateTime.now().minusMinutes(minusMinutes))

}