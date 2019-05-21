package no.nav.arbeid.pam.ontologindexer

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import no.nav.arbeid.pam.ontologindexer.service.JobbtittelIndexerServiceTest
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
class WireMockConfig {

    private val LOGGER = LoggerFactory.getLogger(WireMockConfig::class.java)

    @Bean
    fun wireMockServer(): WireMockServer {
        LOGGER.info("Starter Wiremock")
        val wireMockServer = WireMockServer(wireMockConfig().notifier(ConsoleNotifier(true)).port(8189))
        wireMockServer.stubFor(JobbtittelIndexerServiceTest.mappingBuilder())
        wireMockServer.start()
        LOGGER.info("Wiremock startet")
        return wireMockServer
    }

}
