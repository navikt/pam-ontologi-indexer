package no.nav.arbeid.pam.ontologindexer.service

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import no.nav.arbeid.pam.ontologindexer.PamOntologiIndexerApplication
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [PamOntologiIndexerApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobbtittelIndexerServiceTest {

    @Autowired
    lateinit var jobbtittelIndexerService: JobbtittelIndexerService

    @Test
    @Ignore("Krever oppstart av pam-elasticsearch docker container")
    fun skalFaaResultat() {
        jobbtittelIndexerService.indekser()
    }

    companion object {
        fun mappingBuilder(): MappingBuilder {
            return WireMock.get(urlPathMatching("/pam-ontologi-synonymer/rest/typeahead/stilling/alle"))
                    .willReturn(WireMock.aResponse().withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(WiremockResponse.stillingstittelResponse))
        }


    }
}
