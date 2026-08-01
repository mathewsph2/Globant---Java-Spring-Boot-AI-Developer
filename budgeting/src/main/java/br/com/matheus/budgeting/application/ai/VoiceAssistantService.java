package br.com.matheus.budgeting.application.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VoiceAssistantService {

    private static final Logger log = LoggerFactory.getLogger(VoiceAssistantService.class);

    private final TranscriptionService transcriptionService;
    private final AssistantService assistantService;
    private final TextToSpeechService textToSpeechService;

    public VoiceAssistantService(TranscriptionService transcriptionService,
                                 AssistantService assistantService,
                                 TextToSpeechService textToSpeechService) {
        this.transcriptionService = transcriptionService;
        this.assistantService = assistantService;
        this.textToSpeechService = textToSpeechService;
    }

    public VoiceInteraction handle(byte[] audio, String filename) {
        long inicio = System.currentTimeMillis();

        String transcricao = transcriptionService.transcribe(audio, filename);
        if (transcricao == null || transcricao.isBlank()) {
            throw new IllegalArgumentException("Nao foi possivel entender o audio enviado.");
        }

        String resposta = assistantService.ask(transcricao);
        byte[] audioResposta = textToSpeechService.synthesize(resposta);

        log.info("[VOICE] pipeline completo em {} ms", System.currentTimeMillis() - inicio);

        return new VoiceInteraction(transcricao, resposta, audioResposta);
    }

    public record VoiceInteraction(String transcription, String reply, byte[] audio) {}
}