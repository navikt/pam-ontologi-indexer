package no.nav.arbeid.pam.ontologindexer.es


import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Datestamp {

    private const val DATESTAMP_FORMAT = "yyyyMMdd"

    val current: String
        get() = LocalDate.now().format(DateTimeFormatter.ofPattern(DATESTAMP_FORMAT))

    fun parseFrom(datestamp: String): LocalDate {
        return LocalDate.parse(datestamp, DateTimeFormatter.ofPattern(DATESTAMP_FORMAT))
    }

}
