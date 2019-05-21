package no.nav.arbeid.pam.ontologindexer.config

import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory

@Configuration
@Profile("test")
class RestClientConfigDev {

    @Bean
    fun proxyTemplateCustomizer(): RestTemplateCustomizer {
        return RestTemplateCustomizer { restTemplate ->
            val httpClient = HttpClientBuilder.create().build()
            restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
        }
    }

}
