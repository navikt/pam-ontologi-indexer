package no.nav.arbeid.pam.ontologindexer.es

import no.nav.arbeid.pam.ontologindexer.service.Stillingstittel
import org.apache.commons.lang3.StringUtils
import org.elasticsearch.action.bulk.BulkResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.stream.Collectors

@Service
class IndexService(private val client: ElasticsearchIndexClient) {

    @Throws(IOException::class)
    fun createAndConfigure(prefix: String, datestamp: String) {

        val index = prefix + datestamp
        LOG.info("Creating and configuring index {}", index)
        if (!client.indexExists(index)) {
            client.createIndex(index, settingsFromClasspath)
            LOG.info("Index {} was successfully created with settings and mappings", index)
        }

    }

    @Throws(IOException::class)
    fun replaceAlias(prefix: String, datestamp: String) {

        val index = prefix + datestamp
        if (client.indexExists(index)) {
            client.replaceAlias(prefix, datestamp)
            LOG.info("Successfully replaced aliases. Index {} is now aliased to {}", index, prefix)
        } else {
            LOG.error("Failed to replace the alias. New index {} doesn't exist", index)
        }

    }

    @Throws(IOException::class)
    fun fetchDocCount(prefix: String, datestamp: String): Int {
        return client.fetchIndexDocCount(indexOf(prefix, datestamp))
    }

    @Throws(IOException::class)
    fun indexJobTitles(prefix: String, datestamp: String, list: List<Stillingstittel>) {

        val index = prefix + datestamp
        if (!list.isEmpty()) {
            val bulkResponse = client.indexBulk(list, index)
            reportBulkResponse(bulkResponse, index)
        }

    }

    private fun reportBulkResponse(bulkResponse: BulkResponse, index: String) {

        var failed = 0
        var success = 0
        for (item in bulkResponse.items) {
            if (item.isFailed) {
                // TODO implement failed handling later
                LOG.error("Failed item: {}, index: {}", item.failureMessage, index)
                failed++
            } else {
                LOG.info("Indexed item: {}, index: {}", item.id, index)
                success++
            }
        }
        LOG.info("Indexed {} successfully and {} failed, index: {}", success, failed, index)

    }

    @Throws(IOException::class)
    fun deleteIndexWithDatestamp(prefix: String, datestamp: String) {
        client.deleteIndex(prefix + datestamp)
    }

    @Throws(IOException::class)
    fun deleteOlderIndices(prefix: String) {

        val prefixLowercased = prefix.toLowerCase()
        val maxAge = LocalDate.now().minusDays(INDEX_EXPIRATION_IN_DAYS.toLong())
        client.deleteIndex(
                *client
                        .fetchAllIndicesStartingWith(prefixLowercased)
                        .filter { index -> indexIsBefore(index, prefixLowercased, maxAge) }
                        .toTypedArray()
        )

    }

    private fun indexIsBefore(index: String, prefix: String, date: LocalDate): Boolean {

        return try {
            Datestamp.parseFrom(StringUtils.remove(index, prefix)).isBefore(date)
        } catch (e: DateTimeParseException) {
            LOG.error("Couldn't parse date from index name {}", index)
            false
        }

    }

    companion object {

        private val LOG = LoggerFactory.getLogger(IndexService::class.java)
        private const val CLASSPATH_SETTINGS = "/ESStillingstitlerSetting.json"
        private const val INDEX_EXPIRATION_IN_DAYS = 10

        private fun indexOf(prefix: String, datestamp: String): String {
            return prefix + datestamp
        }

        private val settingsFromClasspath: String
            @Throws(IOException::class)
            get() = InputStreamReader(IndexService::class.java.getResourceAsStream(CLASSPATH_SETTINGS), StandardCharsets.UTF_8).use { reader ->
                return BufferedReader(reader)
                        .lines().collect(Collectors.joining("\n"))

            }
    }

}
