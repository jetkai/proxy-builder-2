package pe.proxy.proxybuilder2.database

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyChannelData
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyConnect
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ProxyMonitor
 *
 * Updates MariaDB every 15 seconds with proxies supplied from ProxyConnect()
 *
 * @author Kai
 * @version 1.0, 23/05/2022
 */
@Component
class ProxyMonitor(val proxyRepository : ProxyRepository) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ProxyMonitor::class.java)

    private val executor : ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    private var ready = AtomicBoolean(true)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        executor.scheduleAtFixedRate( { initialize() }, 15, 15, TimeUnit.SECONDS )
    }

    fun initialize() {
        try {
        logger.info("Monitor task - tick message0")
        if(ready.get()) {
            filter()
            logger.info("Monitor task - completed")
        }
        logger.info("Monitor task - tick message1")
        } catch (e : Exception) {
            e.printStackTrace()
        } catch (t : Throwable) {
            t.printStackTrace()
        }
    }

    //Filters the proxies into a list then calls the "write(proxy)" function
    fun filter() {
        ready.set(false)
        val proxies = ProxyConnect.testedProxies
        if(proxies.isNotEmpty())
            writeAll(proxies)
        ready.set(true)
    }

    //Writes proxy to database
    fun write(proxy : ProxyChannelData) {
        val interaction = ProxyInteraction(proxyRepository)
        val entity = interaction.getProxyEntity(proxy)
        if(entity != null)
            interaction.updateEntity(entity, proxy)
    }

    fun writeAll(proxies : ConcurrentLinkedQueue<ProxyChannelData>) {
        val interaction = ProxyInteraction(proxyRepository)
        val entities = interaction.getProxyEntities(proxies)
        if(entities.isNotEmpty())
            interaction.updateEntities(entities)
    }

}