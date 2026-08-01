package br.com.matheus.budgeting.infrastructure.web;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Violacao de regra do dominio: valor negativo, descricao vazia, audio
     * inaudivel.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail regraDeNegocioViolada(IllegalArgumentException e) {
        log.warn("Requisicao rejeitada pelo dominio: {}", e.getMessage());

        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        problema.setTitle("Nao foi possivel processar o pedido");
        return problema;
    }

    /**
     * A classe-pai JA declara um @ExceptionHandler para
     * MaxUploadSizeExceededException.
     * Por isso aqui e um @Override do ponto de extensao, e nao um @ExceptionHandler
     * novo.
     */
    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE,
                "O arquivo de audio excede o limite de 25 MB aceito pela transcricao.");
        problema.setTitle("Arquivo muito grande");

        return handleExceptionInternal(e, problema, headers, HttpStatus.PAYLOAD_TOO_LARGE, request);
    }

    /** A IA pediu uma ferramenta e a execucao falhou. */
    @ExceptionHandler(ToolExecutionException.class)
    ProblemDetail falhaNaFerramenta(ToolExecutionException e) {
        log.error("Falha ao executar ferramenta solicitada pelo assistente", e);

        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Falha ao executar uma acao solicitada pelo assistente.");
        problema.setTitle("Erro na execucao da ferramenta");
        return problema;
    }

    /** Rede de seguranca: qualquer coisa nao prevista. */
    @ExceptionHandler(Exception.class)
    ProblemDetail erroInesperado(Exception e) {
        log.error("Erro nao tratado", e);

        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro inesperado ao processar a requisicao.");
        problema.setTitle("Erro interno");
        return problema;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> campos = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "valor invalido" : fe.getDefaultMessage(),
                        (primeiro, segundo) -> primeiro));

        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Um ou mais campos da requisicao sao invalidos.");
        problema.setTitle("Dados invalidos");
        problema.setProperty("errors", campos);

        return handleExceptionInternal(e, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

}