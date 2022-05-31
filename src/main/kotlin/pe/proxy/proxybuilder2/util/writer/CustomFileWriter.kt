package pe.proxy.proxybuilder2.util.writer

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
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.LoggerFactory
import pe.proxy.proxybuilder2.database.EntityForPublicView
import pe.proxy.proxybuilder2.database.EntityForPublicViewForCSV
import pe.proxy.proxybuilder2.database.ProxyEntity
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.net.proxy.data.SupplierProxyListData
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Utils
import java.io.File
import java.nio.file.Files


class CustomFileWriter(private val repository : ProxyRepository, private val config : ProxyConfig)  {

    private val logger = LoggerFactory.getLogger(CustomFileWriter::class.java)

    fun initialize() {
        try {
            val lastOnlineSince = Utils.timestampMinus(90) //Within the past 90 minutes
            val lastOnlineSinceProxies = Utils.sortByIp(repository.findByLastSuccessAfter(lastOnlineSince))
            val archiveProxies = Utils.sortByIp(repository.findAll().toList())

            logger.info("Gathering last online since $lastOnlineSince")

            if (lastOnlineSinceProxies.isEmpty())
                logger.error("Empty proxy list, unable to write")
            if (archiveProxies.isEmpty())
                logger.error("Empty proxy archive list, unable to write")

            if(archiveProxies.isEmpty() || lastOnlineSinceProxies.isEmpty())
                return

            ViewType.values().forEach { viewType ->
                when (viewType) {
                    ViewType.CLASSIC -> {
                        val proxies = convertClassic(lastOnlineSinceProxies, viewType)
                        for (fileExtension in FileExtension.values())
                            write(proxies, viewType, fileExtension)
                    }
                    ViewType.ARCHIVE -> {
                        val proxies = convert(archiveProxies, viewType)
                        val classicArchive = convertClassic(archiveProxies, viewType)
                        for (fileExtension in FileExtension.values()) {
                            write(proxies, viewType, fileExtension)
                            write(classicArchive, viewType, fileExtension)
                        }
                    }
                    ViewType.BASIC, ViewType.ADVANCED -> {
                        val proxies = convert(lastOnlineSinceProxies, viewType)
                        for (fileExtension in FileExtension.values())
                            write(proxies, viewType, fileExtension)
                    }
                }
            }

            //Deprecated TODO - Update ReadMe Builder
            ReadMeFile(config).create(convert(lastOnlineSinceProxies, ViewType.ADVANCED), archiveProxies)
        } catch (e : Exception) {
            logger.error(e.localizedMessage)
        } catch (t : Throwable) {
            logger.error(t.localizedMessage)
        }
    }

    private fun convert(repo : List<ProxyEntity>, viewType: ViewType) : MutableList<EntityForPublicView> {
        val proxies = mutableListOf<EntityForPublicView>()
        when (viewType) {
            ViewType.BASIC -> { repo.mapTo(proxies) { EntityForPublicView().basic(it) } }
            ViewType.ADVANCED -> { repo.mapTo(proxies) { EntityForPublicView().advanced(it) } }
            ViewType.ARCHIVE -> { repo.mapTo(proxies) { EntityForPublicView().advanced(it) } }
            else -> { }
        }
        return proxies
    }

    private fun convertClassic(repo : List<ProxyEntity>, viewType: ViewType) : SupplierProxyListData {
        val proxies = SupplierProxyListData(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())
        if (viewType == ViewType.CLASSIC || viewType == ViewType.ARCHIVE) {
            repo.forEach { EntityForPublicView().classic(proxies, it) }
        }
        return proxies
    }

