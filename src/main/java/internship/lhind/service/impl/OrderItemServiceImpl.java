package internship.lhind.service.impl;

import internship.lhind.model.dto.OrderItemDto;
import internship.lhind.model.entity.OrderItem;
import internship.lhind.repository.OrderItemRepository;
import internship.lhind.service.OrderItemService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public OrderItemDto convertToOrderItemDto(OrderItem orderItem){
        OrderItemDto orderItemDto = new OrderItemDto();
        if (orderItem.getItem()!=null)
            orderItemDto.setItemName(orderItem.getItem().getName());
        if (orderItem.getItem()!=null && orderItem.getItem().getMenu() != null)
            orderItemDto.setMenu(orderItem.getItem().getMenu().getName());
        if (orderItem.getItem()!=null && orderItem.getItem().getMenu() != null
                && orderItem.getItem().getMenu().getRestaurant() != null)
            orderItemDto.setRestaurant(orderItem.getItem().getMenu().getRestaurant().getName());
        if (orderItem.getItem()!=null && orderItem.getItem().getMenu() != null)
            orderItemDto.setMenuType(orderItem.getItem().getMenu().getMenuType().getMenuType());
        orderItemDto.setQuantity(orderItem.getQuantity());
        return orderItemDto;
    }
    @Override
    public OrderItem findById(Integer id){
        Optional<OrderItem> optionalOrderItem = orderItemRepository.findById(id);
        return optionalOrderItem.orElse(null);
    }
}
