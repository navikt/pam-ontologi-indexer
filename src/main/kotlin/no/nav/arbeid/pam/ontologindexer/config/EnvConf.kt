package no.nav.arbeid.pam.ontologindexer.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EnvConf {

    @Value("\${ontologi.url:http://localhost:8189/pam-ontologi-synonymer/rest/typeahead/stilling/alle}")
    lateinit var stillingstitlerUrl: String

    @Value("\${es.ontologi.stillingtittel.prefix:ontologi-stillingstittel-}")
    val stillingtittelEsPrefix: String? = null

    @Value("\${es.url:http://localhost:9200}")
    private val elasticsearchUrl: String? = null

}
