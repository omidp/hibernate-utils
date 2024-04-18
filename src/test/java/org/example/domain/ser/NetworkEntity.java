package org.example.domain.ser;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class NetworkEntity {

	@Id private UUID id;
	@JdbcTypeCode(SqlTypes.JSON)
	private ConfigRuleDefinitionList rules;

}