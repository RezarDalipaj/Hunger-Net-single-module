package internship.lhind.util;

import internship.lhind.model.dto.UserDto;
import internship.lhind.model.entity.Role;
import internship.lhind.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserMapper {
    @Mapping(source = "user.userDetails.firstName", target = "firstName")
    @Mapping(source = "user.userDetails.lastName", target = "lastName")
    @Mapping(source = "user.userDetails.phoneNumber", target = "phoneNumber")
    @Mapping(source = "user.userDetails.email", target = "email")
    UserDto convertUserToDto(User user);
    default String rolesToString(Role role) {
        return role == null ? null : role.getRole();
    }
}
