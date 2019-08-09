package no.nav.arbeid.pam.ontologindexer

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import no.nav.arbeid.pam.ontologindexer.config.EnvConf
import no.nav.arbeid.pam.ontologindexer.es.Datestamp
import no.nav.arbeid.pam.ontologindexer.es.ElasticsearchIndexClient
import no.nav.arbeid.pam.ontologindexer.es.IndexService
import no.nav.arbeid.pam.ontologindexer.service.JobbtittelIndexerService
import no.nav.arbeid.pam.ontologindexer.service.WiremockResponse
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [PamOntologiIndexerApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ITest {

    @Autowired
    lateinit var jobbtittelIndexerService: JobbtittelIndexerService

    @Autowired
    lateinit var indexService: IndexService

    @Autowired
    lateinit var elasticSearchClient: ElasticsearchIndexClient

    @Autowired
    lateinit var envConf: EnvConf

    @Test
    @Ignore("Krever lokal ElasticSearch-instans kjørende på http://localhost:9200")
    fun indeksereUtenFeil() {
        jobbtittelIndexerService.indekser()
    }

    // TODO should be easy to mock ElasticsearchIndexClient client for this test to work without real ES
    @Test
    @Ignore("Krever lokal ElasticSearch-instans kjørende på http://localhost:9200")
    fun sletteGamleIndekser() {
        val prefix = envConf.stillingtittelEsPrefix!!
        val oldStamp = "20180101"
        val yesterdayStamp = Datestamp.format(LocalDate.now().minusDays(1))
        val currentStamp = Datestamp.current

        indexService.createAndConfigure(prefix, oldStamp)
        indexService.createAndConfigure(prefix, yesterdayStamp)
        indexService.createAndConfigure(prefix, currentStamp)
        assertTrue(elasticSearchClient.indexExists(prefix + oldStamp))
        assertTrue(elasticSearchClient.indexExists(prefix + yesterdayStamp))
        assertTrue(elasticSearchClient.indexExists(prefix + currentStamp))

        jobbtittelIndexerService.deleteOldIndexes()

        assertFalse(elasticSearchClient.indexExists(prefix + oldStamp))
        assertTrue(elasticSearchClient.indexExists(prefix + yesterdayStamp))
        assertTrue(elasticSearchClient.indexExists(prefix + currentStamp))
    }

    companion object {
        fun mappingBuilder(): MappingBuilder {
            return WireMock.get(urlPathMatching("/pam-ontologi-synonymer/rest/typeahead/stilling/alle"))
                    .withHeader("x-nav-apiKey", equalTo("testkey"))
                    .willReturn(WireMock.aResponse().withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(WiremockResponse.stillingstittelResponse))
        }
    }
}
