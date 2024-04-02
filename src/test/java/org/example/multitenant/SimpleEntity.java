package org.example.multitenant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name="simple_entity")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SimpleEntity {

	@Id
	private UUID id;

	private String name;

}
