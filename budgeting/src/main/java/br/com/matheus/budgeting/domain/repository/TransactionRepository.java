package br.com.matheus.budgeting.domain.repository;

import java.time.LocalDate;
import java.util.List;

import br.com.matheus.budgeting.domain.model.Category;
import br.com.matheus.budgeting.domain.model.Transaction;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findByPeriod(LocalDate start, LocalDate end);

    List<Transaction> findByCategoryAndPeriod(Category category, LocalDate start, LocalDate end);
}