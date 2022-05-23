package pe.proxy.proxybuilder2.git

import java.text.SimpleDateFormat
import java.util.*

/**
 * GitStage
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
enum class GitStage(val command : Array<String>) {

    NOT_RUNNING(emptyArray()),
    RUNNING(arrayOf("git", "add", ".")),
    COMMITTING(arrayOf("git", "commit", "-m", //Action = Commit
        "Updated-${ SimpleDateFormat("dd/MM/yyyy-HH:mm:ss").format(Date()) }"
    )),
    PUSHING(arrayOf("git", "push", "origin", "main")),
    COMPLETE(emptyArray())

}