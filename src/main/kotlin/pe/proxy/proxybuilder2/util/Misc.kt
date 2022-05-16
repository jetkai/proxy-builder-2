package pe.proxy.proxybuilder2.util

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

/**
 * Misc
 *
 * Miscellaneous functions used for date/time/math etc
 *
 * @author Kai
 * @version 1.0, 19/04/2022
 */
object Misc {

    fun getDateAsTimestamp(): Timestamp {
        return Timestamp(Date().time)
    }

    fun getLocalDateAsTimestamp() : Timestamp {
        val date = LocalDateTime.now()
        return Timestamp.valueOf(date)
    }

    fun getLocalDateAsTimestamp(minusMinutes: Long): Timestamp {
        val date = LocalDateTime.now().minusMinutes(minusMinutes)
        return Timestamp.valueOf(date)
    }

}