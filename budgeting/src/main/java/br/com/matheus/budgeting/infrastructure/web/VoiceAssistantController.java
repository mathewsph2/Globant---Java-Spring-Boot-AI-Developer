package br.com.matheus.budgeting.infrastructure.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.com.matheus.budgeting.application.ai.VoiceAssistantService;
import br.com.matheus.budgeting.application.ai.VoiceAssistantService.VoiceInteraction;

@RestController
@RequestMapping("/api/assistant/voice")
public class VoiceAssistantController {

    private static final Set<String> FORMATOS_SUPORTADOS =
            Set.of("flac", "m4a", "mp3", "mp4", "mpeg", "mpga", "oga", "ogg", "wav", "webm");

    private final VoiceAssistantService voiceAssistantService;

    public VoiceAssistantController(VoiceAssistantService voiceAssistantService) {
        this.voiceAssistantService = voiceAssistantService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mpeg")
    public ResponseEntity<byte[]> conversar(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo de audio vazio.");
        }

        String filename = file.getOriginalFilename();
        String extensao = extrairExtensao(filename);

        if (!FORMATOS_SUPORTADOS.contains(extensao)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Formato '%s' nao suportado. Use: %s".formatted(extensao, FORMATOS_SUPORTADOS));
        }

        VoiceInteraction interacao;
        try {
            interacao = voiceAssistantService.handle(file.getBytes(), filename);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("resposta.mp3").build().toString())
                .header("X-Transcription", encode(interacao.transcription()))
                .header("X-Reply", encode(interacao.reply()))
                .body(interacao.audio());
    }

    /** Cabecalho HTTP so aceita ASCII. Acento em portugues quebra — por isso o encode. */
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String extrairExtensao(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}