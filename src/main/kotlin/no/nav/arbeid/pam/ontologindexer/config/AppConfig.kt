package no.nav.arbeid.pam.ontologindexer.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.apache.http.conn.ssl.DefaultHostnameVerifier
import org.apache.http.impl.client.HttpClientBuilder
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component

@Component
class AppConfig {

    @Autowired
    lateinit var envConf: EnvConf

    @Bean
    fun elasticClientBuilder(): RestClientBuilder {
        return RestClient.builder(HttpHost.create(envConf.elasticsearchUrl!!)).setHttpClientConfigCallback({
            it.setSSLHostnameVerifier(DefaultHostnameVerifier())
        })
    }

    @Bean
    fun restTemplateCustomizer(): RestTemplateCustomizer {
        return RestTemplateCustomizer { restTemplate ->
            val httpClient = HttpClientBuilder.create().setSSLHostnameVerifier(DefaultHostnameVerifier()).build()
            restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
        }
    }

    @Bean
    fun jacksonMapper(): ObjectMapper {
        return ObjectMapper()
    }

}
