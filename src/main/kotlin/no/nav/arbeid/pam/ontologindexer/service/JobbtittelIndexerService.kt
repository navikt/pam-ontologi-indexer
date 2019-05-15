package no.nav.arbeid.pam.ontologindexer.service

import ch.qos.logback.classic.LoggerContext
import no.nav.arbeid.pam.ontologindexer.OntologiScheduler
import no.nav.arbeid.pam.ontologindexer.client.ESObject
import no.nav.arbeid.pam.ontologindexer.client.EsIndexerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

@Component
class JobbtittelIndexerService {
    private val LOGGER = LoggerFactory.getLogger(JobbtittelIndexerService::class.java)

    @Autowired
    lateinit var hentOntologiKlient: HentOntologiKlient

    fun indekser() {
        LOGGER.info("Henter stillingstitler fra ontologien")
        hentOntologiKlient.hentTitler()

    }


}
