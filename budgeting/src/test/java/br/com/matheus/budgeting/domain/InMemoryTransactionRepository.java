package br.com.matheus.budgeting.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.matheus.budgeting.domain.model.Category;
import br.com.matheus.budgeting.domain.model.Transaction;
import br.com.matheus.budgeting.domain.repository.TransactionRepository;

/**
 * Implementacao em memoria da porta TransactionRepository, para testes de use case.
 * Sem Spring, sem banco, sem framework de mock: e apenas uma classe Java.
 *
 * <p>Existe porque o dominio define a porta como interface. Se os use cases
 * dependessem de JPA diretamente, testar exigiria subir um banco.
 */
public class InMemoryTransactionRepository implements TransactionRepository {

    private final List<Transaction> stored = new ArrayList<>();

    @Override
    public Transaction save(Transaction transaction) {
        var saved = new Transaction(
                (long) stored.size() + 1,
                transaction.amount(),
                transaction.description(),
                transaction.place(),
                transaction.category(),
                transaction.occurredOn());

        stored.add(saved);
        return saved;
    }

    @Override
    public List<Transaction> findByPeriod(LocalDate start, LocalDate end) {
        return stored.stream()
                .filter(t -> !t.occurredOn().isBefore(start) && !t.occurredOn().isAfter(end))
                .toList();
    }

    @Override
    public List<Transaction> findByCategoryAndPeriod(Category category, LocalDate start, LocalDate end) {
        return findByPeriod(start, end).stream()
                .filter(t -> t.category() == category)
                .toList();
    }
}
