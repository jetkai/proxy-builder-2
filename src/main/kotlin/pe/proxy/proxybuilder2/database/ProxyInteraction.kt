package pe.proxy.proxybuilder2.database

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import pe.proxy.proxybuilder2.net.proxy.data.*
import pe.proxy.proxybuilder2.net.proxy.tester.ProxyChannelData
import pe.proxy.proxybuilder2.util.KotlinDeserializer
import pe.proxy.proxybuilder2.util.KotlinSerializer
import pe.proxy.proxybuilder2.util.Utils
import java.sql.SQLSyntaxErrorException
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * ProxyInteraction
 *
 * Communicates with MariaDB through CrudRepository interface
 * @see updateEntity
 * @see getProxyEntity
 * @see getDefaultTemplate
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
class ProxyInteraction(private val repository : ProxyRepository) {

    private val logger = LoggerFactory.getLogger(ProxyInteraction::class.java)

    //Update single ProxyEntity
    fun updateEntity(entity : ProxyEntity, proxy : ProxyChannelData) {
        entity.connections = connections(entity, proxy)
        entity.credentials = credentials(proxy)
        entity.protocols = protocols(entity, proxy)
        time(entity, proxy)
        repository.save(entity) //Writes to DB
    }

    fun updateEntities(entityData : List<EntityChannelData>) {
        val entities = mutableListOf<ProxyEntity>()
        for(data in entityData) {
            try {
                data.entity.connections = connections(data.entity, data.proxy)
                data.entity.credentials = credentials(data.proxy)
                data.entity.protocols = protocols(data.entity, data.proxy)
                time(data.entity, data.proxy)
                entities.add(data.entity)
            } catch (e : Exception) {
                e.printStackTrace()
            } catch (t : Throwable) {
                t.printStackTrace()
            }
        }
        repository.saveAll(entities)
    }

    //Returns a single instance of ProxyEntity
    fun getProxyEntity(proxy : ProxyChannelData) : ProxyEntity? {
        return try { //Checks if exists in DB
            repository.findByIp(proxy.ip)
        } catch (e : EmptyResultDataAccessException) { //Returns the default template if player doesn't exist in DB
            if(proxy.response.connected == true)
                getDefaultTemplate(proxy.ip, proxy.port)
            else null
        } catch (sql : SQLSyntaxErrorException) { //This is bad, should not get here!
            logger.error(sql.message)
            if(proxy.response.connected == true)
                getDefaultTemplate(proxy.ip, proxy.port)
            else null
        }
    }

    //Might be very heavy on resources, will test - May be better to update as a single entity
    fun getProxyEntities(proxies : ConcurrentLinkedQueue<ProxyChannelData>) : List<EntityChannelData> {
        val copyOfProxies = proxies.distinctBy { it.ip }.toMutableList() //Otherwise we are out of sync when comparing
        val proxyEntityList = mutableListOf<EntityChannelData>()
        try {
            val listIps = copyOfProxies.map { it.ip }.distinct()
            val repositoryList = repository.findByIpIn(listIps)

            //Existing Proxies (that already exist within the database)
            copyOfProxies.flatMap { prox ->
                repositoryList
                    .map { repo -> prox to repo }
                    .filter { it.second.id != 0 && it.second.ip == it.first.ip } }
                .mapTo(proxyEntityList) { EntityChannelData(it.second, it.first) }

            //New Proxies (that do not exist within database)
            copyOfProxies.filter { it.ip !in repositoryList.map { repo -> repo.ip } }
                 .filter { it.response.connected == true } //Only add proxies that have successfully connected
                // .distinctBy { it.ip } //Removes any duplicated ips
                .mapTo(proxyEntityList) { EntityChannelData(getDefaultTemplate(it.ip, it.port), it) }

            //Keep this as proxies.removeIf and not proxiesCopy, so we can remove the ones we have added to DB
           // proxies.removeIf { it.ip in repositoryList.map { repo -> repo.ip } }
            proxies.removeIf { it in proxyEntityList.map { repo -> repo.proxy } }
        } catch (e : Exception) {
            e.printStackTrace()
        } catch (t : Throwable) {
            t.printStackTrace()
        }
        return proxyEntityList
    }

    private fun connections(entity : ProxyEntity, proxy : ProxyChannelData) : String {
        val connectDataJson = entity.connections
        var connectData = PerformanceConnectData().default()
        if(connectDataJson != null && connectDataJson.isNotEmpty())
            connectData = KotlinDeserializer.decode(connectDataJson)!!

        var endpointData : EndpointServerData ?= null

        when (proxy.endpointServer?.name) { //Use Reflection in future
            "ovh_FR" -> { endpointData = connectData.ovh_FR }
            "aws_NA" -> { endpointData = connectData.aws_NA }
            "ora_UK" -> { endpointData = connectData.ora_UK }
            "ora_JP" -> { endpointData = connectData.ora_JP }
            "ms_HK" -> { endpointData = connectData.ms_HK }
        }

        if(endpointData != null) {

            val connections = endpointData.connections
            if(connections != null) {
                if (proxy.response.connected == true)
                    connections.success++
                else
                    connections.fail++

                if (connections.success > 0) {
                    val calculation : Double =
                        (connections.success * 100 / (connections.success + connections.fail)).toDouble()
                    endpointData.uptime = "$calculation%"
                }
            }

            val startTime = proxy.response.startTime
            val endTime = proxy.response.endTime
            if(startTime != null && endTime != null)
                endpointData.ping = (endTime.time - startTime.time)
        }

        return KotlinSerializer.encode(connectData)
    }

    private fun credentials(proxy: ProxyChannelData) : String? {
        val credentialsData = ProxyCredentials(proxy.username,  proxy.password)
        if(!credentialsData.empty())
            return KotlinSerializer.encode(credentialsData)

        return null
    }

    private fun protocols(entity : ProxyEntity, proxy : ProxyChannelData) : String {
        val defaultData = mutableListOf(
            ProtocolDataType(proxy.type, proxy.port, proxy.response.tls, proxy.response.autoRead)
        )
        var protocolData = ProtocolData(defaultData)
        try {
            val protocolsJson = entity.protocols
            if (protocolsJson != null && protocolsJson.isNotEmpty())
                protocolData = KotlinDeserializer.decode(protocolsJson)!!

            val protocol = protocolData.protocol
            val protocolIsNotInList = protocol.none { it.port == proxy.port && it.type == proxy.type }
            if (protocolIsNotInList)
                protocol.add(ProtocolDataType(proxy.type, proxy.port, proxy.response.tls, proxy.response.autoRead))
        } catch (e : Exception) {
            e.printStackTrace()
        } catch (t : Throwable) {
            t.printStackTrace()
        }

        return KotlinSerializer.encode(protocolData)
    }

    private fun time(entity : ProxyEntity, proxy : ProxyChannelData) {
        val connectedTime = proxy.response.endTime
        if(connectedTime != null)
            entity.lastSuccess = connectedTime

        entity.lastTested = Utils.timestampNow()
    }

    //Returns the default template of the ProxyEntity
    private fun getDefaultTemplate(ip: String, port: Int) : ProxyEntity {
        val currentTime = Utils.timestampNow()
        val entity = ProxyEntity()
        entity.id = 0
        entity.ip = ip
        entity.port = port
        entity.protocols = null
        entity.credentials = null
        entity.connections = null
        entity.location = null
        entity.detection = null
        entity.provider = null
        entity.dateAdded = currentTime
        entity.lastTested = currentTime
        entity.lastSuccess = null
        return entity
    }

}