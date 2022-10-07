package internship.lhind.service;

import internship.lhind.model.entity.User;
import internship.lhind.model.entity.UserDetails;

import java.util.List;
import java.util.Optional;
public interface UserDetailsService {
    UserDetails save(UserDetails u);
    Optional<UserDetails> findById(Integer id);
    UserDetails findByUser(User user);
    List<UserDetails> findFirstByFirstName(String fname);
    List<UserDetails> findFirstByEmail(String email);
    List<UserDetails> findFirstByPhoneNumber(String phone);
}
