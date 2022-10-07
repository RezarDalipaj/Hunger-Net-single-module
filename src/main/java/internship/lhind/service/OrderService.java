package internship.lhind.service;

import internship.lhind.model.entity.Order;
import internship.lhind.model.dto.OrderDto;
import internship.lhind.model.dto.OrderStatusDto;

import java.util.List;

public interface OrderService {
    Order findByIdValid(Integer id);

    OrderDto findValidById(Integer id);

    OrderDto deleteByIdUser(Integer id) throws Exception;

    List<OrderDto> findOrdersOfAUser(Integer id);

    List<OrderDto> findOrdersOfARestaurantByStatus(String status, Integer id) throws Exception;

    List<OrderDto> findOrdersOfARestaurant(Integer id);

    OrderDto deleteByIdManager(Integer id);

    OrderDto updateStatus(OrderStatusDto orderStatusDto) throws Exception;

    Order findById(Integer id);

    Order findByIdCreated(Integer id);

    OrderDto save(OrderDto orderDto) throws Exception;
}
