package br.com.matheus.budgeting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.matheus.budgeting.domain.model.Category;
import br.com.matheus.budgeting.domain.model.Transaction;
import br.com.matheus.budgeting.domain.repository.TransactionRepository;

@Component
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity saved = jpaRepository.save(toEntity(transaction));
        return toDomain(saved);
    }

    @Override
    public List<Transaction> findByPeriod(LocalDate start, LocalDate end) {
        return jpaRepository.findByOccurredOnBetweenOrderByOccurredOnDesc(start, end)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Transaction> findByCategoryAndPeriod(Category category, LocalDate start, LocalDate end) {
        return jpaRepository
                .findByCategoryAndOccurredOnBetweenOrderByOccurredOnDesc(category, start, end)
                .stream().map(this::toDomain).toList();
    }

    private TransactionEntity toEntity(Transaction t) {
        return new TransactionEntity(t.id(), t.amount(), t.description(),
                t.place(), t.category(), t.occurredOn());
    }

    private Transaction toDomain(TransactionEntity e) {
        return new Transaction(e.getId(), e.getAmount(), e.getDescription(),
                e.getPlace(), e.getCategory(), e.getOccurredOn());
    }
}