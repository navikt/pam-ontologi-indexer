package no.nav.arbeid.pam.ontologindexer

import no.nav.arbeid.pam.ontologindexer.status.StatusController
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableScheduling
//@Import(StatusController::class)
class PamOntologiIndexerApplication

fun main(args: Array<String>) {
    runApplication<PamOntologiIndexerApplication>(*args)
}
