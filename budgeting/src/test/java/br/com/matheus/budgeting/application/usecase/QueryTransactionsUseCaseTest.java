package br.com.matheus.budgeting.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.com.matheus.budgeting.domain.InMemoryTransactionRepository;
import br.com.matheus.budgeting.domain.model.Category;
import br.com.matheus.budgeting.domain.model.Transaction;

import static org.assertj.core.api.Assertions.assertThat;

class QueryTransactionsUseCaseTest {

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final QueryTransactionsUseCase useCase = new QueryTransactionsUseCase(repository);

    private final LocalDate hoje = LocalDate.now();

    @Test
    void deveSomarOsGastosDoPeriodo() {
        registrar("50.00", "cafe", Category.ALIMENTACAO, hoje);
        registrar("30.50", "uber", Category.TRANSPORTE, hoje);

        assertThat(useCase.totalByPeriod(hoje, hoje)).isEqualByComparingTo("80.50");
    }

    @Test
    void deveRetornarZeroQuandoNaoHaGastosNoPeriodo() {
        assertThat(useCase.totalByPeriod(hoje, hoje)).isEqualByComparingTo("0");
    }

    @Test
    void naoDeveSomarGastosForaDoPeriodo() {
        registrar("50.00", "cafe", Category.ALIMENTACAO, hoje);
        registrar("999.00", "gasto antigo", Category.OUTROS, hoje.minusMonths(2));

        assertThat(useCase.totalByPeriod(hoje.withDayOfMonth(1), hoje))
                .isEqualByComparingTo("50.00");
    }

    @Test
    void deveIncluirGastosNasBordasDoPeriodo() {
        LocalDate inicio = hoje.minusDays(5);
        LocalDate fim = hoje;

        registrar("10.00", "primeiro dia", Category.OUTROS, inicio);
        registrar("20.00", "ultimo dia", Category.OUTROS, fim);

        assertThat(useCase.totalByPeriod(inicio, fim)).isEqualByComparingTo("30.00");
    }

    @Test
    void deveListarOsGastosDoPeriodo() {
        registrar("50.00", "cafe", Category.ALIMENTACAO, hoje);
        registrar("30.00", "uber", Category.TRANSPORTE, hoje);

        assertThat(useCase.byPeriod(hoje, hoje))
                .hasSize(2)
                .extracting(Transaction::description)
                .containsExactlyInAnyOrder("cafe", "uber");
    }

    @Test
    void deveAgruparTotaisPorCategoria() {
        registrar("50.00", "cafe", Category.ALIMENTACAO, hoje);
        registrar("20.00", "pizza", Category.ALIMENTACAO, hoje);
        registrar("30.00", "uber", Category.TRANSPORTE, hoje);

        var totais = useCase.totalGroupedByCategory(hoje, hoje);

        assertThat(totais).hasSize(2);
        assertThat(totais.get(Category.ALIMENTACAO)).isEqualByComparingTo("70.00");
        assertThat(totais.get(Category.TRANSPORTE)).isEqualByComparingTo("30.00");
    }

    @Test
    void deveRetornarMapaVazioQuandoNaoHaGastos() {
        assertThat(useCase.totalGroupedByCategory(hoje, hoje)).isEmpty();
    }

    private void registrar(String valor, String descricao, Category categoria, LocalDate data) {
        repository.save(Transaction.novo(new BigDecimal(valor), descricao, null, categoria, data));
    }
}
