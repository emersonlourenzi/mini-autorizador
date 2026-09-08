package br.com.miniautorizador.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {
    @Bean
    OpenAPI miniAutorizadorOpenApi() {
        return new OpenAPI().info(new Info()
            .title("Mini-autorizador")
            .version("v1")
            .description("Criação de cartões, consulta de saldo e autorização de transações."));
    }
}
