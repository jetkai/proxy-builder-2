package pe.proxy.proxybuilder2.git

/**
 * GitStage
 *
 * @author Kai
 * @version 1.0, 19/05/2022
 */
enum class GitStage(var command : Array<String>) {

    NOT_RUNNING(emptyArray()),
    RUNNING(arrayOf("git", "add", ".")),
    COMMITTING(arrayOf("git", "commit", "-m")),
    PUSHING(arrayOf("git", "push", "origin", "main")),
    COMPLETE(emptyArray())

}