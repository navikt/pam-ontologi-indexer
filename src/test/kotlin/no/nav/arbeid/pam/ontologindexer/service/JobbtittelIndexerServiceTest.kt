package no.nav.arbeid.pam.ontologindexer.service

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import no.nav.arbeid.pam.ontologindexer.PamOntologiIndexerApplication
import no.nav.arbeid.pam.ontologindexer.PamOntologiIndexerApplicationTests
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasSize
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [PamOntologiIndexerApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobbtittelIndexerServiceTest {

    @Autowired
    lateinit var jobbtittelIndexerService: JobbtittelIndexerService

    @Test
    fun skalFaaResultat() {

        jobbtittelIndexerService.indekser()

    }

    companion object {
        fun mappingBuilder(): MappingBuilder {
            return WireMock.post(urlPathMatching("/pam-ontologi-synonymer/rest/typeahead/stilling/alle"))
                    .willReturn(WireMock.aResponse().withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(WiremockResponse.stillingstittelResponse))
        }


    }
}
