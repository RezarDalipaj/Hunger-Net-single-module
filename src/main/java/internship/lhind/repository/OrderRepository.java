package internship.lhind.repository;

import internship.lhind.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Integer> {
    @Query(value = "SELECT order_id FROM hunger_net.order WHERE status_id = :status_id and menu_id " +
            "in(select id from hunger_net.menu where restaurant_id = :restaurant_id)", nativeQuery = true)
    List<Integer> filterOrderByStatus(@Param("status_id") Integer idStatus, @Param("restaurant_id") Integer idRestaurant);
}
