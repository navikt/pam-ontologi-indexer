package no.nav.arbeid.pam.ontologindexer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableScheduling
class PamOntologiIndexerApplication

fun main(args: Array<String>) {
    runApplication<PamOntologiIndexerApplication>(*args)
}
