package pe.proxy.proxybuilder2.util.writer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvFactory
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.database.EntityForPublicView
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Utils
import java.io.File


@Component
class CustomFileWriter(val repository: ProxyRepository,
                       val config : ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        initialize()
    }

    fun initialize() {
        val lastOnlineSince = Utils.timestampMinus(90) //Within the past 30 minutes
        val repo = repository.findByLastSuccessAfter(lastOnlineSince)

        val proxies = mutableListOf<EntityForPublicView>()

        //Temp deserializer (for testing) - this is currently hybrid with KTX & Jackson - Bad
        //Jackson doesn't parse KTX string decode properly, not sure how to parse KTX JsonElement to Jackson ATM
        repo.mapTo(proxies) { EntityForPublicView().basic(it) }
        write(proxies, ViewType.BASIC, FileExtension.YAML)
    }

    fun write(proxies : List<EntityForPublicView>, viewType: ViewType, extension : FileExtension) {

        val factory = factory(extension) ?: return


        val mapper = ObjectMapper(factory).setSerializationInclusion(JsonInclude.Include.NON_NULL)

        val path = extension.name.lowercase()

        mapper.writeValue(File("test.yaml"), proxies)

    }

    fun factory(extension : FileExtension) : JsonFactory? {
        return when (extension) {
            FileExtension.YAML -> { YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER) }
            FileExtension.JSON -> { JsonFactory() }
            FileExtension.XML -> { XmlFactory() }
            FileExtension.CSV -> { CsvFactory() }
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