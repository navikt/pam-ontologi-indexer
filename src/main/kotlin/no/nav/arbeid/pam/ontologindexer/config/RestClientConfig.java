package no.nav.arbeid.pam.ontologindexer.config;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
@Profile("!test")
public class RestClientConfig {

    private final String proxyUrl;

    public RestClientConfig(@Value("${DRIFTPROXY_URL:http://proxy.com}") String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    @Bean
    public RestTemplateCustomizer proxyTemplateCustomizer() {
        return restTemplate -> {
            HttpHost proxy = HttpHost.create(proxyUrl);
            HttpClient httpClient = HttpClientBuilder.create().setProxy(proxy).build();
            restTemplate.setRequestFactory(
                    new HttpComponentsClientHttpRequestFactory(httpClient));
        };
    }

}
