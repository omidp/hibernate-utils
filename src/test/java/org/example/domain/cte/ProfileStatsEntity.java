package org.example.domain.cte;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Table(name = "profile_stats")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileStatsEntity {

	@Id
	private UUID id;

	private long viewed;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_id")
	private ProfileEntity profile;

}
