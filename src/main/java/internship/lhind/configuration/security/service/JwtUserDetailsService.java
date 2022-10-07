package internship.lhind.configuration.security.service;

import internship.lhind.model.entity.Role;
import internship.lhind.model.dto.AdminDto;
import internship.lhind.model.dto.UserDto;
import internship.lhind.service.AdminService;
import internship.lhind.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class JwtUserDetailsService implements UserDetailsService {
	private final UserService userService;
	private final AdminService adminService;

	public JwtUserDetailsService(@Lazy UserService userService, AdminService adminService) {
		this.userService = userService;
		this.adminService = adminService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		internship.lhind.model.entity.User user = userService.findByUserName(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
			Collection<SimpleGrantedAuthority> authorityCollection = new ArrayList<>();
		if (user.getRoles() != null){
			for (Role role : user.getRoles())
				authorityCollection.add(new SimpleGrantedAuthority(role.getRole()));
		}
		else
			authorityCollection.add(new SimpleGrantedAuthority("CLIENT"));
		return new User(user.getUserName(), user.getPassword(), authorityCollection);
		}
	
	public UserDto save(UserDto user) throws Exception {
		return userService.save(user);
	}
	public UserDto put(UserDto user) throws Exception {
		return userService.save(user);
	}
	public AdminDto putAdmin(AdminDto user) throws Exception {
		return adminService.save(user);
	}
}