package pe.proxy.proxybuilder2.net.proxy.proxycheckio

data class LocationData(var continent : String, var country : String, var isoCode : String, var region : String,
                        var regionCode: String, var city : String, var latitude : Float, var longitude : Float,
                        var provider : String, var organisation : String, var asn : String)