package org.example.domain.assignid;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.internal.id.CustomIdGenerator;

import java.util.UUID;

@Table(name = "merchants")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class MerchantEntity {

	@Id
	@CustomIdGenerator
	private UUID id;

	private String name;

}
