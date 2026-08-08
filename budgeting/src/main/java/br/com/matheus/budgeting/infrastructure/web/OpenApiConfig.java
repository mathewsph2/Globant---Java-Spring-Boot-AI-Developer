package br.com.matheus.budgeting.infrastructure.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI budgetingOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Budgeting — API Inteligente")
                .version("0.0.1")
                .description("""
                        Assistente financeiro comandado por voz.

                        O fluxo principal esta em POST /api/assistant/voice: envie um arquivo
                        de audio e receba um MP3 com a resposta falada. Os demais endpoints
                        expoem as etapas isoladas do pipeline (transcricao, chat e sintese).

                        Os dados sao persistidos em H2 em memoria e reiniciam a cada execucao.
                        """));
    }
}