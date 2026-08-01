package br.com.matheus.budgeting.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.matheus.budgeting.domain.model.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;

    private String place;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "occurred_on", nullable = false)
    private LocalDate occurredOn;

    protected TransactionEntity() {
        // exigido pelo JPA
    }

    public TransactionEntity(Long id, BigDecimal amount, String description,
                             String place, Category category, LocalDate occurredOn) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.place = place;
        this.category = category;
        this.occurredOn = occurredOn;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getPlace() { return place; }
    public Category getCategory() { return category; }
    public LocalDate getOccurredOn() { return occurredOn; }
}