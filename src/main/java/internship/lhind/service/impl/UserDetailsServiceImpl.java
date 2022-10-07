package internship.lhind.service.impl;

import internship.lhind.model.entity.User;
import internship.lhind.model.entity.UserDetails;
import internship.lhind.repository.UserDetailsRepository;
import internship.lhind.service.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    UserDetailsRepository userDetailsRepository;
    UserDetailsServiceImpl(UserDetailsRepository userDetailsRepository){
        this.userDetailsRepository = userDetailsRepository;
    }
    public UserDetails save(UserDetails u){
        return userDetailsRepository.save(u);
    }

    @Override
    public Optional<UserDetails> findById(Integer id) {
        return userDetailsRepository.findById(id);
    }

    @Override
    public UserDetails findByUser(User user) {
        return userDetailsRepository.findFirstByTheUser(user);
    }

    public List<UserDetails> findFirstByFirstName(String fname){
        return userDetailsRepository.findAllByFirstNameContainsIgnoreCase(fname);
    }

    @Override
    public List<UserDetails> findFirstByEmail(String email) {
        return userDetailsRepository.findAllByEmailContainsIgnoreCase(email);
    }

    public List<UserDetails> findFirstByPhoneNumber(String phone){
        return userDetailsRepository.findAllByPhoneNumberContainsIgnoreCase(phone);
    }

}
