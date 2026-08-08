package br.com.matheus.budgeting.infrastructure.web;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.com.matheus.budgeting.application.ai.TranscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Transcricao", description = "Speech-to-Text isolado, sem passar pelo assistente")
@RestController
@RequestMapping("/api/transcribe")
public class TranscriptionController {

    // Formatos aceitos pela API de transcricao da OpenAI (whisper-1).
    // Fonte: https://developers.openai.com/api/docs/guides/speech-to-text
    private static final Set<String> FORMATOS_SUPORTADOS =
            Set.of("flac", "m4a", "mp3", "mp4", "mpeg", "mpga", "oga", "ogg", "wav", "webm");

    private final TranscriptionService transcriptionService;

    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @Operation(
            summary = "Converte um arquivo de audio em texto",
            description = """
                    Etapa 1 do pipeline, exposta isoladamente para depuracao.
                    Util para verificar o que o Whisper entendeu antes de o assistente agir.

                    Formatos aceitos: flac, m4a, mp3, mp4, mpeg, mpga, oga, ogg, wav, webm.
                    Limite de 25 MB.
                    """)
    @ApiResponse(responseCode = "200", description = "Texto transcrito")
    @ApiResponse(responseCode = "400", description = "Arquivo de audio vazio",
            content = @Content(mediaType = "application/problem+json"))
    @ApiResponse(responseCode = "413", description = "Arquivo acima de 25 MB",
            content = @Content(mediaType = "application/problem+json"))
    @ApiResponse(responseCode = "415", description = "Formato de audio nao suportado",
            content = @Content(mediaType = "application/problem+json"))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TranscriptionReply transcribe(
            @Parameter(description = "Arquivo de audio a transcrever",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo de audio vazio.");
        }

        String filename = file.getOriginalFilename();
        String extensao = extrairExtensao(filename);

        if (!FORMATOS_SUPORTADOS.contains(extensao)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Formato '%s' nao suportado. Use: %s".formatted(extensao, FORMATOS_SUPORTADOS));
        }

        return new TranscriptionReply(transcriptionService.transcribe(file.getBytes(), filename));
    }

    private String extrairExtensao(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    public record TranscriptionReply(
            @Schema(example = "O Matheus gastou R$ 60,00 hoje de almoco.")
            String text) {}
}