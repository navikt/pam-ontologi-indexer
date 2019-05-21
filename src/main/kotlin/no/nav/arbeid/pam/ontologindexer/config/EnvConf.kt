package no.nav.arbeid.pam.ontologindexer.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EnvConf {

    @Value("\${pam.stillingstitler.url:http://localhost:8189/pam-ontologi-synonymer/rest/typeahead/stilling/alle}")
    lateinit var stillingstitlerUrl: String

    @Value("\${pam.es.ontologi.stillingtittel.prefix:ontologi-stillingstittel-}")
    val stillingtittelEsPrefix: String? = null

    @Value("\${pam.es.url:http://localhost:9210}")
    val elasticsearchUrl: String? = null

    @Value("\${pam.http.proxy.url:#{null}}")
    val httpProxyUrl: String? = null

}
