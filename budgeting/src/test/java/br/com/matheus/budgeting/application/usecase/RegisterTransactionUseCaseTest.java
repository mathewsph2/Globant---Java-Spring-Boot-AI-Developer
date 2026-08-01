package br.com.matheus.budgeting.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.com.matheus.budgeting.domain.InMemoryTransactionRepository;
import br.com.matheus.budgeting.domain.model.Category;
import br.com.matheus.budgeting.domain.model.Transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterTransactionUseCaseTest {

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final RegisterTransactionUseCase useCase = new RegisterTransactionUseCase(repository);

    @Test
    void deveRegistrarGastoComTodosOsDados() {
        var command = new RegisterTransactionUseCase.Command(
                new BigDecimal("50.00"), "Cafe", "Starbucks",
                Category.ALIMENTACAO, LocalDate.of(2026, 7, 31));

        Transaction result = useCase.execute(command);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.amount()).isEqualByComparingTo("50.00");
        assertThat(result.category()).isEqualTo(Category.ALIMENTACAO);
    }

    @Test
    void deveUsarHojeQuandoDataNaoInformada() {
        var command = new RegisterTransactionUseCase.Command(
                new BigDecimal("20"), "Uber", null, Category.TRANSPORTE, null);

        assertThat(useCase.execute(command).occurredOn()).isEqualTo(LocalDate.now());
    }

    @Test
    void deveUsarOutrosQuandoCategoriaNaoInformada() {
        var command = new RegisterTransactionUseCase.Command(
                new BigDecimal("20"), "Algo", null, null, LocalDate.now());

        assertThat(useCase.execute(command).category()).isEqualTo(Category.OUTROS);
    }

    @Test
    void deveRejeitarValorNegativo() {
        var command = new RegisterTransactionUseCase.Command(
                new BigDecimal("-10"), "Errado", null, Category.OUTROS, LocalDate.now());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maior que zero");
    }
}