package br.com.matheus.budgeting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.matheus.budgeting.domain.model.Category;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findByOccurredOnBetweenOrderByOccurredOnDesc(
            LocalDate start, LocalDate end);

    List<TransactionEntity> findByCategoryAndOccurredOnBetweenOrderByOccurredOnDesc(
            Category category, LocalDate start, LocalDate end);
}