package pe.proxy.proxybuilder2.util.writer

import pe.proxy.proxybuilder2.database.EntityForPublicView
import pe.proxy.proxybuilder2.database.ProxyEntity
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Utils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Deprecated("Legacy Code - ProxyBuilder 1.0")
class ReadMeFile(private val config : ProxyConfig) {

    private val readmeFile = File("${config.outputPath}/README.md")

    //Legacy code from ProxyBuilder 1.0 - Change in future
    fun create(proxies : List<EntityForPublicView>, entityArchive : List<ProxyEntity>) {
        val readmeText = readmeFile.readText()

        val originalText = readmeText.substring(0, readmeText.indexOf("# [SAMPLE PROXIES]"))
        var extraText =
            "# [SAMPLE PROXIES] - ${SimpleDateFormat("[MMMM dd yyyy | hh:mm:ss]").format(Date())}\n\n"

        val http = proxies.filter { it.protocols?.any { it1 -> it1.type == "http" } == true }
            .map { it.ip + ":" + it.port }
        val https = proxies.filter { it.protocols?.any { it1 -> it1.type == "https" } == true }
            .map { it.ip + ":" + it.port }
        val socks4 = proxies.filter { it.protocols?.any { it1 -> it1.type == "socks4" } == true }
            .map { it.ip + ":" + it.port }
        val socks5 = proxies.filter { it.protocols?.any { it1 -> it1.type == "socks5" } == true }
            .map { it.ip + ":" + it.port }

        val archive = entityArchive.map { it.ip + ":" + it.port }
        val uniqueSize = proxies.distinctBy { it.ip }.size

        val proxiesInfoText =
            "### Proxy Statistics:\n" +
                    "- _Online Proxies (By Protocol):_\n" +
                    "   - **SOCKS4** -> ${socks4.size}\n" +
                    "   - **SOCKS5** -> ${socks5.size}\n" +
                    "   - **HTTP** -> ${http.size}\n" +
                    "   - **HTTPS** -> ${https.size}\n\n" +
                    "- _Proxies (Total):_\n" +
                    "   - **Online Proxies (SOCKS4/5 + HTTP/S)** -> ${proxies.size}\n" +
                    "   - **Unique Online Proxies** -> ${uniqueSize}\n" +
                    "   - **Unique Online/Offline Proxies (Archive)** -> ${archive.size}\n\n"

        //Append proxiesInfoText to extraText
        extraText += proxiesInfoText

        val gitHubLinkArray = config.githubList

        val codeTextArray = arrayListOf(
            arrayListOf("[SOCKS4 (${socks4.size}/$uniqueSize)](${gitHubLinkArray[0]})",
                Utils.sortByIp2(socks4.take(30)).joinToString(separator = "\n")),
            arrayListOf("[SOCKS5 (${socks5.size}/$uniqueSize)](${gitHubLinkArray[1]})",
                Utils.sortByIp2(socks5.take(30)).joinToString(separator = "\n")),
            arrayListOf("[HTTP (${http.size}/$uniqueSize)](${gitHubLinkArray[2]})",
                Utils.sortByIp2(http.take(30)).joinToString(separator = "\n")),
            arrayListOf("[HTTPS (${https.size}/$uniqueSize)](${gitHubLinkArray[3]})",
                Utils.sortByIp2(https.take(30)).joinToString(separator = "\n")),
            arrayListOf("[ARCHIVE ($uniqueSize/${archive.size})](${gitHubLinkArray[4]})",
                Utils.sortByIp2(archive.take(30)).joinToString(separator = "\n"))
        )

        for(codeText in codeTextArray) {
            extraText += ("## ${codeText[0]}"
                .plus("\n")
                .plus("```yaml")
                .plus("\n")
                .plus(codeText[1])
                .plus("\n```\n\n"
                ))
        }

        readmeFile.writeText(originalText
            .plus(extraText)
            .plus("\n\nThx Co Pure Gs - Sort Meister! \uD83D\uDC9F")
        )

    }

}