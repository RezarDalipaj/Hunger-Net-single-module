package internship.lhind.repository;

import internship.lhind.model.entity.Restaurant;
import internship.lhind.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
    Restaurant findRestaurantByName(String name);
    @Query(value = "SELECT id FROM restaurant WHERE manager_id = :id and status_id != 3", nativeQuery = true)
    Integer findRestaurantByUser(@Param("id") Integer id);
    Restaurant findRestaurantByManager(User user);
}
