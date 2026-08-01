package br.com.matheus.budgeting.application.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, BudgetingTools budgetingTools) {
        return builder
                .defaultTools(budgetingTools)
                .build();
    }
}