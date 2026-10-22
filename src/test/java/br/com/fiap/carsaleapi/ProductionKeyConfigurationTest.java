package br.com.fiap.carsaleapi;

import br.com.fiap.carsaleapi.auth.JwtKeyConfiguration;
import br.com.fiap.carsaleapi.props.RsaKeyProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionKeyConfigurationTest {

    @Test
    void productionDoesNotAcceptMissingKeys() {
        new ApplicationContextRunner().withUserConfiguration(PropertiesConfiguration.class, JwtKeyConfiguration.class)
                .withPropertyValues("spring.profiles.active=prod")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseMessage(
                            "Configure RSA_PUBLIC_KEY e RSA_PRIVATE_KEY com arquivos PEM externos.");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(RsaKeyProperties.class)
    static class PropertiesConfiguration {
    }
}
