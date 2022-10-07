package internship.lhind.controller;

import internship.lhind.configuration.security.config.JwtTokenUtil;
import internship.lhind.configuration.security.model.JwtRequest;
import internship.lhind.configuration.security.model.JwtResponse;
import internship.lhind.configuration.security.service.JwtUserDetailsService;
import internship.lhind.model.entity.*;
import internship.lhind.model.dto.AdminDto;
import internship.lhind.model.dto.RoleDto;
import internship.lhind.model.dto.UserDto;
import internship.lhind.repository.RoleRepository;
import internship.lhind.service.UserService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
@ComponentScan(basePackages = {"backend"})
public class JwtAuthenticationController {
	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final JwtUserDetailsService userDetailsService;
	private final RoleRepository roleRepository;

	public JwtAuthenticationController(UserService userService
			, AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil
			, JwtUserDetailsService userDetailsService, RoleRepository roleRepository) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.jwtTokenUtil = jwtTokenUtil;
		this.userDetailsService = userDetailsService;
		this.roleRepository = roleRepository;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
		jwtTokenUtil.setLoggedOut(false);
		authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(authenticationRequest.getUsername());

		final String token = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new JwtResponse(token));
	}
	public String usernameFromToken(HttpServletRequest request){
		String token = request.getHeader("Authorization").substring(7);
		return jwtTokenUtil.getUsernameFromToken(token);
	}
	public boolean isAdmin(HttpServletRequest request){
		String username = usernameFromToken(request);
		User user = userService.findByUserName(username);
		Role role = roleRepository.findRoleByRole("ADMIN");
		return user.getRoles().contains(role);
	}
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public ResponseEntity<?> saveUser(@RequestBody UserDto userDto) throws Exception {
		userDto.setId(null);
		return ResponseEntity.ok(userDetailsService.save(userDto));
	}
	@RequestMapping(value = "/users/{id}", method = RequestMethod.PUT)
	public ResponseEntity<?> updateUser(@PathVariable(name = "id") Integer id
			, @RequestBody UserDto userDto, HttpServletRequest request) throws Exception{
		if (!(usernameFromToken(request)).equals(userService.findById(id).getUserName()))
			throw new AuthenticationException();
		userDto.setId(id);
		return ResponseEntity.ok(userDetailsService.put(userDto));
	}
    @RequestMapping(value = "/users/{id}/update", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> update(@PathVariable(name = "id") Integer id
            ,@RequestBody AdminDto adminDto) throws Exception {
        adminDto.setId(id);
        return ResponseEntity.ok(userDetailsService.putAdmin(adminDto));
    }
	@PutMapping("/users/{id}/update/role")
	@PreAuthorize("hasAuthority('ADMIN')")
	public UserDto changeRoles(@PathVariable(name = "id") Integer id
			, @RequestBody RoleDto roleDto) throws Exception{
		User user = userService.findByIdd(id);
		user = userService.setRoles(user,roleDto);
		return userService.saveFromRepository(user);
	}
	private void authenticate(String username, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}
}