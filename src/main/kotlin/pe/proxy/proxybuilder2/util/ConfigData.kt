package pe.proxy.proxybuilder2.util

import kotlinx.serialization.Serializable

/**
 * @author Kai
 */
@Serializable
data class ConfigData(
    val proxyOutputPath : String, val victimTestServerIp : Array<String>, val victimTestServerPort : IntArray,
    val proxyEndpointUrl : String, val proxyEndpointGithubUrl : String, val proxyGithubList : Array<String>,
    val localDatabasePath : String
)