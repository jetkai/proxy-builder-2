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
    @Column(name = "port", nullable = false)
    var ports: Int? = null

    @Basic
    @Column(name = "protocols", nullable = false)
    var protocols : String? = null

    @Basic
    @Column(name = "credentials", nullable = false)
    var credentials : String? = null

    @Basic
    @Column(name = "location", nullable = true)
    var location: String? = null

    @Basic
    @Column(name = "connections", nullable = false)
    var connections: String? = null

    @Basic
    @Column(name = "detection", nullable = true)
    var detection: String? = null

    @Basic
    @Column(name = "provider", nullable = true)
    var provider : String? = null

    @Basic
    @Column(name = "date_added", nullable = false)
    var dateAdded: Timestamp? = null

    @Basic
    @Column(name = "last_tested", nullable = false)
    var lastTested: Timestamp? = null

}