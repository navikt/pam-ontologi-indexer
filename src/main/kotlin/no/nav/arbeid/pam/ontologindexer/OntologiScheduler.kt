package no.nav.arbeid.pam.ontologindexer

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class OntologiScheduler {
    private val LOGGER = LoggerFactory.getLogger(OntologiScheduler::class.java)

    @Scheduled(fixedRate = 5000)
    fun leggInnStillingstitler() {
        LOGGER.info("leggInnStillingstitler startet")
    }
}
