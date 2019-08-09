package no.nav.arbeid.pam.ontologindexer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class DevApplication

fun main(args: Array<String>) {
    runApplication<DevApplication>("--spring.profiles.active=dev", *args)
}
