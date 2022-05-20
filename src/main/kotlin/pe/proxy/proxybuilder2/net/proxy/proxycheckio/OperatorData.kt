package pe.proxy.proxybuilder2.net.proxy.proxycheckio

import com.fasterxml.jackson.databind.JsonNode

data class OperatorData(
    var name: String, var url: String, var anonymity: String, var popularity: String,
    var protocols: MutableList<JsonNode>, var policies: Policies)

data class Policies(var adFiltering : Boolean, var freeAccess : Boolean, var paidAccess : Boolean,
                    var portForwarding : Boolean, var logging : Boolean, var anonymousPayments : Boolean,
                    var cryptoPayments : Boolean, var traceableOwnership : Boolean)