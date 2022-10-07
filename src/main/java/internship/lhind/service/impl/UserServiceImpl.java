package internship.lhind.service.impl;

import internship.lhind.customException.InvalidDataException;
import internship.lhind.model.dto.RoleDto;
import internship.lhind.model.dto.UserDto;
import internship.lhind.model.entity.*;
import internship.lhind.repository.*;
import internship.lhind.service.OrderService;
import internship.lhind.service.UserService;
import internship.lhind.util.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final RoleRepository roleRepository;
    private final StatusRepository statusRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final RestaurantServiceImpl restaurantService;
    private final MenuTypeRepository menuTypeRepository;
    private final PasswordEncoder bcryptEncoder;
    private final OrderService orderService;
    private final UserMapper userMapper;
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    public UserServiceImpl(UserRepository userRepository, UserDetailsRepository userDetailsRepository
            , RoleRepository roleRepository, StatusRepository statusRepository
            , OrderStatusRepository orderStatusRepository, @Lazy RestaurantServiceImpl restaurantService
            , MenuTypeRepository menuTypeRepository, PasswordEncoder bcryptEncoder, @Lazy OrderService orderService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.roleRepository = roleRepository;
        this.statusRepository = statusRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.restaurantService = restaurantService;
        this.menuTypeRepository = menuTypeRepository;
        this.bcryptEncoder = bcryptEncoder;
        this.orderService = orderService;
        this.userMapper = userMapper;
    }
    //saving or updating user
    public UserDto save(UserDto userDto) throws Exception {
            User user;
            if (userDto.getId() == null)
                user = convertDtoToUserAdd(userDto);
            else
                user = convertDtoToUserUpdate(userDto);
            user.setPassword(bcryptEncoder.encode(user.getPassword()));
            Status status = statusRepository.findStatusByStatus("VALID");
            user.setStatus(status);
            user.getUserDetails().setStatus(status);
            userDetailsRepository.save(user.getUserDetails());
            userRepository.save(user);
            logger.info("Saved User {} in database", user.getUserName());
            return userMapper.convertUserToDto(user);
    }
    public UserDto saveFromRepository(User user){
        Status status = statusRepository.findStatusByStatus("VALID");
        user.setStatus(status);
        user = userRepository.save(user);
        return userMapper.convertUserToDto(user);
    }
    @Override
    public UserDto findById(Integer id) {
        Optional <User> user = userRepository.findById(id);
        if (user.isPresent())
            return userMapper.convertUserToDto(user.get());
        else
            throw new NullPointerException("USER WITH ID " + id);
    }
    @Override
    public User findByIdd(Integer id) {
        Optional <User> user = userRepository.findById(id);
        if (user.isPresent())
            return user.get();
        else
            throw new NullPointerException("USER WITH ID " + id);
    }
    //all users
    public List<UserDto> findAllValid(){
        return userRepository.findAll().stream().map(userMapper::convertUserToDto).collect(Collectors.toList());
    }
    //all users without admins
    @Override
    public List<UserDto> findAllWithoutAdmin(){
        Role role1 = roleRepository.findRoleByRole("RESTAURANT_MANAGER");
        Role role2 = roleRepository.findRoleByRole("CLIENT");
        Role roleAdmin = roleRepository.findRoleByRole("ADMIN");
        Collection<User> managers;
        Collection<User> clients;
        if (role1.getUsers() == null && role2.getUsers() == null)
            return new ArrayList<>();
        managers = role1.getUsers();
        clients = role2.getUsers();
        managers.addAll(clients);
        managers.removeIf(user -> user.getRoles().contains(roleAdmin));
        return managers.stream().map(userMapper::convertUserToDto).collect(Collectors.toList());
    }
    @Override
    public List<UserDto> findAllByRole(String role) throws Exception{
        Role role1 = roleRepository.findRoleByRole(role);
        Role role2 = roleRepository.findRoleByRole("ADMIN");
        if (role1 == null)
            throw new NullPointerException("ROLE " + role);
        if (role1.getRole().equals("ADMIN"))
            throw new InvalidDataException("Cannot view users with role ADMIN");
        if (role1.getUsers() == null)
            throw new NullPointerException("USERS WITH ROLE" + role);
        Collection<User> userCollection = new ArrayList<>();
        for (User user: role1.getUsers()) {
            if (!user.getRoles().contains(role2))
                userCollection.add(user);
        }
        return userCollection.stream().map(userMapper::convertUserToDto).collect(Collectors.toList());

    }
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(userMapper::convertUserToDto).collect(Collectors.toList());
    }
    @Override
    public Integer nrOfUsers(){
        return findAll().size();
    }
    @Override
    public void deleteById(Integer id) {
        User user = findByIdd(id);
        deletePossibleRestaurants(user);
        deleteOrdersOfAUser(user);
        removeUserFromPreviousRoles(user);
        userRepository.deleteById(id);
    }
    public void deletePossibleRestaurants(User user){
        if (user.getRestaurant()!=null){
            Restaurant restaurant = restaurantService.findByManager(user.getUserName());
            restaurant.setManager(null);
            restaurantService.saveFromRepository(restaurant);
        }
    }
    private void deleteOrdersOfAUser(User user){
        if (user.getOrders() != null) {
            for (Order order : user.getOrders()) {
                if (!order.getStatus().getStatus().equals("DELIVERED")
                        && !order.getStatus().getStatus().equals("REJECTED")
                        && !order.getStatus().getStatus().equals("DELETED"))
                    orderService.deleteByIdManager(order.getOrder_id());
            }
        }
    }
    public UserDto convertUserToDto(User user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUserName(user.getUserName());
        userDto.setPassword(user.getPassword());
        userDto.setBalance(user.getBalance());
        if (user.getUserDetails()!=null){
            userDto.setFirstName(user.getUserDetails().getFirstName());
            userDto.setLastName(user.getUserDetails().getLastName());
            userDto.setEmail(user.getUserDetails().getEmail());
            userDto.setPhoneNumber(user.getUserDetails().getPhoneNumber());
        }
        return getRoles(user,userDto);
    }
    public UserDto getRoles(User u, UserDto userDTO){
        Collection<Role> roleCollection = u.getRoles();
        if (roleCollection == null)
            return userDTO;
        List<String> roles = new ArrayList<>();
        for (Role role:roleCollection) {
            String role1 = role.getRole();
            roles.add(role1);
        }
        userDTO.setRoles(roles);
        return userDTO;
    }
    public User convertDtoToUserAdd(UserDto userDto) throws InvalidDataException {
        User user = new User();
        if (userRepository.findUserByUserName(userDto.getUserName())!=null)
            throw new InvalidDataException("Username already exists");
        if (userDto.getUserName() == null || userDto.getPassword() == null
                || userDto.getFirstName() == null || userDto.getLastName() == null
                || userDto.getEmail() == null) {
            logger.warn("Couldn't add user");
            throw new InvalidDataException("All fields are required");
        }
        checkBalance(user,userDto);
        user = setUserAdd(user, userDto);
        return setRolesUser(user);
    }
    public void checkBalance(User user, UserDto userDto) throws InvalidDataException {
            if (userDto.getBalance() == null)
                throw new InvalidDataException("Balance cannot be null");
            if (userDto.getBalance()<=0)
                user.setBalance(0D);
            else
                user.setBalance(userDto.getBalance());
    }
    public User convertDtoToUserUpdate(UserDto userDto) throws InvalidDataException {
        User user = findByIdd(userDto.getId());
        if (userDto.getUserName() == null)
            return setUserUpdate(user,userDto);
        if (user.getUserName().equals(userDto.getUserName())){
            return setUserUpdate(user, userDto);
        }
        else if (findUserByUserName(userDto.getUserName()) != null) {
            logger.warn("Couldn't update user");
            throw new InvalidDataException("Username already exists");
        }
        else
            return setUserUpdate(user, userDto);
    }
    //creating statuses if deleted
    public void createStatusIfNecessary(){
        if (statusRepository.findAll().size()!=3){
            if (!statusRepository.findAll().isEmpty())
                statusRepository.deleteAll();
            logger.warn("Status Db was changed");
            List<String> statusList = List.of("VALID","INVALID","DELETED");
            for (String s : statusList) {
                Status status = new Status();
                status.setStatus(s);
                statusRepository.save(status);
            }
        }
    }
    public void createRolesIfNecessary(){
        if (roleRepository.findAll().size()!=3){
            if (!roleRepository.findAll().isEmpty())
                roleRepository.deleteAll();
            logger.warn("Roles Db was changed");
            List<String> roles = List.of("ADMIN","RESTAURANT_MANAGER","CLIENT");
            for (String s : roles) {
                Role role = new Role();
                role.setRole(s);
                roleRepository.save(role);
            }
        }
    }
    public void createOrderStatusIfNecessary(){
        if (orderStatusRepository.findAll().size()!=7){
            if (!orderStatusRepository.findAll().isEmpty())
                orderStatusRepository.deleteAll();
            logger.warn("OrderStatus Db was changed");
            List<String> statusList = List.of("CREATED","APPROVED","REJECTED","PREPARED",
                    "WAITING_FOR_DELIVERY","DELIVERED", "DELETED");
            for (String s : statusList) {
                OrderStatus orderStatus = new OrderStatus();
                orderStatus.setStatus(s);
                orderStatusRepository.save(orderStatus);
            }
        }
    }
    public void createMenuTypeIfNecessary(){
        if (menuTypeRepository.findAll().size()!=4){
            if (!menuTypeRepository.findAll().isEmpty())
                menuTypeRepository.deleteAll();
            logger.warn("MenuType Db was changed");
            List<String> menuTypeList = List.of("BREAKFAST","LUNCH","AFTERNOON","DINNER");
            for (String s : menuTypeList) {
                MenuType menuType = new MenuType();
                menuType.setMenuType(s);
                menuTypeRepository.save(menuType);
            }
        }
    }
    //checking if there is an admin
    public Boolean adminExists(){
        List<UserDto> userDtoList = findAll();
        if (userDtoList.isEmpty())
            return false;
        for (UserDto userDto : userDtoList) {
            if (userDto.getRoles() == null)
                continue;
            for (int j = 0; j < userDto.getRoles().size(); j++) {
                if (userDto.getRoles().get(j).equals("ADMIN"))
                    return true;
            }
        }
        logger.warn("ADMIN doesnt exist");
        return false;
    }
    //setting roles to a user who just signed up
    public User setRolesUser(User user){
        createStatusIfNecessary();
        createRolesIfNecessary();
        createOrderStatusIfNecessary();
        createMenuTypeIfNecessary();
        Role role;
        if (!adminExists() && (user.getUserName().equals("rezari")
                || userRepository.findUserByUserName("rezari") != null))
            role = roleRepository.findRoleByRole("ADMIN");
        else
            role = roleRepository.findRoleByRole("CLIENT");
        addUsersToRoles(user, role);
        Collection<Role> roleCollection = new ArrayList<>();
        user.setRoles(roleCollection);
        user.getRoles().add(role);
        userRepository.save(user);
        return user;
    }
    private void addUsersToRoles(User user, Role role) {
        if (role.getUsers() == null) {
            Collection<User> userCollection = new ArrayList<>();
            role.setUsers(userCollection);
            role.getUsers().add(user);
        } else {
            if (!(role.getUsers().contains(user)))
                role.getUsers().add(user);
        }
    }
    //updating roles by admin
    public User setRoles(User user, RoleDto roleDto) throws Exception{
        List<String> roles;
        if (roleDto.getRoles() != null && !roleDto.getRoles().isEmpty()) {
            removeUserFromPreviousRoles(user);
            roles = roleDto.getRoles();
            Collection<Role> roleCollection = new ArrayList<>();
            addRoles(roles, user, roleCollection);
            return saveRoles(user, roleCollection);
        }
        else
            throw new InvalidDataException("Please add a role to the user");
    }
    //removing previous roles from user when updating roles
    public void removeUserFromPreviousRoles(User user){
        Collection<Role> roleCollection2 = user.getRoles();
        for (Role r:roleCollection2) {
            r.getUsers().remove(user);
        }
    }
    public void addRoles(List<String> roles, User user, Collection<Role> roleCollection) throws InvalidDataException{
        for (String string : roles) {
            Role role1 = roleRepository.findRoleByRole(string);
            if (role1 == null)
                throw new InvalidDataException("Role does not exist");
            addUsersToRoles(user, role1);
            roleCollection.add(role1);
            roleRepository.save(role1);
        }
    }
    public User saveRoles(User user, Collection<Role> roleCollection){
        Collection<Role> roleCollection1 = new ArrayList<>();
        user.setRoles(roleCollection1);
        user.getRoles().addAll(roleCollection);
        Restaurant restaurant = restaurantService.findByManager(user.getUserName());
        //deleting restaurant of a user who is no longer a manager
        Role role = roleRepository.findRoleByRole("RESTAURANT_MANAGER");
        if (restaurant != null && !user.getRoles().contains(role))
        {
            restaurant.setManager(null);
            restaurantService.saveFromRepository(restaurant);
        }
        userRepository.save(user);
        return user;
    }
    public User setUserAdd(User user1, UserDto userDTO) throws InvalidDataException {
            setUser(user1, userDTO);
            UserDetails userDetails = new UserDetails();
            setUserDetails(user1,userDTO,userDetails);
            user1.setUserDetails(userDetails);
            saveFromRepository(user1);
            userDetailsRepository.save(userDetails);
            return user1;
    }
    public void setUser(User user, UserDto userDTO) throws InvalidDataException {
        if (userDTO.getBalance() != null)
            checkBalance(user,userDTO);
        if (userDTO.getUserName() != null) {
            if (userDTO.getUserName().length()<3)
                throw new InvalidDataException("Username cannot be that short");
            user.setUserName(userDTO.getUserName());
        }
        if (userDTO.getPassword() != null)
            user.setPassword(userDTO.getPassword());
    }
    public void setUserDetails(User user1, UserDto userDTO, UserDetails userDetails){
        userDetails.setFirstName(userDTO.getFirstName());
        userDetails.setLastName(userDTO.getLastName());
        userDetails.setEmail(userDTO.getEmail());
        userDetails.setPhoneNumber(userDTO.getPhoneNumber());
        userDetails.setTheUser(user1);
        Status status = statusRepository.findStatusByStatus("VALID");
        userDetails.setStatus(status);
    }
    public User setUserUpdate(User user, UserDto userDTO) throws InvalidDataException {
        setUser(user,userDTO);
        return setUserDetailsUpdate(user,userDTO);
    }
    public User setUserDetailsUpdate(User user1, UserDto userDTO){
        if (user1.getUserDetails() != null){
            if (userDTO.getFirstName() != null)
                user1.getUserDetails().setFirstName(userDTO.getFirstName());
            if (userDTO.getLastName() != null)
                user1.getUserDetails().setLastName(userDTO.getLastName());
            if (userDTO.getEmail() != null)
                user1.getUserDetails().setEmail(userDTO.getEmail());
            if (userDTO.getPhoneNumber() != null)
                user1.getUserDetails().setPhoneNumber(userDTO.getPhoneNumber());
        }
        saveFromRepository(user1);
        userDetailsRepository.save(user1.getUserDetails());
        return user1;
    }
    //finding user by username
    @Override
    public UserDto findUserByUserName(String username) {
        User user = userRepository.findUserByUserName(username);
        if(user != null)
            return userMapper.convertUserToDto(user);
        else
            return null;
    }

    @Override
    public User findByUserName(String username) {
        return userRepository.findUserByUserName(username);
    }
}
