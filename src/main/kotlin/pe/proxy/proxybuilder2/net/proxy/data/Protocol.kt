package pe.proxy.proxybuilder2.net.proxy.data

import kotlinx.serialization.Serializable

@Serializable
data class Protocol(val protocols : MutableList<String> ?= mutableListOf())