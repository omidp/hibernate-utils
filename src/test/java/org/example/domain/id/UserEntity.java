package org.example.domain.id;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.internal.id.AbstractEntity;
import org.hibernate.envers.Audited;

import java.util.UUID;

@Data
@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class UserEntity extends AbstractEntity<UUID> {
	private String name;
}