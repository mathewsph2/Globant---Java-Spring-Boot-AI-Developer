package br.com.matheus.budgeting.infrastructure.web;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import br.com.matheus.budgeting.application.ai.TranscriptionService;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TranscriptionReply transcribe(@RequestParam("file") MultipartFile file) throws IOException {
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

    public record TranscriptionReply(String text) {}
}