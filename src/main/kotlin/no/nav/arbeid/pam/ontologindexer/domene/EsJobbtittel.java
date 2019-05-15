package no.nav.arbeid.pam.ontologindexer.domene;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.nav.elasticsearch.mapping.annotations.ElasticDocument;
import no.nav.elasticsearch.mapping.annotations.ElasticTextField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ElasticDocument
public class EsJobbtittel {
    public EsJobbtittel(String jobbtittel) {
        this.jobbtittel = jobbtittel;
    }

    @ElasticTextField
    private String jobbtittel;

}
