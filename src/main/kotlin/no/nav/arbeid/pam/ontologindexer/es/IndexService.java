package no.nav.arbeid.pam.ontologindexer.es;

import no.nav.arbeid.pam.ontologindexer.service.Stillingstittel;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IndexService {

    private static final Logger LOG = LoggerFactory.getLogger(IndexService.class);
    private static final String CLASSPATH_SETTINGS = "/ESStillingstitlerSetting.json";
    private static final int INDEX_EXPIRATION_IN_DAYS = 10;

    private final ElasticsearchIndexClient client;

    public IndexService(ElasticsearchIndexClient client) {
        this.client = client;
    }

    private static String indexOf(String prefix, String datestamp) {
        return (prefix + datestamp);
    }

    private static String getSettingsFromClasspath()
            throws IOException {
        try (InputStreamReader reader = new InputStreamReader(IndexService.class.getResourceAsStream(CLASSPATH_SETTINGS), StandardCharsets.UTF_8)) {
            return new BufferedReader(reader)
                    .lines().collect(Collectors.joining("\n"));

        }
    }

    public void createAndConfigure(String prefix, String datestamp)
            throws IOException {

        String index = prefix + datestamp;
        LOG.info("Creating and configuring index {}", index);
        if (!client.indexExists(index)) {
            client.createIndex(index, getSettingsFromClasspath());
            LOG.info("Index {} was successfully created with settings and mappings", index);
        }

    }

    public void replaceAlias(String prefix, String datestamp)
            throws IOException {

        String index = prefix + datestamp;
        if (client.indexExists(index)) {
            client.replaceAlias(prefix, datestamp);
            LOG.info("Successfully replaced aliases. Index {} is now aliased to {}", index, prefix);
        } else {
            LOG.error("Failed to replace the alias. New index {} doesn't exist", index);
        }

    }

    public int fetchDocCount(String prefix, String datestamp)
            throws IOException {
        return client.fetchIndexDocCount(indexOf(prefix, datestamp));
    }

    public void indexJobTitles(String prefix, String datestamp, List<Stillingstittel> list) throws IOException {

        String index = prefix + datestamp;
        if (!list.isEmpty()) {
            BulkResponse bulkResponse = client.indexBulk(list, index);
            reportBulkResponse(bulkResponse, index);
        }

    }

    private void reportBulkResponse(BulkResponse bulkResponse, String index) {

        int failed = 0;
        int success = 0;
        for (BulkItemResponse item : bulkResponse.getItems()) {
            if (item.isFailed()) {
                // TODO implement failed handling later
                LOG.error("Failed item: {}, index: {}", item.getFailureMessage(), index);
                failed++;
            } else {
                LOG.info("Indexed item: {}, index: {}", item.getId(), index);
                success++;
            }
        }
        LOG.info("Indexed {} successfully and {} failed, index: {}", success, failed, index);

    }

    public void deleteIndexWithDatestamp(String prefix, String datestamp)
            throws IOException {
        client.deleteIndex(prefix + datestamp);
    }

    public void deleteOlderIndices(String prefix)
            throws IOException {

        String prefixLowercased = prefix.toLowerCase();
        LocalDate maxAge = LocalDate.now().minusDays(INDEX_EXPIRATION_IN_DAYS);
        client.deleteIndex(
                client
                        .fetchAllIndicesStartingWith(prefixLowercased)
                        .stream()
                        .filter(index -> indexIsBefore(index, prefixLowercased, maxAge))
                        .toArray(String[]::new)
        );

    }

    private boolean indexIsBefore(String index, String prefix, LocalDate date) {

        try {
            return Datestamp.INSTANCE.parseFrom(StringUtils.remove(index, prefix)).isBefore(date);
        } catch (DateTimeParseException e) {
            LOG.error("Couldn't parse date from index name {}", index);
            return false;
        }
    }

}
