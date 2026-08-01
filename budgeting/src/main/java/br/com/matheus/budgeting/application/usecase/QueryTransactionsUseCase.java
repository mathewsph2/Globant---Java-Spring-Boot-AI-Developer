package br.com.matheus.budgeting.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.matheus.budgeting.domain.model.Category;
import br.com.matheus.budgeting.domain.model.Transaction;
import br.com.matheus.budgeting.domain.repository.TransactionRepository;

@Service
public class QueryTransactionsUseCase {

    private final TransactionRepository repository;

    public QueryTransactionsUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> byPeriod(LocalDate start, LocalDate end) {
        return repository.findByPeriod(start, end);
    }

    public BigDecimal totalByPeriod(LocalDate start, LocalDate end) {
        return repository.findByPeriod(start, end).stream()
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<Category, BigDecimal> totalGroupedByCategory(LocalDate start, LocalDate end) {
        return repository.findByPeriod(start, end).stream()
                .collect(Collectors.groupingBy(
                        Transaction::category,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));
    }
}