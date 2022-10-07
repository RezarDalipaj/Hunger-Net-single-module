package internship.lhind.service;

import internship.lhind.model.dto.RestaurantDto;
import internship.lhind.model.entity.Restaurant;

import java.util.List;

public interface RestaurantService {
    Restaurant findByManager(String username);

    RestaurantDto findById(Integer id);

    RestaurantDto findByIdForManager(Integer id);

    Restaurant findByIdd(Integer id);

    RestaurantDto save(RestaurantDto restaurantDto) throws Exception;
    List<RestaurantDto> findAll();
    List<RestaurantDto> findAllValid();

    List<RestaurantDto> findAllValidForAdmin();

    Integer nrOfRestaurants();
    RestaurantDto findRestaurantByName(String name);
    Restaurant findByName(String name);
    RestaurantDto findRestaurantByManager(String username);

    RestaurantDto saveFromRepository(Restaurant restaurant);

    void deleteById(Integer id);

    List<RestaurantDto> deleteAll();
}
