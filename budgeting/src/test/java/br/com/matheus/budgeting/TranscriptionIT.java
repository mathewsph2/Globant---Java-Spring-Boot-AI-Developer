package br.com.matheus.budgeting;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import br.com.matheus.budgeting.application.ai.TranscriptionService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class TranscriptionIT {

    @Autowired
    TranscriptionService transcriptionService;

    @Test
    void deveTranscreverAudioEmPortugues() throws IOException {
        var audio = new ClassPathResource("audio/gasto.m4a");

        String texto = transcriptionService.transcribe(
                audio.getContentAsByteArray(), "gasto.m4a");

        System.out.println("Transcrito: " + texto);

        assertThat(texto).isNotBlank();
        assertThat(texto.toLowerCase()).contains("mercado");
    }
}