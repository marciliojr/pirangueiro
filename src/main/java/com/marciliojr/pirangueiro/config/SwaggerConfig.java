package com.marciliojr.pirangueiro.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Controle Financeiro - Pirangueiro")
                        .description("API RESTful para controle de receitas e gastos pessoais. " +
                                   "Esta API fornece endpoints para gerenciar receitas, despesas, " +
                                   "contas bancárias, categorias e gerar relatórios financeiros.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Marcilio Jr")
                                .email("marciliojr@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Servidor Local Docker"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor Local Desenvolvimento"),
                        new Server()
                                .url("http://host.docker.internal:8080/api")
                                .description("Servidor Docker (acesso do host)")
                ));
    }
} 