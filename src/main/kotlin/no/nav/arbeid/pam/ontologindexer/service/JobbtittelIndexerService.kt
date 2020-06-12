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
    lateinit var envConf: EnvConf

    fun indexJanzzConcepts() {
        LOGGER.info("Henter stillingstitler fra ontologien")
        val occupationTitles = hentOntologiKlient.fetchOccupationTitles()
        val skills = hentOntologiKlient.fetchSkills()
        LOGGER.info("Hentet ${occupationTitles.size} stillingstitler og ${skills.size} kompetanser fra ontologien")

        val occupationIndexPrefix = envConf.stillingtittelEsPrefix ?: "defaultOccupationIndex-"
        val skillIndexPrefix = envConf.skillEsPrefix ?: "defaultSkillIndex-"
        val datestamp = Datestamp.current

        opprettIndeks(occupationIndexPrefix, datestamp)
        opprettIndeks(skillIndexPrefix, datestamp)

        index(occupationIndexPrefix, datestamp, occupationTitles)
        index(skillIndexPrefix, datestamp, skills)

        LOGGER.info("!!! JOB FINISHED! Time to verify the results")

        Thread.sleep(2000)
        verifyIndex(occupationIndexPrefix, datestamp, occupationTitles)
        verifyIndex(skillIndexPrefix, datestamp, skills)

    }

    private fun verifyIndex(prefix: String, datestamp: String, concepts: List<JanzzConcept>) {
        try {

            val docCount = indexService.fetchDocCount(prefix, datestamp)

            if (docCount >= concepts.size) {
                LOGGER.info("Index doc count: {}", docCount)
                LOGGER.info("Verifying the new index and replacing the alias.")
                indexService.replaceAlias(prefix, datestamp)
            } else {
                LOGGER.error("Write count {} is greater than index doc count {}. Skipping verification, aliasing and deleting the new index.", concepts.size, docCount)
                indexService.deleteIndexWithDatestamp(prefix, datestamp)
            }

        } catch (e: Exception) {
            LOGGER.error("Failed to verify job", e)
            throw e
        }
    }

    private fun index(prefix: String, datestamp: String, concepts: List<JanzzConcept>) {
        try {
            indexService.indexJanzzConcept(prefix, datestamp, concepts)
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
        val prefix = envConf.stillingtittelEsPrefix!!
        LOGGER.info("Will delete old indices prefixed with '${prefix}' ..")
        indexService.deleteOlderIndices(prefix)
    }


}
