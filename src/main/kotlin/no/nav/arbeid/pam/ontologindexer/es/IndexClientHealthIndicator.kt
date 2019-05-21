package no.nav.arbeid.pam.ontologindexer.es

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class IndexClientHealthIndicator
@Autowired
constructor(private val client: ElasticsearchIndexClient) : HealthIndicator {

    private val LOGGER = LoggerFactory.getLogger(IndexClientHealthIndicator::class.java)

    override fun health(): Health {
        return try {
            if (client.isHealthy) Health.up().build() else Health.down().build()
        } catch (e: IOException) {
            LOGGER.error("Failed health check of {}", client.javaClass.simpleName, e)
            Health.down(e).build()
        }

    }

}
