package pe.proxy.proxybuilder2.database

import java.sql.Timestamp
import javax.persistence.*

/**
 * Entities
 *
 * Serializer/Deserializer placeholder
 * Reserve Name Ref: https://dev.mysql.com/doc/refman/8.0/en/keywords.html#keywords-8-0-detailed-I
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
@Entity
@Table(name = "all_proxies", schema = "localdb")
class ProxyEntity {

    @Basic
    @Column(name = "id", nullable = false)
    var id: Int? = null

    @Id
    @Column(name = "ip", nullable = false)
    var ip: String? = null

    @Basic
    @Column(name = "ports", nullable = false)
    var ports: String? = null

    @Basic
    @Column(name = "protocols", nullable = false)
    var protocols : String? = null

    @Basic
    @Column(name = "credentials", nullable = false)
    var credentials : String? = null

    @Basic
    @Column(name = "country_data", nullable = true)
    var countryData: String? = null

    @Basic
    @Column(name = "connect_data", nullable = false)
    var connectData: String? = null

    @Basic
    @Column(name = "risk_data", nullable = true)
    var riskData: String? = null

    @Basic
    @Column(name = "date_added", nullable = false)
    var dateAdded: Timestamp? = null

    @Basic
    @Column(name = "last_tested", nullable = false)
    var lastTested: Timestamp? = null


    override fun toString(): String =
        "Entity of type: ${javaClass.name} ( " +
                "id = $id " +
                "ip = $ip " +
                "ports = $ports " +
                "protocols = $protocols " +
                "credentials = $credentials " +
                "countryData = $countryData " +
                "connectData = $connectData " +
                "riskData = $riskData " +
                "dateAdded = $dateAdded " +
                "lastTested = $lastTested " +
                ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ProxyEntity

        if (id != other.id) return false
        if (ip != other.ip) return false
        if (ports != other.ports) return false
        if (protocols != other.protocols) return false
        if (credentials != other.credentials) return false
        if (countryData != other.countryData) return false
        if (connectData != other.connectData) return false
        if (riskData != other.riskData) return false
        if (dateAdded != other.dateAdded) return false
        if (lastTested != other.lastTested) return false

        return true
    }
}