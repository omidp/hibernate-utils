package org.example.domain.root;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemVO(UUID orderId, UUID itemId, BigDecimal amount, String name) {
}
