package pe.proxy.proxybuilder2

import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import pe.proxy.proxybuilder2.util.ProxyConfig

@SpringBootTest(classes = [ProxyBuilder2Application::class])
@EnableConfigurationProperties(ProxyConfig::class)
class MainProxySupplierTest {

    @Test
    fun getProxyList_from_web_then_parse(apiConfig : ProxyConfig) {
       /* val supplierProxyListData = SupplierProxyListData(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())
        val mainProxySupplier = MainProxySupplier(supplierProxyListData, apiConfig)

        mainProxySupplier
            .request()  //Requests proxies from the web
            .parse()    //Attempt to parse the proxies from the web

        val isProxyListPopulated = !mainProxySupplier.finalProxyList.isEmpty()

        assert(isProxyListPopulated)*/
    }

}
