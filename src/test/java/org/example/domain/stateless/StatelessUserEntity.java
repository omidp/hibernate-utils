package org.example.domain.stateless;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.domain.id.UserEntity;
import org.example.internal.id.AbstractEntity;
import org.hibernate.envers.Audited;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StatelessUserEntity  {

	@GeneratedValue
	@Id
	private UUID id;
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "referral_id")
	private StatelessUserEntity referral;

	@OneToMany(mappedBy = "user")
	private List<StatelessPaymentEntity> payments;



}