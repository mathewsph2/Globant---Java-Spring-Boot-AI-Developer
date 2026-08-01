package br.com.matheus.budgeting;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import br.com.matheus.budgeting.application.ai.VoiceAssistantService;
import br.com.matheus.budgeting.application.usecase.QueryTransactionsUseCase;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class VoiceAssistantIT {

    @Autowired
    VoiceAssistantService voiceAssistantService;

    @Autowired
    QueryTransactionsUseCase queryTransactions;

    @Test
    void audioDeGastoDeveVirarTransacaoNoBancoERespostaEmAudio() throws Exception {
        var audio = new ClassPathResource("audio/almoco.ogg");

        var interacao = voiceAssistantService.handle(
                audio.getContentAsByteArray(), "almoco.ogg");

        System.out.println("Transcricao: " + interacao.transcription());
        System.out.println("Resposta:    " + interacao.reply());

        // 1. o audio foi entendido
        assertThat(interacao.transcription()).isNotBlank();

        // 2. o efeito real aconteceu no banco
        LocalDate hoje = LocalDate.now();
        assertThat(queryTransactions.byPeriod(hoje, hoje)).isNotEmpty();

        // 3. voltou audio de verdade
        assertThat(interacao.audio()).hasSizeGreaterThan(1024);
    }
}