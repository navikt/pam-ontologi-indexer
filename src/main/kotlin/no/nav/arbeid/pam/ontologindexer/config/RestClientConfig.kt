package no.nav.arbeid.pam.ontologindexer.config

import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory

@Configuration
class RestClientConfig {

    @Bean
    fun templateCustomizer(): RestTemplateCustomizer {
        return RestTemplateCustomizer { restTemplate ->
            val httpClient = HttpClientBuilder.create().build()
            restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
        }
    }

}
