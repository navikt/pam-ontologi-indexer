package no.nav.arbeid.pam.ontologindexer.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.arbeid.pam.ontologindexer.service.Stillingstittel;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Elasticsearch client implementation.
 * <br/><br>
 * Note that in cases where parameters are used as part of an index name, the value(s) are converted to lower case before being used.
 */
@ConditionalOnProperty(prefix = "elasticsearch", name = "usemock", havingValue = "false", matchIfMissing = true)
@Service
public class ElasticsearchIndexClient extends RestHighLevelClient {

    private final static Logger LOG = LoggerFactory.getLogger(ElasticsearchIndexClient.class);
    private final static String STILLINGSTITTEL_TYPE = "stillingstittel";

    private final ObjectMapper objectMapper;

    @Autowired
    public ElasticsearchIndexClient(RestClientBuilder client,
                                    ObjectMapper objectMapper) {
        super(client);
        this.objectMapper = objectMapper;
    }

    public void createIndex(String index, String settings)
            throws IOException {

        String lowerCaseIndex = index.toLowerCase();
        indices().create(new CreateIndexRequest(lowerCaseIndex).source(settings, XContentType.JSON));

    }

    public void deleteIndex(String... indices)
            throws IOException {

        String[] lowerCaseIndices = Arrays.stream(indices).map(String::toLowerCase).toArray(String[]::new);

        if (lowerCaseIndices.length > 0) {
            indices().delete(new DeleteIndexRequest(lowerCaseIndices));
        }
    }

    public boolean indexExists(String index)
            throws IOException {

        String lowerCaseIndex = index.toLowerCase();
        try {
            getLowLevelClient().performRequest("GET", "/" + lowerCaseIndex);
            return true;
        } catch (ResponseException e) {
            LOG.debug("Exception while calling indexExists" + e.getMessage());
        }
        return false;

    }

    public void replaceAlias(String alias, String indexDatestamp)
            throws IOException {

        String lowerCaseAlias = alias.toLowerCase();
        String jsonString = "{\n" +
                "    \"actions\" : [\n" +
                "        { \"remove\" : { \"index\" : \"*\", \"alias\" : \"" + lowerCaseAlias + "\" } },\n" +
                "        { \"add\" : { \"index\" : \"" + lowerCaseAlias + indexDatestamp + "\", \"alias\" : \"" + lowerCaseAlias + "\" } }\n" +
                "    ]\n" +
                "}";
        getLowLevelClient().performRequest(
                "POST",
                "/_aliases",
                Collections.emptyMap(),
                new NStringEntity(jsonString, ContentType.APPLICATION_JSON)
        );

    }

    public BulkResponse indexBulk(List<Stillingstittel> contents, String index)
            throws IOException {

        String lowerCaseIndex = index.toLowerCase();
        BulkRequest request = new BulkRequest();

        for (Stillingstittel content : contents) {
            request.add(new IndexRequest(lowerCaseIndex, STILLINGSTITTEL_TYPE, String.valueOf(content.getKonseptId()))
                    .source(objectMapper.writeValueAsString(content), XContentType.JSON));
        }
        return bulk(request);

    }

    public int fetchIndexDocCount(String index)
            throws IOException {

        String lowerCaseIndex = index.toLowerCase();
        getLowLevelClient().performRequest("POST", "/" + lowerCaseIndex + "/_refresh");
        Response response = getLowLevelClient().performRequest("GET", "/_cat/indices/" + lowerCaseIndex);
        String line = EntityUtils.toString(response.getEntity());
        return Integer.parseInt(line.split(" ")[6]);

    }

    public List<String> fetchAllIndicesStartingWith(String name)
            throws IOException {

        String lowerCaseName = name.toLowerCase();
        List<String> indices = new ArrayList<>();
        Response response = getLowLevelClient().performRequest("GET", "/_cat/indices/" + lowerCaseName + "*");

        String full = EntityUtils.toString(response.getEntity());

        if (!(full == null || full.trim().equals(""))) {
            String[] lines = full.split("\\r?\\n");

            for (String line : lines) {
                String[] tokenized = line.split("\\s");
                indices.add(tokenized[2]);
            }
        }

        return indices;
    }

    public boolean isHealthy()
            throws IOException {
        return super.ping();
    }

}
