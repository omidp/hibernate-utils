package org.example.domain.id;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.internal.id.AbstractEntity;
import org.example.internal.id.TransientEntity;
import org.hibernate.envers.Audited;

import java.util.UUID;

@Data
@Entity
@Table(name = "log")
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class LogEntity extends AbstractEntity<UUID> implements TransientEntity<UUID> {

	private String name;

	@Override
	public void setId(UUID uuid) {
		this.id = uuid;
	}
}