package org.example.domain.assignid;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Table(name = "merchants")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class MerchantEntity {

	@Id
	@GeneratedValue
	private Integer id;

	private String name;

}
