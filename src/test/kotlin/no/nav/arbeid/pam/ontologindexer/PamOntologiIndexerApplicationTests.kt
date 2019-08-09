package no.nav.arbeid.pam.ontologindexer

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@TestPropertySource(properties = [
    "PAM_ES_URL=http://localhost:9200",
    "PAM_SYNONYMER_TYPEAHEAD_X_NAV_APIKEY=testkey",
    "PAM_SYNONYMER_TYPEAHEAD_URL=http://localhost:8189/pam-ontologi-synonymer/rest/typeahead/stilling/alle"])
class PamOntologiIndexerApplicationTests {

    @Test
    fun prodContextLoads() {
    }

}
