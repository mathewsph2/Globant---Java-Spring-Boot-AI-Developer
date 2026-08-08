package br.com.matheus.budgeting.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.matheus.budgeting.application.ai.TextToSpeechService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Sintese de voz", description = "Text-to-Speech isolado, sem passar pelo assistente")
@RestController
@RequestMapping("/api/synthesize")
public class TextToSpeechController {

    private final TextToSpeechService textToSpeechService;

    public TextToSpeechController(TextToSpeechService textToSpeechService) {
        this.textToSpeechService = textToSpeechService;
    }

    @Operation(
            summary = "Converte um texto em audio MP3",
            description = """
                    Etapa 4 do pipeline, exposta isoladamente.
                    Util para comparar vozes e testar como o texto soa falado.

                    Valores monetarios ficam melhores por extenso: "sessenta reais" soa
                    natural, enquanto "R$ 60,00" e lido de forma estranha pelo sintetizador.

                    Limite de 4096 caracteres.
                    """)
    @ApiResponse(responseCode = "200", description = "Audio sintetizado",
            content = @Content(mediaType = "audio/mpeg",
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "400", description = "Texto vazio ou acima de 4096 caracteres",
            content = @Content(mediaType = "application/problem+json"))
    @PostMapping(produces = "audio/mpeg")
    public ResponseEntity<byte[]> synthesize(@Valid @RequestBody SynthesizeRequest request) {
        byte[] audio = textToSpeechService.synthesize(request.text());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("resposta.mp3").build().toString())
                .body(audio);
    }

    public record SynthesizeRequest(
            @Schema(example = "Voce gastou sessenta reais este mes.")
            @NotBlank(message = "O texto nao pode ser vazio")
            @Size(max = 4096, message = "Texto muito longo para sintese de voz.")
            String text) {}
}