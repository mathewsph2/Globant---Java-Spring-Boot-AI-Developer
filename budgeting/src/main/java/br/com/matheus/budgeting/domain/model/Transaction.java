package br.com.matheus.budgeting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(
        Long id,
        BigDecimal amount,
        String description,
        String place,
        Category category,
        LocalDate occurredOn) {

    public Transaction {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("O valor do gasto deve ser maior que zero.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("A descricao do gasto e obrigatoria.");
        }
        if (occurredOn == null) {
            throw new IllegalArgumentException("A data do gasto e obrigatoria.");
        }
        if (category == null) {
            category = Category.OUTROS;
        }
        amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public static Transaction novo(BigDecimal amount, String description,
                                   String place, Category category, LocalDate occurredOn) {
        return new Transaction(null, amount, description, place, category, occurredOn);
    }
}