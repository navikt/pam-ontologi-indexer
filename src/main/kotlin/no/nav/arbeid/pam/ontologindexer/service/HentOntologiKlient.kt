package no.nav.arbeid.pam.ontologindexer.service

import no.nav.arbeid.pam.ontologindexer.config.EnvConf
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class HentOntologiKlient(templateBuilder: RestTemplateBuilder) {

    @Autowired
    lateinit var envConf: EnvConf

    private val restTemplate: RestTemplate = templateBuilder.build()

    fun fetchOccupationTitles(): List<Stillingstittel> {

        val entity:HttpEntity<String> = HttpEntity(envConf.gwHeaders())

        LOG.info("Hent ontologi med url/data request: ${envConf.typeaheadStillingstittlerGwUrl}")
        val response = restTemplate.exchange(
                "${envConf.typeaheadStillingstittlerGwUrl}",
                HttpMethod.GET,
                entity,
                object : ParameterizedTypeReference<List<Stillingstittel>>() {})
                .body ?: throw RuntimeException("Tomt resultat fra hent ontologi kall til stillinger/alle")

        LOG.info("Titler returnert: ${response.size}")
        return response
    }

    fun fetchSkills(): List<Skill> {
        val entity:HttpEntity<String> = HttpEntity(envConf.gwHeaders())

        LOG.info("Hent ontologi med url/data request: ${envConf.typeaheadSkillsGwUrl}")
        val response = restTemplate.exchange(
                "${envConf.typeaheadSkillsGwUrl}",
                HttpMethod.GET,
                entity,
                object : ParameterizedTypeReference<List<Skill>>() {})
                .body ?: throw RuntimeException("Tomt resultat fra hent ontologi kall til kompetanser/alle")

        LOG.info("Kompetanser returnert: ${response.size}")
        return response
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(HentOntologiKlient::class.java)
    }
}


abstract class JanzzConcept(
        open val konseptId: Int = 0,
        open val label: String = ""
)

data class Skill(
        override val konseptId: Int = 0,
        override val label: String = ""
): JanzzConcept()

data class Stillingstittel(
        override val konseptId: Int = 0,
        override val label: String = "",
        val styrk08: String = ""
): JanzzConcept()
