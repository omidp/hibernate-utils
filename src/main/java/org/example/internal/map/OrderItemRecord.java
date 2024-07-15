package org.example.internal.map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRecord {

	private Category category;
	private BigDecimal price;
//	private Map<ProductAttribute, String> attributes = new HashMap<>();
}
