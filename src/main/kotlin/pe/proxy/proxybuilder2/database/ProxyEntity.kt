package pe.proxy.proxybuilder2.database

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import java.sql.Timestamp
import javax.persistence.*

/**
 * ProxyEntity
 *
 * Serializer/Deserializer placeholder
 * Reserve Name Ref: https://dev.mysql.com/doc/refman/8.0/en/keywords.html#keywords-8-0-detailed-I
 * (Future revision use OneToOne https://www.baeldung.com/jpa-one-to-one & deprecate json)
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
@Entity
@Table(name = "all_proxies", schema = "prod")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ProxyEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @JsonIgnore
    var id: Int? = null

    @Basic
    @Column(name = "ip", nullable = false)
    var ip: String? = null

    @Basic
    @Column(name = "port", nullable = false)
    var port : Int? = null

    // @Enumerated(EnumType.STRING)
    // @JoinColumn(name = "protocols")
    @Basic
    @Column(name = "protocols", nullable = true)
    var protocols : String ?=null

    @Basic
    @Column(name = "credentials", nullable = true)
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

    @Basic
    @Column(name = "last_success", nullable = true)
    var lastSuccess: Timestamp? = null

}