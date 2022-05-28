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
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.database.*
import pe.proxy.proxybuilder2.net.proxy.data.SupplierProxyListData
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Utils
import java.io.File


@Component
class CustomFileWriter(val repository: ProxyRepository,
                       val config: ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(CustomFileWriter::class.java)

    @Throws
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        try {
            initialize()
        } catch (e : Exception) {
            e.printStackTrace()
        } catch (t : Throwable) {
            t.printStackTrace()
        }
    }

    @Throws
    fun initialize() {
        //val lastOnlineSince = Utils.timestampMinus(30) //Within the past 30 minutes
        //val repo = repository.findByLastSuccessAfter(lastOnlineSince)
        val repo = repository.findByLocationIsNotNullAndLastSuccessIsNotNull()
        for (viewType in ViewType.values()) {
            if (viewType == ViewType.CLASSIC) {
                val proxies = convertClassic(repo, viewType)
                for (fileExtension in FileExtension.values())
                    write(proxies, viewType, fileExtension)
            } else {
                val proxies = convert(repo, viewType)
                for (fileExtension in FileExtension.values())
                    write(proxies, viewType, fileExtension)
            }
        }
    }

    fun convert(repo : List<ProxyEntity>, viewType: ViewType) : MutableList<EntityForPublicView> {
        val proxies = mutableListOf<EntityForPublicView>()
        when (viewType) {
            ViewType.BASIC -> { repo.mapTo(proxies) { EntityForPublicView().basic(it) } }
            ViewType.ADVANCED -> { repo.mapTo(proxies) { EntityForPublicView().advanced(it) } }
            else -> {}
        }
        return proxies
    }

    fun convertClassic(repo : List<ProxyEntity>, viewType: ViewType) : SupplierProxyListData {
        val proxies = SupplierProxyListData(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())
        if (viewType == ViewType.CLASSIC) {
            repo.forEach { EntityForPublicView().classic(proxies, it) }
        }
        return proxies
    }

    @Throws
    fun write(proxies : List<EntityForPublicView>, viewType: ViewType, extension : FileExtension) {
        var file = File("${config.outputPath}/online-proxies/${extension.name.lowercase()}" +
                "/proxies-${viewType.name.lowercase()}.${extension.name.lowercase()}")
        if (file.lastModified() >= Utils.timestampMinus(60).time) //Prevent overwriting file within 60 mins
            return

        if(extension == FileExtension.TXT && viewType == ViewType.BASIC) {
            //All Proxies
            var proxiesAsString = proxies.joinToString(separator = "\n") { "${it.ip}:${it.port}" }
            file = File("${config.outputPath}/online-proxies/${extension.name.lowercase()}" +
                    "/proxies.${extension.name.lowercase()}")
            file.writeText(proxiesAsString)

            val protocols = listOf("http", "https", "socks4", "socks5")
            for(protocolName in protocols) {
                proxiesAsString = proxies.flatMap { prox ->
                    prox.protocols
                        ?.map { repo -> prox to repo }
                        ?.filter { it.second.type == protocolName }!!
                }.joinToString(separator = "\n") { "${it.first.ip}:${it.first.port}" }
                file = File(
                    "${config.outputPath}/online-proxies/${extension.name.lowercase()}" +
                            "/proxies-$protocolName.${extension.name.lowercase()}"
                )
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
                ViewType.ADVANCED -> {
                    val entity = EntityForPublicViewForCSV().convert(proxies)
                    val schema: CsvSchema = mapper.schemaFor(EntityForPublicViewForCSV::class.java)
                        .withHeader().sortedBy(* EntityForPublicViewForCSV().order(viewType))
                    mapper.writerFor(List::class.java).with(schema).writeValue(file, entity)
                }
                ViewType.BASIC -> { //TODO
                }
                else -> {}
            }
            return
        }

        mapper.writeValue(file, proxies)
    }

    fun write(proxies : SupplierProxyListData, viewType: ViewType, extension : FileExtension) {
        //val file = File("${config.outputPath}/test.${extension.name.lowercase()}")
        val file = File("${config.outputPath}/online-proxies/${extension.name.lowercase()}" +
                "/proxies.${extension.name.lowercase()}")
        if (file.lastModified() >= Utils.timestampMinus(60).time) //Prevent overwriting file within 60 mins
            return

        val mapper = mapper(extension)
            ?.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            ?.enable(SerializationFeature.INDENT_OUTPUT)
            ?: return logger.error("Unable to read mapper extension type")

        if(extension == FileExtension.CSV && mapper is CsvMapper) {
            when (viewType) {
                ViewType.CLASSIC -> {
                    val schema: CsvSchema = mapper.schemaFor(SupplierProxyListData::class.java).withHeader()
                        .sortedBy(* EntityForPublicViewForCSV().order(viewType))
                    mapper.writerFor(SupplierProxyListData::class.java).with(schema).writeValue(file, proxies)
                }
                else -> {}
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

    enum class ViewType(private val id : Int) {
        CLASSIC(0),
        BASIC(1),
        ADVANCED(2);
        //   MODERN(3),

        open fun getById(id : Int) : ViewType {
            return ViewType.values().firstOrNull { it.id == id } ?: CLASSIC
        }
    }

    enum class FileExtension(private val id : Int) {
        TXT(0),
        JSON(1),
        YAML(2),
        XML(3),
        CSV(4);

        open fun getById(id : Int) : FileExtension {
            return FileExtension.values().firstOrNull { it.id == id } ?: TXT
        }
    }

}