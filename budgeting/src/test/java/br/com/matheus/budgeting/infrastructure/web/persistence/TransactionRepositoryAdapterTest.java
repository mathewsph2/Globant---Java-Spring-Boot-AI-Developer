package br.com.matheus.budgeting.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import br.com.matheus.budgeting.domain.model.Category;
import br.com.matheus.budgeting.domain.model.Transaction;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TransactionRepositoryAdapter.class)
class TransactionRepositoryAdapterTest {

    @Autowired
    TransactionRepositoryAdapter adapter;

    @Test
    void deveSalvarERecuperarPorPeriodo() {
        var hoje = LocalDate.now();
        adapter.save(Transaction.novo(new BigDecimal("42.00"), "almoco", "Mercado",
                Category.ALIMENTACAO, hoje));

        var encontrados = adapter.findByPeriod(hoje, hoje);

        assertThat(encontrados).hasSize(1);
        assertThat(encontrados.getFirst().id()).isNotNull();
        assertThat(encontrados.getFirst().amount()).isEqualByComparingTo("42.00");
        assertThat(encontrados.getFirst().category()).isEqualTo(Category.ALIMENTACAO);
    }

    @Test
    void naoDeveRetornarGastoForaDoPeriodo() {
        var hoje = LocalDate.now();
        adapter.save(Transaction.novo(new BigDecimal("10.00"), "cafe", null,
                Category.ALIMENTACAO, hoje.minusMonths(2)));

        assertThat(adapter.findByPeriod(hoje.withDayOfMonth(1), hoje)).isEmpty();
    }

    @Test
    void deveFiltrarPorCategoria() {
        var hoje = LocalDate.now();
        adapter.save(Transaction.novo(new BigDecimal("30.00"), "uber", null, Category.TRANSPORTE, hoje));
        adapter.save(Transaction.novo(new BigDecimal("20.00"), "pizza", null, Category.ALIMENTACAO, hoje));

        assertThat(adapter.findByCategoryAndPeriod(Category.TRANSPORTE, hoje, hoje)).hasSize(1);
    }
}