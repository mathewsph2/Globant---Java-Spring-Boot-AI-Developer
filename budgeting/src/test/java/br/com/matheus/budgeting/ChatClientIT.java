package br.com.matheus.budgeting;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.matheus.budgeting.application.ai.AssistantService;
import br.com.matheus.budgeting.application.usecase.QueryTransactionsUseCase;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class ChatClientIT {

    @Autowired
    AssistantService assistantService;

    @Autowired
    QueryTransactionsUseCase queryTransactions;

    /**
     * Verifica o EFEITO no banco, nao o texto da resposta.
     * A frase que o modelo devolve varia a cada execucao ("registrei", "anotado",
     * "cinquenta reais", "R$ 50,00"); a transacao gravada, nao.
     */
    @Test
    void comandoDeTextoDeveVirarTransacaoNoBanco() {
        String resposta = assistantService.ask("Gastei 50 reais no Starbucks agora");
        System.out.println(resposta);

        LocalDate hoje = LocalDate.now();

        assertThat(queryTransactions.byPeriod(hoje, hoje))
                .anySatisfy(t -> {
                    assertThat(t.amount()).isEqualByComparingTo("50.00");
                    assertThat(t.occurredOn()).isEqualTo(hoje);
                });
    }

    @Test
    void deveConhecerADataDeHojeInjetadaNoSystemPrompt() {
        String resposta = assistantService.ask("Em que ano estamos? Responda apenas o ano.");

        System.out.println(resposta);

        assertThat(resposta).contains(String.valueOf(LocalDate.now().getYear()));
    }
}
