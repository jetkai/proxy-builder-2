package pe.proxy.proxybuilder2.database

import org.springframework.dao.EmptyResultDataAccessException
import pe.proxy.proxybuilder2.util.Misc
import java.sql.SQLSyntaxErrorException
import java.sql.Timestamp

/**
 * ProxyInteraction Class
 *
 * Communicates with MariaDB through CrudRepository interface
 * @see updateEntity
 * @see getProxyEntity
 * @see getDefaultTemplate
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
class ProxyInteraction(private val proxyRepository : ProxyRepository) {

    //Update single ProxyEntity
    private fun updateEntity(proxy : ProxyEntity) {
        proxyRepository.save(proxy) //Writes to DB
    }

    //Updates ProxyEntity in bulk, single transaction
    fun updateEntities(proxy : MutableIterable<ProxyEntity>, mapName : String) {
        proxyRepository.saveAll(proxy) //Writes to DB
    }

    //Returns a single instance of ProxyEntity
    private fun getProxyEntity(ip : String) : ProxyEntity? {
        return try { //Checks if exists in DB
            proxyRepository.findByIp(ip)
        } catch (e : EmptyResultDataAccessException) { //Returns the default template if player doesn't exist in DB
            println(e.message)
            //getDefaultTemplate()
            null
        } catch (sql : SQLSyntaxErrorException) { //This is bad, should not get here!
            println(sql.message)
            //getDefaultTemplate()
            null
        }
    }

    //Returns the default template of the ProxyEntity
    private fun getDefaultTemplate(ip : String, port : Int, lastTested : Timestamp,
                                   connectData : String, countryData : String, riskData : String) : ProxyEntity {
        val proxyEntity = ProxyEntity()
        proxyEntity.id = 0
        proxyEntity.ip = ip
        proxyEntity.ports = port.toString()
        proxyEntity.connectData = connectData
        proxyEntity.countryData = countryData
        proxyEntity.riskData = riskData
        proxyEntity.dateAdded = Misc.getLocalDateAsTimestamp()
        proxyEntity.lastTested = lastTested
        proxyEntity.persistence = 0.0
        return proxyEntity
    }

}