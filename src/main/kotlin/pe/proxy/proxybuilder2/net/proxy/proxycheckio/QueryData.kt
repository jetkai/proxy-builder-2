package pe.proxy.proxybuilder2.net.proxy.proxycheckio

import kotlinx.serialization.Serializable

@Serializable
data class LocationData(var continent : String?=null, var country : String?=null,
                        var isoCode : String?=null, var region : String?=null, var regionCode: String?=null,
                        var city : String?=null, var latitude : Float?=null, var longitude : Float?=null,
                        var provider : String?=null, var organisation : String?=null, var asn : String?=null)

@Serializable
data class OperatorData(var name: String?=null, var url: String?=null,
                        var anonymity: String?=null, var popularity: String?=null,
                        var policies: PoliciesData?=null)

@Serializable
data class PoliciesData(var ad_filtering : Boolean?=null, var free_access : Boolean?=null,
                    var paid_access : Boolean?=null, var port_forwarding : Boolean?=null,
                    var logging : Boolean?=null, var anonymous_payments : Boolean?=null,
                    var crypto_payments : Boolean?=null, var traceable_ownership : Boolean?=null)

@Serializable
data class RiskData(var proxy : Boolean?=null, var type : String?=null, var risk : Int?=null)