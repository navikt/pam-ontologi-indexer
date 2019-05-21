package no.nav.arbeid.pam.ontologindexer

import no.nav.arbeid.pam.ontologindexer.service.JobbtittelIndexerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException


@Component
class OntologiScheduler {
    private val LOGGER = LoggerFactory.getLogger(OntologiScheduler::class.java)

    @Autowired
    lateinit var jobbtittelIndexerService: JobbtittelIndexerService

    @Scheduled(cron = CRON_INDEXER)
    fun leggInnStillingstitler() {
        LOGGER.info("leggInnStillingstitler scheduled job startet cron:$CRON_INDEXER")
        jobbtittelIndexerService.indekser()
        LOGGER.info("leggInnStillingstitler scheduled job avsluttet")
    }

    @Scheduled(cron = CRON_CLEANUP)
    fun cleanup() {

        try {
            LOGGER.info("Deleting older indices cron:$CRON_CLEANUP")
            jobbtittelIndexerService.deleteOldIndexes()
            LOGGER.info("Deleted older indices")
        } catch (e: IOException) {
            LOGGER.error("Failed to delete older indices", e)
        }

    }

    companion object {
        const val CRON_INDEXER = "0 0 1 * * *"
        const val CRON_CLEANUP = "0 0 2 * * *"
    }
}