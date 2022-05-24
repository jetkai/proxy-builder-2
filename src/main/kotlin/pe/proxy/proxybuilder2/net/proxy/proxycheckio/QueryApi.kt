package pe.proxy.proxybuilder2.net.proxy.proxycheckio

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.util.KotlinSerializer
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Utils
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * QueryApi
 *
 * @author Kai
 * @version 1.0, 21/05/2022
 */
@Component
class QueryApi(private val proxyRepository : ProxyRepository,
               private val appConfig : ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(QueryApi::class.java)

    private val client : HttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()

    private val builder : HttpRequest.Builder = HttpRequest.newBuilder()

    private val executor : ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    override fun onApplicationEvent(event : ApplicationReadyEvent) {
        //Runs query() every minute
        executor.scheduleAtFixedRate({ this.query() }, 0, 1, TimeUnit.MINUTES)
    }

    fun query() {
        logger.info("Querying ProxyCheck API")

        val entitiesFromRepository = proxyRepository.findByLocationIsNullAndLastSuccessIsNotNull()

        val entities = entitiesFromRepository.subList(0, 100) //Max 100 so that we don't overload the API

        for (entity in entities) {
            val request = builder.uri(apiURI(entity.ip!!)) //Impossible for entity.ip to be null
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build()

            val jsonResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get().body()
            val clazzes = Utils.serialize(jsonResponse)
            val serializer = KotlinSerializer()

            clazzes.forEach { clazz ->
                when (clazz) {
                    is LocationData -> entity.location = serializer.encodeString(clazz)
                    is RiskData -> entity.detection = serializer.encodeString(clazz)
                    is OperatorData -> {
                        val policiesClazz = clazzes[0]
                        if (policiesClazz is PoliciesData && !policiesClazz.isEmpty())
                            clazz.policies = policiesClazz
                        if (!clazz.isEmpty())
                            entity.provider = serializer.encodeString(clazz)
                    }
                }
            }
        }

        proxyRepository.saveAll(entities)

        logger.info("Query complete - ${entities.size}")
    }

    fun apiURI(proxyIp : String) : URI {
        return URI.create("http://proxycheck.io/v2/$proxyIp?" +
                "key=${appConfig.proxyCheckIo.apiKey}&vpn=1&asn=1&risk=2&seen=1&tag=msg")
        //http://proxycheck.io/v2/80.90.80.54?key=7y658u-044228-737v80-64lq59" (Example IP/Key)
    }

}