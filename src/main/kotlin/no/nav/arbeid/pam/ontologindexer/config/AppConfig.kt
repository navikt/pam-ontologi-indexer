package no.nav.arbeid.pam.ontologindexer.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.InetSocketAddress
import java.net.MalformedURLException
import java.net.Proxy
import java.net.URL

@Component
class AppConfig {

    @Autowired
    lateinit var envConf: EnvConf

    @Bean
    fun elasticClientBuilder(): RestClientBuilder {
        return RestClient.builder(HttpHost.create(envConf.elasticsearchUrl!!))
    }


    @Bean
    fun jacksonMapper(): ObjectMapper {
        return ObjectMapper()
    }

}
