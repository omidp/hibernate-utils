package org.example.domain.root;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderLineEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private BigDecimal amount;

	@Column(name = "order_id")
	private UUID orderId;


}
