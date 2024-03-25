package org.example.domain.subselect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Subselect(value = """
select c.id as customer_id, c.name, t.amount from customer c 
left join transaction t on c.id = t.customer_id 
""")
@Synchronize({"customer", "transaction"})
@Data
public class CustomerTransactionView {

	@Id
	@Column(name = "customer_id")
	private UUID customerId;

	private String name;

	private BigDecimal amount;

}
