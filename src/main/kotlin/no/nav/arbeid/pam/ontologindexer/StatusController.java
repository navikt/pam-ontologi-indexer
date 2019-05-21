package no.nav.arbeid.pam.ontologindexer;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.nav.arbeid.pam.ontologindexer.es.IndexClientHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller("/internal")
public class StatusController {

    private final IndexClientHealthIndicator indexClientHealthIndicator;

    @Autowired
    public StatusController(IndexClientHealthIndicator indexClientHealthIndicator) {
        this.indexClientHealthIndicator = indexClientHealthIndicator;
    }

    @GetMapping(path = "/isAlive")
    public ResponseEntity<String> isAlive() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping(path = "/isReady")
    public ResponseEntity<String> isReady() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping(path = "/internal/status")
    public ResponseEntity<ObjectNode> statusHealth() {

        boolean isElastisSearchOK = indexClientHealthIndicator.health().getStatus().equals(Status.UP);


        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("Elastic Search connection status", (isElastisSearchOK) ? "OK" : "NOT OK");

        return ResponseEntity.ok(node);
    }
}
