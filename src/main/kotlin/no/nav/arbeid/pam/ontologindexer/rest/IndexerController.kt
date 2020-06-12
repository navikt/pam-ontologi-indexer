package no.nav.arbeid.pam.ontologindexer.rest

import no.nav.arbeid.pam.ontologindexer.service.JobbtittelIndexerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal")
class IndexerController @Autowired
constructor(private val indexerService: JobbtittelIndexerService) {

    @PostMapping(path = ["/reindex"])
    fun reindex(): ResponseEntity<String> {
        return try {
            indexerService.indexJanzzConcepts()
            ResponseEntity.ok("OK")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed: " + e.message)
        }
    }

    @PostMapping(path = ["/deleteOldIndices"])
    fun deleteOlderIndices(): ResponseEntity<String> {
        return try {
            indexerService.deleteOldIndexes()
            ResponseEntity.ok("OK")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed: " + e.message)
        }
    }
}


