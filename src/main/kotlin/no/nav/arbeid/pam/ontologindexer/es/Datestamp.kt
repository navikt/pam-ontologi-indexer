package no.nav.arbeid.pam.ontologindexer.es


import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Datestamp {

    private val DATESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")

    val current: String
        get() = LocalDate.now().format(DATESTAMP_FORMATTER)

    fun parseFrom(datestamp: String): LocalDate {
        return LocalDate.parse(datestamp, DATESTAMP_FORMATTER)
    }

    fun format(d: LocalDate) = d.format(DATESTAMP_FORMATTER)
}
