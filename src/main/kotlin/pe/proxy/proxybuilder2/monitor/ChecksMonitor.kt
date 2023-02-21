package pe.proxy.proxybuilder2.monitor

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.util.ProxyConfig
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * ChecksMonitor
 *
 * @author Kai
 * @version 1.0, 29/05/2022
 */
@Component
class ChecksMonitor(private val config : ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ChecksMonitor::class.java)

    private val executor : ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    companion object { var proxyListSize = 0; var currentIndex = 0 }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if(config.enabledThreads.checksPerSecond)
            executor.scheduleAtFixedRate( { initialize() }, 0, 1, TimeUnit.SECONDS )
    }

    private var checksPerSecond = 0
    private var currentTime = 0
    private var remainingTime = "00:00:00"

    private fun initialize() {
        val remaining = proxyListSize - currentIndex

        currentTime++

        if (currentIndex == 0)
            return

        checksPerSecond = currentIndex / currentTime

        val remainingAmount: Int = remaining / checksPerSecond

        remainingTime = String.format(
            "%d:%02d:%02d", remainingAmount / 3600, remainingAmount % 3600 / 60, remainingAmount % 60)

        logger.info("Time Remaining[$remainingTime] CPS[$checksPerSecond], ListSize[$proxyListSize]," +
                " Tested[$currentIndex], Remaining[$remaining]\r")
    }

}