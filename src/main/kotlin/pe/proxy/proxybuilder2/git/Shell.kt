package pe.proxy.proxybuilder2.git

import com.sun.istack.logging.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pe.proxy.proxybuilder2.util.ProxyConfig
import pe.proxy.proxybuilder2.util.Utils

/**
 * Shell
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
@Component
class Shell {

    private val logger = Logger.getLogger(Shell::class.java)

    @Autowired
    final lateinit var appConfig : ProxyConfig

    fun parseCommand(gitArguments : Array<String>) {

        val directory = arrayOf("cd \"${appConfig.outputPath}\" && ")

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