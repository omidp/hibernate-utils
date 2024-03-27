package org.example.domain.cte;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "customer")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerEntity {

	@Id
	private Long id;

	private String name;

}
