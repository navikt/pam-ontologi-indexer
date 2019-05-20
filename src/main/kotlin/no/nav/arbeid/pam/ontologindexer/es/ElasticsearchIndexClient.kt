package no.nav.arbeid.pam.ontologindexer.es

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeid.pam.ontologindexer.service.Stillingstittel
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.apache.http.util.EntityUtils
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.Response
import org.elasticsearch.client.ResponseException
import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

/**
 * Elasticsearch client implementation.
 * <br></br><br></br>
 * Note that in cases where parameters are used as part of an index name, the value(s) are converted to lower case before being used.
 */
@ConditionalOnProperty(prefix = "elasticsearch", name = ["usemock"], havingValue = "false", matchIfMissing = true)
@Service
class ElasticsearchIndexClient @Autowired
constructor(client: RestClientBuilder,
            private val objectMapper: ObjectMapper) : RestHighLevelClient(client) {

    val isHealthy: Boolean
        @Throws(IOException::class)
        get() = super.ping()

    @Throws(IOException::class)
    fun createIndex(index: String, settings: String) {

        val lowerCaseIndex = index.toLowerCase()
        indices().create(CreateIndexRequest(lowerCaseIndex).source(settings, XContentType.JSON))

    }

    @Throws(IOException::class)
    fun deleteIndex(vararg indices: String) {

        val lowerCaseIndices = indices.map { it.toLowerCase() }.toTypedArray()

        if (lowerCaseIndices.size > 0) {
            indices().delete(DeleteIndexRequest(*lowerCaseIndices))
        }
    }

    @Throws(IOException::class)
    fun indexExists(index: String): Boolean {

        val lowerCaseIndex = index.toLowerCase()
        try {
            lowLevelClient.performRequest("GET", "/$lowerCaseIndex")
            return true
        } catch (e: ResponseException) {
            LOG.debug("Exception while calling indexExists" + e.message)
        }

        return false

    }

    @Throws(IOException::class)
    fun replaceAlias(alias: String, indexDatestamp: String) {

        val lowerCaseAlias = alias.toLowerCase()
        val jsonString = "{\n" +
                "    \"actions\" : [\n" +
                "        { \"remove\" : { \"index\" : \"*\", \"alias\" : \"" + lowerCaseAlias + "\" } },\n" +
                "        { \"add\" : { \"index\" : \"" + lowerCaseAlias + indexDatestamp + "\", \"alias\" : \"" + lowerCaseAlias + "\" } }\n" +
                "    ]\n" +
                "}"
        lowLevelClient.performRequest(
                "POST",
                "/_aliases",
                emptyMap(),
                NStringEntity(jsonString, ContentType.APPLICATION_JSON)
        )

    }

    @Throws(IOException::class)
    fun indexBulk(contents: List<Stillingstittel>, index: String): BulkResponse {

        val lowerCaseIndex = index.toLowerCase()
        val request = BulkRequest()

        for (content in contents) {
            request.add(IndexRequest(lowerCaseIndex, STILLINGSTITTEL_TYPE, content.konseptId.toString() + "-" + content.label)
                    .source(objectMapper.writeValueAsString(content), XContentType.JSON))
        }
        return bulk(request)

    }

    @Throws(IOException::class)
    fun fetchIndexDocCount(index: String): Int {

        val lowerCaseIndex = index.toLowerCase()
        lowLevelClient.performRequest("POST", "/$lowerCaseIndex/_refresh")
        val response = lowLevelClient.performRequest("GET", "/_cat/indices/$lowerCaseIndex")
        val line = EntityUtils.toString(response.entity)
        return Integer.parseInt(line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[6])

    }

    @Throws(IOException::class)
    fun fetchAllIndicesStartingWith(name: String): List<String> {

        val lowerCaseName = name.toLowerCase()
        val indices = ArrayList<String>()
        val response = lowLevelClient.performRequest("GET", "/_cat/indices/$lowerCaseName*")

        val full = EntityUtils.toString(response.entity)

        if (!(full == null || full.trim { it <= ' ' } == "")) {
            val lines = full.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (line in lines) {
                val tokenized = line.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                indices.add(tokenized[2])
            }
        }

        return indices
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(ElasticsearchIndexClient::class.java)
        private val STILLINGSTITTEL_TYPE = "stillingstittel"
    }

}
