package pe.proxy.proxybuilder2.git

import org.slf4j.LoggerFactory

/**
 * @author Kai
 */
class GitActions {

    private val logger = LoggerFactory.getLogger(GitActions::class.java)

    private val shell = Shell()

    fun init() {

        GitStage.values()
            .asSequence()
            .filter { it.command.isNotEmpty() }
            .forEach { nextAction(it) }

        logger.info(GitStage.COMPLETE.name)

    }

    private fun nextAction(stage : GitStage) {
        shell.parseCommand(stage.command)
        logger.info(stage.name)
    }

}