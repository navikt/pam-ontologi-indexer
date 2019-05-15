package no.nav.arbeid.pam.ontologindexer.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EnvConf {

    @Value("\${ontologi.url:http://localhost:8189/ontologi/}")
    val ontologiUrl: String? = null

}
