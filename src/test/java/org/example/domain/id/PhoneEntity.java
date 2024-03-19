package org.example.domain.id;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.internal.id.AbstractEntity;

@Data
@Entity
@Table(name = "phone")
@NoArgsConstructor
public class PhoneEntity extends AbstractEntity<Long> {

	private String name;

}