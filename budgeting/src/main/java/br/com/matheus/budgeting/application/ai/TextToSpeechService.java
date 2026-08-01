package br.com.matheus.budgeting.application.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.stereotype.Service;

@Service
public class TextToSpeechService {

    private static final Logger log = LoggerFactory.getLogger(TextToSpeechService.class);

    private final TextToSpeechModel textToSpeechModel;

    public TextToSpeechService(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    public byte[] synthesize(String text) {
        byte[] audio = textToSpeechModel.call(text);
        log.info("[TTS] \"{}\" -> {} bytes de audio", text, audio.length);
        return audio;
    }
}