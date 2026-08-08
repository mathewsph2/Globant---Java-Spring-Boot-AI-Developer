package br.com.matheus.budgeting.infrastructure.web;

import br.com.matheus.budgeting.application.ai.AssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat", description = "Conversa por texto com o assistente")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AssistantService assistantService;

    public ChatController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @Operation(
            summary = "Envia uma mensagem de texto ao assistente",
            description = """
                    O assistente pode chamar ferramentas para registrar ou consultar gastos.
                    Exemplos: "Gastei 50 reais no Starbucks agora", "Quanto gastei este mes?"
                    """)
    @ApiResponse(responseCode = "200", description = "Resposta do assistente")
    @ApiResponse(responseCode = "400", description = "Mensagem vazia",
            content = @Content(mediaType = "application/problem+json"))
    @PostMapping
    public ChatReply chat(@Valid @RequestBody ChatRequest request) {
        return new ChatReply(assistantService.ask(request.message()));
    }

    public record ChatRequest(
            @Schema(example = "Gastei 50 reais no Starbucks agora")
            @NotBlank(message = "A mensagem nao pode ser vazia")
            String message) {}

    public record ChatReply(String reply) {}
}