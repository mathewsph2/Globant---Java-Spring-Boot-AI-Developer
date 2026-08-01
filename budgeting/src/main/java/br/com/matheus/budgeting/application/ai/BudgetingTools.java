package br.com.matheus.budgeting.application.ai;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import br.com.matheus.budgeting.application.usecase.QueryTransactionsUseCase;
import br.com.matheus.budgeting.application.usecase.RegisterTransactionUseCase;
import br.com.matheus.budgeting.domain.model.Category;
import br.com.matheus.budgeting.domain.model.Transaction;

@Component
public class BudgetingTools {

    private static final Logger log = LoggerFactory.getLogger(BudgetingTools.class);

    private final RegisterTransactionUseCase registerTransaction;
    private final QueryTransactionsUseCase queryTransactions;

    public BudgetingTools(RegisterTransactionUseCase registerTransaction,
                          QueryTransactionsUseCase queryTransactions) {
        this.registerTransaction = registerTransaction;
        this.queryTransactions = queryTransactions;
    }

    @Tool(description = """
            Registra um novo gasto do usuario no sistema.
            Use sempre que o usuario relatar uma compra, pagamento ou gasto que ja aconteceu.
            """)
    public String registrarGasto(
            @ToolParam(description = "Valor do gasto em reais, apenas o numero. Exemplo: 50.00")
            BigDecimal valor,

            @ToolParam(description = "Descricao curta do que foi comprado. Exemplo: cafe, almoco, combustivel. "
                    + "Se o usuario nao disser, deixe vazio", required = false)
            String descricao,

            @ToolParam(description = "Estabelecimento onde ocorreu o gasto", required = false)
            String local,

            @ToolParam(description = "Categoria do gasto")
            Category categoria,

            @ToolParam(description = "Data do gasto no formato aaaa-MM-dd. "
                    + "Se o usuario nao disser quando foi, deixe vazio", required = false)
            String data) {

        // A IA pode omitir campos opcionais. Aqui aplicamos os padroes,
        // para nunca precisar interromper o usuario pedindo detalhe secundario.
        String descricaoFinal = (descricao != null && !descricao.isBlank()) ? descricao
                : (local != null && !local.isBlank()) ? local
                : "Gasto";

        LocalDate dataFinal = (data != null && !data.isBlank())
                ? LocalDate.parse(data)
                : LocalDate.now();

        var command = new RegisterTransactionUseCase.Command(
                valor, descricaoFinal, local, categoria, dataFinal);

        Transaction saved = registerTransaction.execute(command);
        log.info("[TOOL] registrarGasto -> {}", saved);

        return "Gasto registrado com id %d: R$ %s, %s, em %s, categoria %s."
                .formatted(saved.id(), saved.amount(), saved.description(),
                        saved.occurredOn(), saved.category());
    }

    @Tool(description = """
            Consulta o valor TOTAL gasto pelo usuario dentro de um periodo.
            Use para perguntas como "quanto gastei este mes" ou "quanto gastei esta semana".
            """)
    public String consultarTotalDoPeriodo(
            @ToolParam(description = "Data inicial do periodo, formato aaaa-MM-dd") String dataInicial,
            @ToolParam(description = "Data final do periodo, formato aaaa-MM-dd") String dataFinal) {

        BigDecimal total = queryTransactions.totalByPeriod(
                LocalDate.parse(dataInicial), LocalDate.parse(dataFinal));

        log.info("[TOOL] consultarTotalDoPeriodo {} a {} -> {}", dataInicial, dataFinal, total);

        return "Total gasto entre %s e %s: R$ %s.".formatted(dataInicial, dataFinal, total);
    }

    @Tool(description = """
            Lista os gastos individuais do usuario dentro de um periodo,
            com valor, descricao, local e data de cada um.
            """)
    public String listarGastosDoPeriodo(
            @ToolParam(description = "Data inicial do periodo, formato aaaa-MM-dd") String dataInicial,
            @ToolParam(description = "Data final do periodo, formato aaaa-MM-dd") String dataFinal) {

        List<Transaction> gastos = queryTransactions.byPeriod(
                LocalDate.parse(dataInicial), LocalDate.parse(dataFinal));

        log.info("[TOOL] listarGastosDoPeriodo {} a {} -> {} registros",
                dataInicial, dataFinal, gastos.size());

        if (gastos.isEmpty()) {
            return "Nenhum gasto registrado nesse periodo.";
        }

        return gastos.stream()
                .map(t -> "%s | R$ %s | %s | %s | %s".formatted(
                        t.occurredOn(), t.amount(), t.description(),
                        t.place() == null ? "-" : t.place(), t.category()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    @Tool(description = """
            Mostra quanto o usuario gastou em cada categoria dentro de um periodo.
            Use para perguntas como "com o que eu mais gasto" ou "quanto gastei com alimentacao".
            """)
    public String consultarTotalPorCategoria(
            @ToolParam(description = "Data inicial do periodo, formato aaaa-MM-dd") String dataInicial,
            @ToolParam(description = "Data final do periodo, formato aaaa-MM-dd") String dataFinal) {

        Map<Category, BigDecimal> totais = queryTransactions.totalGroupedByCategory(
                LocalDate.parse(dataInicial), LocalDate.parse(dataFinal));

        log.info("[TOOL] consultarTotalPorCategoria {} a {} -> {}", dataInicial, dataFinal, totais);

        if (totais.isEmpty()) {
            return "Nenhum gasto registrado nesse periodo.";
        }

        return totais.entrySet().stream()
                .map(e -> "%s: R$ %s".formatted(e.getKey(), e.getValue()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }
}