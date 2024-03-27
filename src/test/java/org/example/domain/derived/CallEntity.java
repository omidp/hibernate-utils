package org.example.domain.derived;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallEntity {

	@Id private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId
	private PhoneEntity phone;

	private BigDecimal payment;

}
