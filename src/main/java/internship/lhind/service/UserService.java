package internship.lhind.service;

import internship.lhind.model.dto.RoleDto;
import internship.lhind.model.dto.UserDto;
import internship.lhind.model.entity.User;

import java.util.List;
public interface UserService {
    UserDto save(UserDto userDto) throws Exception;
    UserDto saveFromRepository(User user);
    UserDto findById(Integer id);
    User findByIdd(Integer id);

    List<UserDto> findAllWithoutAdmin();

    List<UserDto> findAllByRole(String role) throws Exception;

    List<UserDto> findAll();
    List<UserDto> findAllValid();
    Integer nrOfUsers();
    void deleteById(Integer id);
    User setRoles(User user, RoleDto roleDto) throws Exception;
    UserDto findUserByUserName(String username);
    User findByUserName(String username);

}
