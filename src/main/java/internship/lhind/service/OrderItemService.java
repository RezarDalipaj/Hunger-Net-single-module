package internship.lhind.service;

import internship.lhind.model.entity.OrderItem;
import internship.lhind.model.dto.OrderItemDto;

public interface OrderItemService {
    OrderItemDto convertToOrderItemDto(OrderItem orderItem);

    OrderItem findById(Integer id);
}
