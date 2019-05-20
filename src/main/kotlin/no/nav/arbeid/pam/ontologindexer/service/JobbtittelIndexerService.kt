package no.nav.arbeid.pam.ontologindexer.service

import no.nav.arbeid.pam.ontologindexer.config.EnvConf
import no.nav.arbeid.pam.ontologindexer.es.Datestamp
import no.nav.arbeid.pam.ontologindexer.es.IndexService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.io.IOException

@Component
class JobbtittelIndexerService {
    private val LOGGER = LoggerFactory.getLogger(JobbtittelIndexerService::class.java)

    @Autowired
    lateinit var hentOntologiKlient: HentOntologiKlient

    @Autowired
    lateinit var indexService: IndexService

    @Autowired
    lateinit var envConf:EnvConf

    fun indekser() {
        LOGGER.info("Henter stillingstitler fra ontologien")
        val stillingstitler = hentOntologiKlient.hentTitler()
        LOGGER.info("Hentet ${stillingstitler.size} fra ontologien")

        val prefix = envConf.stillingtittelEsPrefix ?: "xxx"
        val datestamp = Datestamp.current

        opprettIndeks(prefix, datestamp)


        indekser(prefix, datestamp, stillingstitler)

        LOGGER.info("!!! JOB FINISHED! Time to verify the results")

        Thread.sleep(2000)
        verifiser(prefix, datestamp, stillingstitler)


    }

    private fun verifiser(prefix: String, datestamp: String, stillingstitler: List<Stillingstittel>) {
        try {

            val docCount = indexService.fetchDocCount(prefix, datestamp)

            if (docCount >= stillingstitler.size) {
                LOGGER.info("Index doc count: {}", docCount)
                LOGGER.info("Verifying the new index and replacing the alias.")
                indexService.replaceAlias(prefix, datestamp)
            } else {
                LOGGER.error("Write count {} is greater than index doc count {}. Skipping verification, aliasing and deleting the new index.", stillingstitler.size, docCount)
                indexService.deleteIndexWithDatestamp(prefix, datestamp)
            }

        } catch (e: Exception) {
            LOGGER.error("Failed to verify job", e)
            throw e
        }
    }

    private fun indekser(prefix: String, datestamp: String, stillingstitler: List<Stillingstittel>) {
        try {
            indexService.indexJobTitles(prefix, datestamp, stillingstitler)
        } catch (e: Exception) {
            LOGGER.error("Failed to index indexJobTitles", e)
            throw e
        }
    }

    private fun opprettIndeks(prefix: String, datestamp: String) {
        try {
            indexService.createAndConfigure(prefix, datestamp)
        } catch (e: IOException) {
            LOGGER.error("Couldn't create and configure index. ", e)
            throw e
        }
    }

    fun deleteOldIndexes() {
        indexService.deleteOlderIndices(envConf.stillingtittelEsPrefix ?: "xxx")
    }


}
