package no.nav.arbeid.pam.ontologindexer.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EnvConf {

    @Value("\${pam.stillingstitler.url}")
    lateinit var stillingstitlerUrl: String

    @Value("\${pam.es.ontologi.stillingtittel.prefix}")
    val stillingtittelEsPrefix: String? = null

    @Value("\${pam.es.url}")
    val elasticsearchUrl: String? = null

}
