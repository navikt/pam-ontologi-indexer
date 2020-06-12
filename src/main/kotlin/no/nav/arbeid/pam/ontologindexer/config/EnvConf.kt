package no.nav.arbeid.pam.ontologindexer.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class EnvConf {

    @Value("\${pam.es.ontologi.stillingtittel.prefix}")
    val stillingtittelEsPrefix: String? = null

    @Value("\${pam.es.ontologi.skill.prefix}")
    val skillEsPrefix: String? = null

    @Value("\${pam.es.url}")
    val elasticsearchUrl: String? = null

    @Value("\${pam.ad.gateway.apikey}")
    val synonymerGwKey: String? = null

    @Value("\${pam.synonymer.typeahead.headername}")
    val synonymerGwHeaderName: String? = null

    @Value("\${pam.synonymer.typeahead.stillingstitler.url}")
    val typeaheadStillingstittlerGwUrl: String? = null

    @Value("\${pam.synonymer.typeahead.skills.url}")
    val typeaheadSkillsGwUrl: String? = null

    fun gwHeaders(): HttpHeaders {
        // connect to servicegateway
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON_UTF8
        headers.accept = listOf(MediaType.APPLICATION_JSON_UTF8)
        if (synonymerGwHeaderName != null) headers.add(synonymerGwHeaderName!!, synonymerGwKey)
        return headers
    }

}
