package pe.proxy.proxybuilder2.util

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

object Tasks {

    private val logger = LoggerFactory.getLogger(Tasks::class.java)

    val thread = RunningThreads()

    data class RunningThreads(val endpointMonitor : RunningThreadAttributes?=RunningThreadAttributes(),
                              val sqlProxyMonitor: RunningThreadAttributes?=RunningThreadAttributes(),
                              val proxyConnect : RunningThreadAttributes?=RunningThreadAttributes(),
                              val queryApi : RunningThreadAttributes?=RunningThreadAttributes(),
                              val gitActions : RunningThreadAttributes?=RunningThreadAttributes()) {

        fun pauseAllExcept(exemptThread : Any) {
            val threads = listOf(endpointMonitor, sqlProxyMonitor, proxyConnect, queryApi, gitActions)
            for(thread in threads)
                if(thread != exemptThread)
                    thread?.pause?.set(true)

            logger.info("Pausing all threads except $thread")
        }

        fun resumeAll() {
            val threads = listOf(endpointMonitor, sqlProxyMonitor, proxyConnect, queryApi, gitActions)
            for(thread in threads)
                thread?.pause?.set(false)

            logger.info("Resuming all threads")
        }

    }

    data class RunningThreadAttributes(val running : AtomicBoolean?=AtomicBoolean(false),
                                       val pause : AtomicBoolean?=AtomicBoolean(false))

}