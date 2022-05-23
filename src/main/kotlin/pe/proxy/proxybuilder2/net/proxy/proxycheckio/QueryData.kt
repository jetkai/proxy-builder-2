package pe.proxy.proxybuilder2.net.proxy.proxycheckio

import kotlinx.serialization.Serializable

/**
 * QueryData
 *
 * @author Kai
 * @version 1.0, 21/05/2022
 */
@Serializable
data class LocationData(var continent : String?=null, var country : String?=null,
                        var isocode : String?=null, var region : String?=null, var regioncode: String?=null,
                        var city : String?=null, var latitude : Float?=null, var longitude : Float?=null,
                        var provider : String?=null, var organisation : String?=null, var asn : String?=null)

@Serializable
data class OperatorData(var name: String?=null, var url: String?=null, var anonymity: String?=null,
                        var popularity: String?=null, var policies: PoliciesData?=null) {
    fun isEmpty() : Boolean = name == null && url == null && anonymity == null
            && popularity == null && policies == null
}

@Serializable
data class PoliciesData(var ad_filtering : Boolean?=null, var free_access : Boolean?=null,
                    var paid_access : Boolean?=null, var port_forwarding : Boolean?=null,
                    var logging : Boolean?=null, var anonymous_payments : Boolean?=null,
                    var crypto_payments : Boolean?=null, var traceable_ownership : Boolean?=null) {
    fun isEmpty() : Boolean = ad_filtering == null && free_access == null && paid_access == null
            && port_forwarding == null && logging == null && anonymous_payments == null
            && crypto_payments == null && traceable_ownership == null
}

@Serializable
data class RiskData(var proxy : Boolean?=null, var type : String?=null, var risk : Int?=null)