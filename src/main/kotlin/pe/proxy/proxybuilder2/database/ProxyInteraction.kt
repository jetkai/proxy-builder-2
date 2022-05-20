package pe.proxy.proxybuilder2.database

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import pe.proxy.proxybuilder2.net.proxy.data.*
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyChannelData
import pe.proxy.proxybuilder2.util.Utils
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

    private val logger = LoggerFactory.getLogger(ProxyInteraction::class.java)

    //Update single ProxyEntity
     fun updateEntity(entity : ProxyEntity, proxy : ProxyChannelData) {
        entity.connectData = connectData(entity, proxy.endpoint, true, proxy.startTime)
        entity.credentials = credentials(entity, proxy)
        entity.protocols = protocols(entity, proxy)
        proxyRepository.save(entity) //Writes to DB
    }

    //Updates ProxyEntity in bulk, single transaction
    fun updateEntities(proxy : MutableIterable<ProxyEntity>, mapName : String) {
        proxyRepository.saveAll(proxy) //Writes to DB
    }

    //Returns a single instance of ProxyEntity
     fun getProxyEntity(proxy : ProxyChannelData) : ProxyEntity {
        return try { //Checks if exists in DB
            proxyRepository.findByIp(proxy.ip)
        } catch (e : EmptyResultDataAccessException) { //Returns the default template if player doesn't exist in DB
            logger.error(e.message)
            getDefaultTemplate(proxy.ip, proxy.port, proxy.type, "", null, null)
        } catch (sql : SQLSyntaxErrorException) { //This is bad, should not get here!
            logger.error(sql.message)
            getDefaultTemplate(proxy.ip, proxy.port, proxy.type, "", null, null)
        }
    }

    private fun connectData(entity : ProxyEntity, endpoint : String, connected : Boolean, startTime : Timestamp) : String {
        val connectDataJson = entity.connectData
        var connectData = PerformanceConnectData.default()
        if(connectDataJson != null && connectDataJson.isNotEmpty())
            connectData = KotlinDeserializer().decode(connectDataJson)

        var endpointData : EndpointServerData ?= null

        when (endpoint) { //Use Reflection in future
            "ovh_FR" -> { endpointData = connectData.ovh_FR }
            "aws_NA" -> { endpointData = connectData.aws_NA }
            "ora_UK" -> { endpointData = connectData.ora_UK }
            "ora_JP" -> { endpointData = connectData.ora_JP }
            "ms_HK" -> { endpointData = connectData.ms_HK }
        }

        if(endpointData != null) {
            val connections = endpointData.connections
            if (connected)
                connections.success += 1
            else
                connections.fail += 1
            endpointData.uptime = (connections.success * 100 / connections.success + connections.fail).toString() + "%"
            endpointData.ping = (Utils.getLocalDateNowAsTimestamp().time - startTime.time)
        }

        return KotlinSerializer().encode(connectData)
    }

    private fun credentials(entity : ProxyEntity, proxy : ProxyChannelData): String {
        val credentialsData = ProxyCredentials(proxy.username,  proxy.password)
        return KotlinSerializer().encode(credentialsData)
    }

    private fun protocols(entity : ProxyEntity, proxy : ProxyChannelData) : String {
        var protocolData = Protocol(mutableListOf())
        val protocolsJson = entity.connectData
        if(protocolsJson != null && protocolsJson.isNotEmpty())
            protocolData = KotlinDeserializer().decode(protocolsJson)
        if(!protocolData.protocols!!.contains(proxy.type))
            protocolData.protocols!!.add(proxy.type)
        return KotlinSerializer().encode(protocolData)
    }

    //Returns the default template of the ProxyEntity
     private fun getDefaultTemplate(ip : String, port : Int, protocol : String,
                                   connectData : String, countryData : String?, riskData : String?) : ProxyEntity {
        val proxyEntity = ProxyEntity()
        proxyEntity.id = 0
        proxyEntity.ip = ip
        proxyEntity.ports = port.toString()
        proxyEntity.protocols = protocol
        proxyEntity.credentials = null
        proxyEntity.connectData = connectData
        proxyEntity.countryData = countryData
        proxyEntity.riskData = riskData
        proxyEntity.dateAdded = Utils.getLocalDateNowAsTimestamp()
        proxyEntity.lastTested = Utils.getLocalDateNowAsTimestamp()
        return proxyEntity
    }

}