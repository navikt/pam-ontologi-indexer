package no.nav.arbeid.pam.ontologindexer.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.core.ParameterizedTypeReference



@Service
class HentOntologiKlient(templateBuilder: RestTemplateBuilder) {


    @Value("\${ontologi.url:http://localhost:8189/pam-ontologi-synonymer/rest/typeahead/stilling/alle}")
    lateinit var stillingstitlerUrl: String


    private val restTemplate: RestTemplate

    init {
        restTemplate = templateBuilder
                .build()
    }

    fun hentTitler(): List<Stillingstitler> {
        LOG.info("Hent ontologi med url/data request: $stillingstitlerUrl")
        val response = restTemplate.exchange(stillingstitlerUrl, HttpMethod.GET, null, object: ParameterizedTypeReference<List<Stillingstitler>>(){}).body
        LOG.info("response: $response")
        return response ?: throw RuntimeException("Tomt resultat fra hent ontologi kall")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(HentOntologiKlient::class.java)
    }
}

data class Stillingstitler (
        val konseptId: Int = 0,
        val label: String = "",
        val styrk08: String = ""
)
