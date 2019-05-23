package no.nav.arbeid.pam.ontologindexer.status

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.arbeid.pam.ontologindexer.es.IndexClientHealthIndicator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Status
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal")
class StatusController {

    //private final IndexClientHealthIndicator indexClientHealthIndicator;

    /*@Autowired
    public StatusController(IndexClientHealthIndicator indexClientHealthIndicator) {
        this.indexClientHealthIndicator = indexClientHealthIndicator;
    }*/

    val isAlive: ResponseEntity<String>
        @GetMapping(path = ["/isAlive"])
        get() = ResponseEntity.ok("OK")

    val isReady: ResponseEntity<String>
        @GetMapping(path = ["/isReady"])
        get() = ResponseEntity.ok("OK")

    /*@GetMapping(path = "/status")
    public ResponseEntity<ObjectNode> statusHealth() {

        boolean isElastisSearchOK = indexClientHealthIndicator.health().getStatus().equals(Status.UP);


        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("Elastic Search connection status", (isElastisSearchOK) ? "OK" : "NOT OK");

        return ResponseEntity.ok(node);
    }*/
}