    private fun write(proxies : List<EntityForPublicView>, viewType: ViewType, extension : FileExtension) {
        var file = fileBuilder("proxies-${viewType.name.lowercase()}", extension, viewType)

        //Prevent overwriting file within 60 mins
        if (file.lastModified() >= Utils.timestampMinus(30).time)
            return

        if(extension == FileExtension.TXT && (viewType == ViewType.BASIC || viewType == ViewType.ARCHIVE)) {
            //All Proxies
            var proxiesAsString = proxies.joinToString(separator = "\n") { "${it.ip}:${it.port}" }
            file = fileBuilder("proxies", extension, viewType)
            file.writeText(proxiesAsString)

            val protocols = listOf("http", "https", "socks4", "socks5")
            for(protocolName in protocols) {
                proxiesAsString = proxies.flatMap { prox ->
                    prox.protocols
                        ?.map { repo -> prox to repo }
                        ?.filter { it.second.type == protocolName }!!
                }.distinctBy { listOf(it.first.ip, it.first.port) }
                    .joinToString(separator = "\n") { "${it.first.ip}:${it.first.port}" }
                file = fileBuilder("proxies-$protocolName", extension, viewType)
                file.writeText(proxiesAsString)
            }
            return
        }

        val mapper = mapper(extension)
            ?.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            ?.enable(SerializationFeature.INDENT_OUTPUT)
            ?: return logger.error("Unable to read mapper extension type")

        if(extension == FileExtension.CSV && mapper is CsvMapper) {
            when (viewType) {
                ViewType.ADVANCED, ViewType.ARCHIVE -> {
                    val entity = EntityForPublicViewForCSV().convert(proxies)
                    val schema: CsvSchema = mapper.schemaFor(EntityForPublicViewForCSV::class.java)
                        .withHeader().sortedBy(* EntityForPublicViewForCSV().order(viewType))
                    mapper.writerFor(List::class.java).with(schema).writeValue(file, entity)
                }
                ViewType.BASIC -> { }  //TODO
                else -> { }
            }
            return
        }

        mapper.writeValue(file, proxies)
    }

    private fun write(proxies : SupplierProxyListData, viewType: ViewType, extension : FileExtension) {
        val file = fileBuilder("proxies", extension, viewType)
        if (file.lastModified() >= Utils.timestampMinus(30).time) //Prevent overwriting file within 60 mins
            return

        val mapper = mapper(extension)
            ?.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            ?.enable(SerializationFeature.INDENT_OUTPUT)
            ?: return logger.error("Unable to read mapper extension type")

        if(extension == FileExtension.CSV && mapper is CsvMapper) { //TODO CHANGE THIS - Proxy Builder 1.0
            when (viewType) {
                ViewType.CLASSIC, ViewType.ARCHIVE -> {
                    val writer = Files.newBufferedWriter(file.toPath())
                    val format = CSVFormat.Builder.create()
                    format.setHeader("http", "https", "socks4", "socks5")

                    val csvPrinter = CSVPrinter(writer, format.build())

                    val socks4Size = proxies.socks4.size
                    val socks5Size = proxies.socks5.size
                    val httpSize = proxies.http.size
                    val httpsSize = proxies.https.size

                    var maxSize = 0
                    if (socks4Size > maxSize) maxSize = socks4Size
                    if (socks5Size > maxSize) maxSize = socks5Size
                    if (httpSize > maxSize) maxSize = httpSize
                    if (httpsSize > maxSize) maxSize = httpsSize

                    for (i in 0 until maxSize) {
                        var socks4Value = ""
                        var socks5Value = ""
                        var httpValue = ""
                        var httpsValue = ""
                        if (proxies.socks4.size > i) socks4Value = proxies.socks4[i]
                        if (proxies.socks5.size > i) socks5Value = proxies.socks5[i]
                        if (proxies.http.size > i) httpValue = proxies.http[i]
                        if (proxies.https.size > i) httpsValue = proxies.https[i]
                        csvPrinter.printRecord(httpValue, httpsValue, socks4Value, socks5Value)
                    }
                    csvPrinter.flush()
                    csvPrinter.close()
                }
                else -> { }
            }
            return
        }

        mapper.writeValue(file, proxies)
    }

    @Throws
    fun mapper(extension : FileExtension) : ObjectMapper? {
        return when (extension) {
            FileExtension.YAML -> { ObjectMapper(YAMLFactory()) }
            FileExtension.JSON -> { ObjectMapper(JsonFactory()) }
            FileExtension.XML -> { XmlMapper(XmlFactory()) }
            FileExtension.CSV -> { CsvMapper(CsvFactory()) }
            else -> null
        }
    }

    private fun fileBuilder(fileName : String, fileExtension : FileExtension, viewType : ViewType) : File {
        var filePath = config.outputPath
        val extension = fileExtension.name.lowercase()

        //Primary Folder
        filePath += (if(viewType == ViewType.ARCHIVE) "/archive/" else "/online-proxies/")

        //Sub Folder
        filePath += "$extension/"

        //File Name + Extension
        filePath += "$fileName.$extension"

        return File(filePath) //Final Path
    }

    enum class ViewType { CLASSIC, BASIC, ADVANCED, ARCHIVE; }

    enum class FileExtension { TXT, JSON, YAML, XML, CSV; }

}