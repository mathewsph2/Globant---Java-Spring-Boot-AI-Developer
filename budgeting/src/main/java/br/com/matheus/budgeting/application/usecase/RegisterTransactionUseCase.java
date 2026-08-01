package br.com.matheus.budgeting.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import br.com.matheus.budgeting.domain.model.Category;
import br.com.matheus.budgeting.domain.model.Transaction;
import br.com.matheus.budgeting.domain.repository.TransactionRepository;

@Service
public class RegisterTransactionUseCase {

    private final TransactionRepository repository;

    public RegisterTransactionUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction execute(Command command) {
        Transaction transaction = Transaction.novo(
                command.amount(),
                command.description(),
                command.place(),
                command.category(),
                command.occurredOn() != null ? command.occurredOn() : LocalDate.now());

        return repository.save(transaction);
    }

    public record Command(
            BigDecimal amount,
            String description,
            String place,
            Category category,
            LocalDate occurredOn) {}
}