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
import pe.proxy.proxybuilder2.database.EntityForPublicView
import pe.proxy.proxybuilder2.database.EntityForPublicViewForCSV
import pe.proxy.proxybuilder2.database.ProxyEntity
import pe.proxy.proxybuilder2.database.ProxyRepository
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
        for(viewType in ViewType.values()) {
            val proxies = convert(repo, viewType)
            for(fileExtension in FileExtension.values())
                write(proxies, viewType, fileExtension)
        }
    }

    fun convert(repo : List<ProxyEntity>, viewType: ViewType) : MutableList<Any> {
        val proxies = mutableListOf<Any>()
        when (viewType) {
            ViewType.CLASSIC -> { repo.mapTo(proxies) { EntityForPublicView().classic(it) } }
            ViewType.BASIC -> { repo.mapTo(proxies) { EntityForPublicView().basic(it) } }
            ViewType.ADVANCED -> { repo.mapTo(proxies) { EntityForPublicView().advanced(it) } }
            else -> {}
        }
        return proxies
    }

    @Throws
    fun write(proxies : List<Any>, viewType: ViewType, extension : FileExtension) {
        //val file = File("${config.outputPath}/test.${extension.name.lowercase()}")
        val file = File("test/test-${viewType.name.lowercase()}.${extension.name.lowercase()}")
        if (file.lastModified() >= Utils.timestampMinus(60).time) //Prevent overwriting file within 60 mins
            return

        if(extension == FileExtension.TXT && viewType == ViewType.BASIC) {
            proxies as List<EntityForPublicView>
            val proxiesAsString = proxies.joinToString(separator = "\n") { "${it.ip}:${it.port}" }
            file.writeText(proxiesAsString)
            return
        }

        val mapper = mapper(extension)
            ?.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            ?.enable(SerializationFeature.INDENT_OUTPUT)
            ?: return logger.error("Unable to read mapper extension type")

        if(extension == FileExtension.CSV && mapper is CsvMapper) {
            if (viewType == ViewType.CLASSIC) {
                proxies as List<SupplierProxyListData>
                val schema: CsvSchema = mapper.schemaFor(SupplierProxyListData::class.java).withHeader()
                    .sortedBy(* EntityForPublicViewForCSV().order(viewType))
                mapper.writerFor(MutableList::class.java).with(schema).writeValue(file, proxies)
            } else if(viewType == ViewType.ADVANCED) {
                proxies as List<EntityForPublicView>
                val entity = EntityForPublicViewForCSV().convert(proxies)
                val schema: CsvSchema = mapper.schemaFor(EntityForPublicViewForCSV::class.java)
                    .withHeader().sortedBy(* EntityForPublicViewForCSV().order(viewType))
                mapper.writerFor(List::class.java).with(schema).writeValue(file, entity)
                return
            } else if(viewType == ViewType.BASIC) { //TODO
                return
            }
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
        MODERN(1),
        BASIC(2),
        ADVANCED(3);

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