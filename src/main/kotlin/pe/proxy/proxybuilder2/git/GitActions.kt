package pe.proxy.proxybuilder2.git

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.database.ProxyRepository
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Tasks
import pe.proxy.proxybuilder2.util.Utils
import pe.proxy.proxybuilder2.util.writer.CustomFileWriter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * GitActions
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
@Component
class GitActions(private val repository: ProxyRepository,
                 private val config: ProxyConfig) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(GitActions::class.java)

    private val shell = Shell(config)

    private val executor : ScheduledExecutorService = Executors.newScheduledThreadPool(2)

    override fun onApplicationEvent(event : ApplicationReadyEvent) {
        executor.scheduleAtFixedRate({ initialize() },50,90, TimeUnit.MINUTES)
    }

    fun initialize() {
        Tasks.thread.gitActions?.let { Tasks.thread.pauseAllExcept(it) }
        //Heavy task, requesting other threads to pause until this task has been completed
        val largeArchiveFile = File(config.outputPath + "/archive/json/proxies-archive.json")
        while(largeArchiveFile.lastModified() <= Utils.timestampMinus(30).time) {
            val task = Runnable {
                CustomFileWriter(repository, config).initialize()
            }
            executor.submit(task).get()
            Thread.sleep(10000L)
        }
        Tasks.thread.resumeAll()

        GitStage.values()
            .filter { it.command.isNotEmpty() }
            .forEach { nextAction(it) }

        logger.info(GitStage.COMPLETE.name)

    }

    private fun nextAction(stage : GitStage) {
        val command = when (stage) {
            GitStage.COMMITTING -> {
                stage.command
                    .plus("Updated-${ SimpleDateFormat("dd/MM/yyyy-HH:mm:ss").format(Date()) }")
            }
            else -> { stage.command }
        }
        shell.parseCommand(command)

        logger.info("${stage.name} -> $command")
    }

    enum class GitStage(val command : Array<String>) {

        NOT_RUNNING(emptyArray()),
        RUNNING(arrayOf("git", "add", ".")),
        COMMITTING(arrayOf("git", "commit", "-m")),
        PUSHING(arrayOf("git", "push", "origin", "main")),
        COMPLETE(emptyArray())

    }

}