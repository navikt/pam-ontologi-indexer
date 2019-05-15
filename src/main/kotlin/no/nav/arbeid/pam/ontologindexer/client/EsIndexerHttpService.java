package no.nav.arbeid.pam.ontologindexer.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.elasticsearch.mapping.MappingBuilder;
import no.nav.elasticsearch.mapping.MappingBuilderImpl;
import no.nav.elasticsearch.mapping.ObjectMapping;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EsIndexerHttpService implements EsIndexerService {


    private static final Logger LOGGER = LoggerFactory.getLogger(EsIndexerHttpService.class);

    private final RestHighLevelClient client;
    private final ObjectMapper mapper;

    public EsIndexerHttpService(RestHighLevelClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.mapper = objectMapper;
    }

    @Override
    public void createIndex(Class clazz) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(clazz.getSimpleName());

        MappingBuilder mappingBuilder = new MappingBuilderImpl();
        ObjectMapping mapping = mappingBuilder.build(clazz);

        // String jsonMapping = mapping.getContentAsString();
        XContentBuilder contentBuilder = mapping.getContent();
        String jsonMapping = contentBuilder.string();
        LOGGER.debug("MAPPING: " + jsonMapping);
        createIndexRequest.mapping(clazz.getSimpleName(), jsonMapping, XContentType.JSON);

        CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest);
        LOGGER.debug("CREATEINDEXRESPONSE: " + createIndexResponse);
    }

    @Override
    public void deleteIndex(Class clazz) throws IOException {
        DeleteIndexRequest deleteRequest = new DeleteIndexRequest(clazz.getSimpleName());
        DeleteIndexResponse deleteIndexResponse = client.indices().delete(deleteRequest);
        LOGGER.debug("DELETERESPONSE: " + deleteIndexResponse.toString());
    }

    @Override
    public void index(ESObject esObject, Class clazz) throws IOException {
        //String jsonString = mapper.writeValueAsString(json);
        LOGGER.debug("DOKUMENTET: " + esObject.asJson());

        IndexRequest request =
                new IndexRequest(clazz.getSimpleName(), clazz.getSimpleName(), esObject.getId());
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        request.source(esObject.asJson(), XContentType.JSON);
        IndexResponse indexResponse = esExec(() -> client.index(request), clazz);
        LOGGER.debug("INDEXRESPONSE: " + indexResponse.toString());
    }

    @Override
    public int bulkIndex(List<ESObject> esObjects, Class clazz) throws IOException {
        BulkRequest bulkRequest = Requests.bulkRequest();
        Long currentArenaId = 0l;
        try {
            for (ESObject esObject : esObjects) { ;
                IndexRequest ir = Requests.indexRequest().source(esObject.asJson(), XContentType.JSON)
                        .index(clazz.getSimpleName()).type(clazz.getSimpleName()).id(esObject.getId());

                bulkRequest.add(ir);
            }
        } catch (Exception e) {
            LOGGER.info(
                    "Greide ikke å serialisere objekt til JSON for å bygge opp bulk-indekseringsrequest: {}. ArenaId: {}",
                    e.getMessage(), currentArenaId, e);
            throw new RuntimeException(
                    "Greide ikke å serialisere CV til JSON for å bygge opp bulk-indekseringsrequest. ArenaId: "
                            + currentArenaId,
                    e);
        }

        LOGGER.info("Sender bulk indexrequest med {} cv'er", esObjects.size());
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        BulkResponse bulkResponse = esExec(() -> client.bulk(bulkRequest), clazz);
        int antallIndeksert = esObjects.size();

        if (bulkResponse.hasFailures()) {
            long antallFeil = 0;
            for (BulkItemResponse bir : bulkResponse.getItems()) {
                antallFeil += bir.isFailed() ? 1 : 0;
                try {
                    if (bir.getFailure() != null) {
                        Optional<ESObject> cvMedFeil = esObjects.stream().filter(
                                esObject -> ("" + esObject.getId()).trim().equals(bir.getFailure().getId()))
                                .findFirst();

                        LOGGER.warn("Feilet ved indeksering av CV {}: " + bir.getFailure().getMessage(),
                                cvMedFeil.isPresent() && LOGGER.isTraceEnabled() ? mapper.writeValueAsString(cvMedFeil.get()) : "",
                                bir.getFailure().getCause());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Feilet ved parsing av bulkitemresponse..", e);
                    //meterRegistry.counter("cv.es.index.feil", Tags.of("type", "infrastruktur")).increment();
                }
                //meterRegistry.counter("cv.es.index.feil", Tags.of("type", "applikasjon")).increment(antallFeil);
            }
            antallIndeksert -= antallFeil;
            LOGGER.warn(
                    "Feilet under indeksering av CVer: " + bulkResponse.buildFailureMessage());
        }
        LOGGER.debug("BULKINDEX tidsbruk: " + bulkResponse.getTook());
        return antallIndeksert;
    }

    @Override
    public boolean doesIndexExist(Class clazz) throws IOException {
        try {
            Response restResponse =
                    client.getLowLevelClient().performRequest("HEAD", "/" + clazz.getSimpleName());
            return restResponse.getStatusLine().getStatusCode() == 200;
        } catch (ResponseException e) {
            LOGGER.info("Exception while calling isExistingIndex", e);
        }
        return false;
    }

    /**
     * Pakk inn kall mot elastic search i sjekk på om index finnes. Hvis index ikke finnes så
     * opprettes den, og kalles forsøkes på nytt
     *
     * @param <T>
     * @param fun
     * @param clazz
     * @return
     * @throws IOException
     */
    private <T> T esExec(IOSupplier<T> fun, Class clazz) throws IOException {
        try {
            return fun.get();
        } catch (ElasticsearchStatusException e) {
            if (e.status().getStatus() == 404
                    && e.getMessage().contains("index_not_found_exception")) {
                LOGGER.info(
                        "Greide ikke å utfore operasjon mot elastic search. Prøver å opprette index og forsøke på nytt.");
                createIndex(clazz);
                return fun.get();
            }
            throw (e);
        }
    }

    /** Tilsvarer java.functions.Supplier bare at get metoden kan kaste IOException */
    private interface IOSupplier<T> {
        T get() throws IOException;
    }

    @Override
    public long antallIndeksert(Class clazz) {
        long antallIndeksert = 0;
        try {
            Response response = client.getLowLevelClient().performRequest("GET",
                    String.format("/%s/%s/_count", clazz.getSimpleName(), clazz.getSimpleName()));
            if (response != null && response.getStatusLine().getStatusCode() >= 200
                    && response.getStatusLine().getStatusCode() < 300) {
                String json = EntityUtils.toString(response.getEntity());
                JsonNode countNode = mapper.readTree(json).path(("count"));
                antallIndeksert = countNode != null ? countNode.asLong() : 0;
            } else {
                LOGGER.warn("Greide ikke å hente ut antall dokumenter det i ES indeksen: {} : {}",
                        response.getStatusLine().getStatusCode(),
                        response.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            LOGGER.warn("Greide ikke å hente ut antall dokumenter i ES indeksen: {}",
                    e.getMessage(), e);
        }
        return antallIndeksert;
    }
}
