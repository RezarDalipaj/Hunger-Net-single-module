package internship.lhind.model.dto;

import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class RoleDto {
    private List<String> roles;

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
