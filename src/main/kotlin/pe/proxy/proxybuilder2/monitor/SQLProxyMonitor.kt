package pe.proxy.proxybuilder2.monitor

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.database.ProxyInteraction
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyChannelData
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyConnect
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Tasks
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * SQLProxyMonitor
 *
 * Updates MariaDB every 15 seconds with proxies supplied from ProxyConnect()
 *
 * @author Kai
 * @version 1.0, 23/05/2022
 */
@Component
class SQLProxyMonitor(val repository : ProxyRepository, val config : ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(SQLProxyMonitor::class.java)

    private val executor : ScheduledExecutorService = Executors.newScheduledThreadPool(4)

    private val running = Tasks.thread.sqlProxyMonitor?.running!!
    private val pause = Tasks.thread.proxyConnect?.pause!!

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if(config.enabledThreads.sqlProxyMonitor)
            executor.scheduleAtFixedRate( { initialize() }, 5, 5, TimeUnit.SECONDS )
    }

    fun initialize() = try {
        if (!running.get())
            filter()
        else
            logger.info("Task is not ready")
    } catch (e : Exception) {
        logger.error(e.localizedMessage)
    }

    //Filters the proxies into a list then calls the "write(proxy)" function
    fun filter() {
        if(pause.get())
            return logger.info("Thread paused")

        running.set(true)
        val proxies = ProxyConnect.testedProxies
        if(proxies.isNotEmpty())
            writeAll(proxies)

        running.set(false)
    }

    //Writes proxy to database
    fun write(proxy : ProxyChannelData) {
        val interaction = ProxyInteraction(repository)
        val entity = interaction.getProxyEntity(proxy)
        if(entity != null)
            interaction.updateEntity(entity, proxy)
    }

    fun writeAll(proxies : ConcurrentLinkedQueue<ProxyChannelData>) {
        val interaction = ProxyInteraction(repository)
        val entities = interaction.getProxyEntities(proxies)
        if(entities.isNotEmpty())
            interaction.updateEntities(entities)
    }

}