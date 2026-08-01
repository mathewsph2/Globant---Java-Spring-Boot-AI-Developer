package br.com.matheus.budgeting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class ChatModelIT {

    @Autowired
    ChatModel chatModel;

    @Test
    void deveResponderQuandoChamadoOChatModel() {
        String resposta = chatModel.call(
                "Gere um exemplo de registro de budgeting, com descricao de gasto, "
                        + "valor em reais e local. Responda em portugues do Brasil.");

        assertThat(resposta).isNotEmpty();
        System.out.println(resposta);
    }
}