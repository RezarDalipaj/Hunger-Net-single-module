package internship.lhind.repository;

import internship.lhind.model.entity.Role;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findRoleByRole(String role);
    void deleteById(Integer id);
    @Override
    void deleteAll();
}
