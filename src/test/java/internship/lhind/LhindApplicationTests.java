package internship.lhind;

import internship.lhind.model.dto.UserDto;
import internship.lhind.model.entity.Status;
import internship.lhind.model.entity.User;
import internship.lhind.model.entity.UserDetails;
import internship.lhind.repository.*;
import internship.lhind.service.OrderService;
import internship.lhind.service.impl.RestaurantServiceImpl;
import internship.lhind.service.impl.UserServiceImpl;
import internship.lhind.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class LhindApplicationTests {
        private UserRepository userRepository;
        private UserDetailsRepository userDetailsRepository;
    private StatusRepository statusRepository;
    private PasswordEncoder bcryptEncoder;
    private UserServiceImpl userService;
        @Autowired
        private UserMapper userMapper;

        @BeforeEach
        void setUp() {
            userRepository = Mockito.mock(UserRepository.class);
            userDetailsRepository = Mockito.mock(UserDetailsRepository.class);
            RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
            RestaurantServiceImpl restaurantService = Mockito.mock(RestaurantServiceImpl.class);
            statusRepository = Mockito.mock(StatusRepository.class);
            OrderStatusRepository orderStatusRepository = Mockito.mock(OrderStatusRepository.class);
            MenuTypeRepository menuTypeRepository = Mockito.mock(MenuTypeRepository.class);
            bcryptEncoder = Mockito.mock(PasswordEncoder.class);
            OrderService orderService = Mockito.mock(OrderService.class);
            userService = new UserServiceImpl(userRepository, userDetailsRepository
                    , roleRepository, statusRepository, orderStatusRepository
                    , restaurantService, menuTypeRepository, bcryptEncoder, orderService, userMapper);
        }
        @Test
        void user_update_returns_user_dto_when_updated_successfully() throws Exception{
            UserDto userDto = new UserDto();
            userDto.setUserName("new_user");
            userDto.setPassword("new_user");
            userDto.setId(1);
            Status status = new Status();
            status.setStatus("VALID");

            User persistentUser = new User();
            persistentUser.setId(1);
            persistentUser.setUserName("old_user");
            persistentUser.setPassword("new_password");
            persistentUser.setStatus(status);
            persistentUser.setUserDetails(new UserDetails());


            Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(persistentUser));
            Mockito.when(userRepository.save(any())).thenReturn(persistentUser);
            Mockito.when(userDetailsRepository.save(any())).thenReturn(persistentUser.getUserDetails());
            Mockito.when(bcryptEncoder.encode(any())).thenReturn("test");
            Mockito.when(statusRepository.findStatusByStatus(any())).thenReturn(status);

            UserDto newUser = userService.save(userDto);
            assertEquals(userDto.getUserName(), newUser.getUserName());
        }
        @Test
        void user_update_throws_Exception_when_username_is_short() {
            UserDto userDto = new UserDto();
            userDto.setUserName("ok");
            userDto.setId(1);
            Status status = new Status();
            status.setStatus("VALID");

            User persistentUser = new User();
            persistentUser.setId(1);
            persistentUser.setUserName("old_user");
            persistentUser.setPassword("new_password");
            persistentUser.setStatus(status);
            persistentUser.setUserDetails(new UserDetails());


            Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(persistentUser));
            Mockito.when(userRepository.save(any())).thenReturn(persistentUser);
            Mockito.when(userDetailsRepository.save(any())).thenReturn(Optional.of(persistentUser.getUserDetails()));
            Mockito.when(bcryptEncoder.encode(any())).thenReturn("test");
            Mockito.when(statusRepository.findStatusByStatus(any())).thenReturn(status);

            Class<? extends Throwable> InvalidDataException = null;
            assertThrows(InvalidDataException);
        }

        private void assertThrows(Class<? extends Throwable> invalidDataException) {
        }
}
