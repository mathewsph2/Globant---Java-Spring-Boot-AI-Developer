package br.com.matheus.budgeting.infrastructure.web;

import br.com.matheus.budgeting.application.ai.AssistantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AssistantService assistantService;

    public ChatController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping
    public ChatReply chat(@Valid @RequestBody ChatRequest request) {
        return new ChatReply(assistantService.ask(request.message()));
    }

    public record ChatRequest(@NotBlank(message = "A mensagem nao pode ser vazia") String message) {}

    public record ChatReply(String reply) {}
}