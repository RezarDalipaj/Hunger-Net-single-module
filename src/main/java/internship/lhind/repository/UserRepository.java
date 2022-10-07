package internship.lhind.repository;

import internship.lhind.model.entity.User;
import internship.lhind.model.entity.UserDetails;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findUserByUserName(String username);
    User findAllByUserDetails(UserDetails ud);
    void deleteUserById(Integer id);
    @Override
    void deleteAll();
    @Query(value = "SELECT role_id FROM user_roles WHERE user_id = :id", nativeQuery = true)
    List<Integer> findRolesOfAUser(@Param("id") Integer id);
}
