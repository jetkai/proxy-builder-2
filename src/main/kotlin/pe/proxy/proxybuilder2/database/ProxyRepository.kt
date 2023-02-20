package pe.proxy.proxybuilder2.database

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp

/**
 * ProxyRepository
 *
 * Uses CrudRepository interface to run query's on the MariaDB
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
@Repository("ProxyRepository")
interface ProxyRepository : CrudRepository<ProxyEntity, String> {

    fun findByIp(ip : String) : ProxyEntity

    fun findByIpIn(ip : List<String>) : List<ProxyEntity>

    fun findByLocationIsNull() : List<ProxyEntity>

    fun findByLocationIsNullAndLastSuccessIsNotNull() : List<ProxyEntity>

    fun findByLocationIsNotNullAndLastSuccessIsNotNull() : List<ProxyEntity>

    fun findByIpInAndLastSuccessBefore(ip : List<String>, timestamp : Timestamp) : List<ProxyEntity>
    fun findByLastSuccessIsNullAndLastTestedBefore(lastTestedBefore : Timestamp): List<ProxyEntity>

    //@Override
    //@Cacheable("ProxyRepository_lastSuccess")
    fun findByLastSuccessAfter(timestamp : Timestamp) : List<ProxyEntity>

    fun findByLastSuccessIsNotNull() : List<ProxyEntity>

}