package pe.proxy.proxybuilder2.monitor

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * CachingMonitor
 *
 * @author Kai
 * @version 1.0, 09/06/2022
 */
@Component
@EnableScheduling
class CachingMonitor {

    private val logger = LoggerFactory.getLogger(CachingMonitor::class.java)

    @CacheEvict(allEntries = true, value = ["ProxyRepository_lastSuccess", "ProxyController_proxies"])
    @Scheduled(fixedDelay = (1000 * 60) * 15, initialDelay = 1000)
    fun flush() { //Runs every 15 minutes
        logger.info("Cache has been flushed")
    }

}