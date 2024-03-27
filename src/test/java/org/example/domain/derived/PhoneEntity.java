package org.example.domain.derived;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneEntity {
	@Id private long id;
	private String number;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id", insertable = false, updatable = false)
	private PersonEntity person;

	@Column(name = "person_id")
	private Long personId;

}
