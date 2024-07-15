package org.example.internal.map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_item")
@Data
public class OrderItemEntity {

	@Id
	private UUID id;
	private BigDecimal price;
	@Enumerated(EnumType.STRING)
	private Category category;

	@ElementCollection
	@CollectionTable(
		name = "order_attributes",
		joinColumns = @JoinColumn(name = "order_item_id")
	)
	@MapKeyEnumerated(EnumType.STRING)
	@Column(name = "attribute_value")
	private Map<ProductAttribute, String> attributes = new HashMap<>();

}
