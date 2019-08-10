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
import org.elasticsearch.client.*
import org.elasticsearch.common.xcontent.XContentType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.stream.Collectors.toList

/**
 * Elasticsearch client implementation.
 * <br></br><br></br>
 * Note that in cases where parameters are used as part of an index name, the value(s) are converted to lower case before being used.
 */
@Service
class ElasticsearchIndexClient @Autowired
constructor(elasticClientBuilder: RestClientBuilder,
            private val objectMapper: ObjectMapper) : RestHighLevelClient(elasticClientBuilder) {

    val isHealthy: Boolean
        @Throws(IOException::class)
        get() = super.ping(RequestOptions.DEFAULT)

    @Throws(IOException::class)
    fun createIndex(index: String, settings: String) {

        val lowerCaseIndex = index.toLowerCase()
        indices().create(CreateIndexRequest(lowerCaseIndex).source(settings, XContentType.JSON), RequestOptions.DEFAULT)

    }

    @Throws(IOException::class)
    fun deleteIndex(vararg indices: String) {

        val lowerCaseIndices = indices.map { it.toLowerCase() }.toTypedArray()

        if (lowerCaseIndices.isNotEmpty()) {
            indices().delete(DeleteIndexRequest(*lowerCaseIndices), RequestOptions.DEFAULT)
        }
    }

    @Throws(IOException::class)
    fun indexExists(index: String): Boolean {

        val lowerCaseIndex = index.toLowerCase()
        try {
            lowLevelClient.performRequest(Request("GET", "/$lowerCaseIndex"))
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

        lowLevelClient.performRequest(Request("POST", "/_aliases").apply {
            this.entity = NStringEntity(jsonString, ContentType.APPLICATION_JSON)
        })

    }

    @Throws(IOException::class)
    fun indexBulk(contents: List<Stillingstittel>, index: String): BulkResponse {

        return bulk(
                BulkRequest().apply {
                    contents.forEach {
                        this.add(IndexRequest(index.toLowerCase(), STILLINGSTITTEL_TYPE, it.konseptId.toString() + "-" + it.label)
                                .source(objectMapper.writeValueAsString(it), XContentType.JSON))
                    }
                }
        )

    }

    @Throws(IOException::class)
    fun fetchIndexDocCount(index: String): Int {

        val lowerCaseIndex = index.toLowerCase()
        lowLevelClient.performRequest(Request("POST", "/$lowerCaseIndex/_refresh"))
        val response = lowLevelClient.performRequest(Request("GET", "/_cat/indices/$lowerCaseIndex"))
        val line = EntityUtils.toString(response.entity)
        return Integer.parseInt(line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[6])

    }

    @Throws(IOException::class)
    fun fetchAllIndicesStartingWith(name: String): List<String> {

        val lowerCaseName = name.toLowerCase()
        val response = lowLevelClient.performRequest(Request("GET", "/_cat/indices/$lowerCaseName*"))

        return response.entity.content.bufferedReader().lines().filter {
            !it.trim().isEmpty()
        }.map {
            it.split("\\s+".toRegex())[2]
        }.filter {
            it.startsWith(lowerCaseName) // Extra sanity check
        }.collect(toList())
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(ElasticsearchIndexClient::class.java)
        private const val STILLINGSTITTEL_TYPE = "stillingstittel"
    }

}
