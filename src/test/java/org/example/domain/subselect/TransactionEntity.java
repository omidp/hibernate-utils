package org.example.domain.subselect;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@NoArgsConstructor
@Data
public class TransactionEntity {

	@Id
	@GeneratedValue
	private UUID id;

	private BigDecimal amount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private CustomerEntity customer;

	public TransactionEntity(BigDecimal amount, CustomerEntity customer) {
		this.amount = amount;
		this.customer = customer;
	}
}
