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
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Utils
import java.io.File


@Component
class CustomFileWriter(
    val repository: ProxyRepository,
    val config: ProxyConfig,
) : ApplicationListener<ApplicationReadyEvent> {

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
        val lastOnlineSince = Utils.timestampMinus(30) //Within the past 30 minutes
            // val repo = repository.findByLastSuccessAfter(lastOnlineSince)
        val repo = repository.findByLocationIsNotNullAndLastSuccessIsNotNull()

        val proxies = mutableListOf<EntityForPublicView>()

        //Temp deserializer (for testing) - this is currently hybrid with KTX & Jackson - Bad
        //Jackson doesn't parse KTX string decode properly, not sure how to parse KTX JsonElement to Jackson ATM
        repo.mapTo(proxies) { EntityForPublicView().advanced(it) }
        write(proxies, ViewType.ADVANCED, FileExtension.CSV)
    }

    @Throws
    fun write(proxies : List<EntityForPublicView>, viewType: ViewType, extension : FileExtension) {

        //val file = File("${config.outputPath}/test.${extension.name.lowercase()}")
        val file = File("test.${extension.name.lowercase()}")

        val mapper = mapper(extension)
            ?.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            ?.enable(SerializationFeature.INDENT_OUTPUT)
            ?: return logger.error("Unable to read mapper extension type")

        if(extension == FileExtension.CSV && mapper is CsvMapper) {
            val entity = EntityForPublicViewForCSV().convert(proxies)
            val schema : CsvSchema = mapper.schemaFor(EntityForPublicViewForCSV::class.java)
                .withHeader().sortedBy(* EntityForPublicViewForCSV().order())
            mapper.writerFor(List::class.java).with(schema).writeValue(file, entity)
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