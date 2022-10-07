package internship.lhind.service;

import internship.lhind.model.dto.RestaurantDto;
import internship.lhind.model.entity.Status;
import internship.lhind.repository.RestaurantRepository;
import internship.lhind.repository.RoleRepository;
import internship.lhind.repository.StatusRepository;
import internship.lhind.service.impl.MenuServiceImpl;
import internship.lhind.service.impl.RestaurantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

class RestaurantServiceImplTest {
    private RestaurantRepository restaurantRepository;
    private StatusRepository statusRepository;
    private RestaurantServiceImpl restaurantService;

    @BeforeEach
    void setUp(){
        restaurantRepository = Mockito.mock(RestaurantRepository.class);
        UserService userService = Mockito.mock(UserService.class);
        RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
        statusRepository = Mockito.mock(StatusRepository.class);
        MenuServiceImpl menuService = Mockito.mock(MenuServiceImpl.class);
        restaurantService = new RestaurantServiceImpl(restaurantRepository
                , userService, roleRepository,statusRepository, menuService);
    }
    @Test
    void returns_restaurant_dto_when_saved_successfully() throws Exception{
        RestaurantDto restaurantToBeSaved = new RestaurantDto();
        restaurantToBeSaved.setName("restaurant");
        restaurantToBeSaved.setId(null);

        Status status = new Status();
        status.setStatus("VALID");

        Mockito.when(restaurantRepository.findRestaurantByName(any())).thenReturn(null);
        Mockito.when(statusRepository.findStatusByStatus(any())).thenReturn(status);

        RestaurantDto savedRestaurant = restaurantService.save(restaurantToBeSaved);
        assertEquals(savedRestaurant.getName(), restaurantToBeSaved.getName());
    }
}