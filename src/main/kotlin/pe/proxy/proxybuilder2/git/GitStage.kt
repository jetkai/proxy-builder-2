package pe.proxy.proxybuilder2.git

import java.text.SimpleDateFormat
import java.util.*

enum class GitStage(val command : Array<String>) {

    NOT_RUNNING(emptyArray()),
    RUNNING(arrayOf("git", "add", ".")),
    COMMITTING(arrayOf("git", "commit", "-m", //Action = Commit
        "Updated-${ SimpleDateFormat("dd/MM/yyyy-HH:mm:ss").format(Date()) }"
    )),
    PUSHING(arrayOf("git", "push", "origin", "main")),
    COMPLETE(emptyArray())

}