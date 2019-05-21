package no.nav.arbeid.pam.ontologindexer.config

import org.apache.http.HttpHost
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory

@Configuration
@Profile("!test")
class RestClientConfig(@param:Value("\${DRIFTPROXY_URL:http://proxy.com}") private val proxyUrl: String) {

    @Bean
    fun proxyTemplateCustomizer(): RestTemplateCustomizer {
        return RestTemplateCustomizer { restTemplate ->
            val proxy = HttpHost.create(proxyUrl)
            val httpClient = HttpClientBuilder.create().setProxy(proxy).build()
            restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
        }
    }

}
