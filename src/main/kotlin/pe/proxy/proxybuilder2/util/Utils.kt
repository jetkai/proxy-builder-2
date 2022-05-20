package pe.proxy.proxybuilder2.util

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

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

    //Checks if proxy ip matches from start to end
    fun ipMatch(proxyIp : String, remoteIp : String?) : Boolean {
        return remoteIp != null && remoteIp == proxyIp
    }

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

/*    fun sortByIp(proxyArray : MutableList<PerformanceProxyData>) : List<PerformanceProxyData> {
        val comparator : Comparator<PerformanceProxyData> = Comparator {
                ip1, ip2 -> toNumeric(ip1.ip).compareTo(toNumeric(ip2.ip))
        }
        return proxyArray.sortedWith(comparator)
    }*/

    private fun toNumeric(remoteAddress : String) : Long {
        if (!remoteAddress.contains(":") || !remoteAddress.contains("."))
            return -1

        val ip = replaceBadValues(remoteAddress.split(":")[0])
        val finalIpArray = ip.split(".")

        return ((finalIpArray[0].toLong() shl 24) + (finalIpArray[1].toLong() shl 16) + (finalIpArray[2].toLong() shl 8)
                + finalIpArray[3].toLong())
    }

    private fun replaceBadValues(remoteAddress : String) : String {
        return remoteAddress.replace("\n", "").replace("\r", "")
    }

    fun getDateAsTimestamp(): Timestamp {
        return Timestamp(Date().time)
    }

    fun getLocalDateNowAsTimestamp() : Timestamp {
        val date = LocalDateTime.now()
        return Timestamp.valueOf(date)
    }

    fun getLocalDateNowAsTimestamp(minusMinutes: Long): Timestamp {
        val date = LocalDateTime.now().minusMinutes(minusMinutes)
        return Timestamp.valueOf(date)
    }

}