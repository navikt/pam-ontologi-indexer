package no.nav.arbeid.pam.ontologindexer.service

import no.nav.arbeid.pam.ontologindexer.config.EnvConf
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class HentOntologiKlient(templateBuilder: RestTemplateBuilder) {

    @Autowired
    lateinit var envConf: EnvConf

    private val restTemplate: RestTemplate = templateBuilder.build()

    fun hentTitler(): List<Stillingstittel> {
        LOG.info("Hent ontologi med url/data request: ${envConf.stillingstitlerUrl}")
        val response = restTemplate.exchange(envConf.stillingstitlerUrl, HttpMethod.GET, null, object : ParameterizedTypeReference<List<Stillingstittel>>() {}).body
        LOG.info("response: $response")
        return response ?: throw RuntimeException("Tomt resultat fra hent ontologi kall")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(HentOntologiKlient::class.java)
    }
}

data class Stillingstittel(
        val konseptId: Int = 0,
        val label: String = "",
        val styrk08: String = ""
)
