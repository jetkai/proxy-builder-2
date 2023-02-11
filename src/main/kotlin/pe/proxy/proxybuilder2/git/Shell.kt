package pe.proxy.proxybuilder2.git

import org.slf4j.LoggerFactory
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Utils

/**
 * Shell
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
class Shell(val config : ProxyConfig) {

    private val logger = LoggerFactory.getLogger(Shell::class.java)

    fun parseCommand(gitArguments : Array<String>) {

        val directory = arrayOf("cd \"${config.outputPath}\" && ")

        val command : Array<String> = if(Utils.IS_WINDOWS) {
            val console = (arrayOf("cmd", "/c"/*, "start", "cmd", "/k"*/))
            console.plus(directory).plus(gitArguments)
        } else {
            val console = (arrayOf("/bin/sh", "-c"))
            console.plus(directory.joinToString { it } + gitArguments.joinToString(" ") { it })
        }

        execute(command)
    }

    private fun execute(command : Array<String>) {
        val reader = Runtime.getRuntime().exec(command).inputStream.reader()
        logger.info(reader.readText())
        Thread.sleep(60000) //Wait 60 seconds in-between commands
    }

}