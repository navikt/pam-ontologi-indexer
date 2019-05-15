package no.nav.arbeid.pam.ontologindexer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import no.nav.arbeid.pam.ontologindexer.service.JobbtittelIndexerServiceTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Profile({"test"})
@Configuration
public class WireMockConfig {

  @Bean
  public WireMockServer wireMockServer() {
    WireMockServer wireMockServer =  new WireMockServer(wireMockConfig().notifier(new ConsoleNotifier(true)).port(8189));
    wireMockServer.stubFor(JobbtittelIndexerServiceTest.Companion.mappingBuilder());
    wireMockServer.start();
    return wireMockServer;
  }

}
