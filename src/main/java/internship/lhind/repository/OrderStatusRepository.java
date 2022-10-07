package internship.lhind.repository;

import internship.lhind.model.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus,Integer> {
    OrderStatus findOrderStatusByOrderStatus(String orderStatus);
}
