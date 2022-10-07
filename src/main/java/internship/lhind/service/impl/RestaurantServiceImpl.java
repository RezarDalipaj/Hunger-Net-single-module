package internship.lhind.service.impl;

import internship.lhind.customException.InvalidDataException;
import internship.lhind.model.dto.MenuDto;
import internship.lhind.model.dto.RestaurantDto;
import internship.lhind.model.entity.*;
import internship.lhind.repository.RestaurantRepository;
import internship.lhind.repository.RoleRepository;
import internship.lhind.repository.StatusRepository;
import internship.lhind.service.RestaurantService;
import internship.lhind.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final StatusRepository statusRepository;
    private final MenuServiceImpl menuService;

    private final Logger logger = LoggerFactory.getLogger(RestaurantServiceImpl.class);
    public RestaurantServiceImpl(RestaurantRepository restaurantRepository, UserService userService
            , RoleRepository roleRepository, StatusRepository statusRepository
            , MenuServiceImpl menuService){
        this.restaurantRepository = restaurantRepository;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.statusRepository = statusRepository;
        this.menuService = menuService;
    }
    @Override
    public List<RestaurantDto> findAll(){
        return restaurantRepository.findAll().stream().map(this::convertRestaurantToDto).collect(Collectors.toList());
    }
    //restaurants will valid menus
    @Override
    public List<RestaurantDto> findAllValid(){
        Status status = statusRepository.findStatusByStatus("VALID");
        if (status.getRestaurants() == null)
            throw new NullPointerException("RESTAURANTS");
        List<Restaurant> restaurants = status.getRestaurants();
        return restaurants.stream().map(this::convertRestaurantToDto).collect(Collectors.toList());
    }
    //restaurants with all menus
    @Override
    public List<RestaurantDto> findAllValidForAdmin(){
        Status status = statusRepository.findStatusByStatus("VALID");
        if (status.getRestaurants() == null)
            throw new NullPointerException("RESTAURANTS");
        List<Restaurant> restaurants = status.getRestaurants();
        return restaurants.stream().map(this::convertToDtoForAdmin).collect(Collectors.toList());
    }
    @Override
    public Integer nrOfRestaurants() {
        return findAllValid().size();
    }

    @Override
    public RestaurantDto findRestaurantByName(String name) {
        Restaurant restaurant = restaurantRepository.findRestaurantByName(name);
        if (restaurant != null && restaurant.getStatus().getStatus().equals("VALID"))
            return convertRestaurantToDto(restaurantRepository.findRestaurantByName(name));
        else
            return null;
    }
    @Override
    public Restaurant findByName(String name) {
        Restaurant restaurant = restaurantRepository.findRestaurantByName(name);
        if (restaurant != null && restaurant.getStatus().getStatus().equals("VALID"))
            return restaurantRepository.findRestaurantByName(name);
        else
            return null;
    }

    @Override
    public RestaurantDto findRestaurantByManager(String username) {
        User user = userService.findByUserName(username);
        if (user == null)
            return null;
        Restaurant restaurant = restaurantRepository.findRestaurantByManager(user);
        if (restaurant == null || !restaurant.getStatus().getStatus().equals("VALID"))
            return null;
        return convertRestaurantToDto(restaurant);
    }
    //finding restaurant by manager
    @Override
    public Restaurant findByManager(String username) {
        User user = userService.findByUserName(username);
        if (user == null)
            return null;
        Integer id = restaurantRepository.findRestaurantByUser(user.getId());
        Restaurant restaurant;
        try {
            restaurant = findByIdd(id);
        }catch (Exception e){
            return null;
        }
        return restaurant;
    }
    //only active menus
    @Override
    public RestaurantDto findById(Integer id) {
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findById(id);
        if (optionalRestaurant.isPresent()){
            Restaurant restaurant = optionalRestaurant.get();
            if (restaurant.getStatus().getStatus().equals("VALID"))
                return convertRestaurantToDto(restaurant);
            else
                throw new NullPointerException("RESTAURANT WITH " + id);
        }
        else
            throw new NullPointerException("RESTAURANT WITH " + id);
    }
    //all menus
    @Override
    public RestaurantDto findByIdForManager(Integer id) {
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findById(id);
        if (optionalRestaurant.isPresent()){
            Restaurant restaurant = optionalRestaurant.get();
            if (restaurant.getStatus().getStatus().equals("VALID"))
                return convertToDtoForAdmin(restaurant);
            else
                throw new NullPointerException("RESTAURANT WITH ID" + id);
        }
        else
            throw new NullPointerException("RESTAURANT WITH ID" + id);
    }
    @Override
    public Restaurant findByIdd(Integer id) {
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findById(id);
        if (optionalRestaurant.isPresent()){
            Restaurant restaurant = optionalRestaurant.get();
            if (restaurant.getStatus().getStatus().equals("VALID"))
                return restaurant;
            else
                throw new NullPointerException("RESTAURANT WITH ID" + id);
        }
        else
            throw new NullPointerException("RESTAURANT WITH ID" + id);
    }
    //saving and updating restaurant
    @Override
    public RestaurantDto save(RestaurantDto restaurantDto) throws Exception{
        Restaurant restaurant;
        if (restaurantDto.getId()==null)
            restaurant = convertDtoToRestaurantAdd(restaurantDto);
        else
            restaurant = convertDtoToRestaurantUpdate(restaurantDto);
        Status status = statusRepository.findStatusByStatus("VALID");
        restaurant.setStatus(status);
        restaurantRepository.save(restaurant);
        return convertRestaurantToDto(restaurant);
    }
    @Override
    public RestaurantDto saveFromRepository(Restaurant restaurant){
        Status status = statusRepository.findStatusByStatus("VALID");
        restaurant.setStatus(status);
        restaurant = restaurantRepository.save(restaurant);
        return convertRestaurantToDto(restaurant);
    }
    //deleting menus of a deleted restaurant
    private void deletePossibleMenus(Restaurant restaurant){
            if (restaurant.getMenuList() != null) {
                List<Menu> menuList = restaurant.getMenuList();
                for (Menu menu : menuList) {
                    if (!menu.getStatus().getStatus().equals("DELETED")) {
                        logger.info("Deleting menu...");
                        menuService.deleteById(menu.getId());
                    }
                }
            }
        }
    @Override
    public void deleteById(Integer id) {
        Restaurant restaurant = findByIdd(id);
        Status status = statusRepository.findStatusByStatus("DELETED");
        deletePossibleMenus(restaurant);
        restaurant.setStatus(status);
        restaurantRepository.save(restaurant);
        logger.info("Deleted restaurant with id " + id);
    }
    @Override
    public List<RestaurantDto> deleteAll() {
        List<RestaurantDto> restaurantDtoList = findAllValid();
        Status statusValid = statusRepository.findStatusByStatus("VALID");
        List<Restaurant> restaurantList = statusValid.getRestaurants();
        Status status = statusRepository.findStatusByStatus("DELETED");
        for (Restaurant restaurant: restaurantList) {
            deletePossibleMenus(restaurant);
            restaurant.setStatus(status);
            restaurantRepository.save(restaurant);
        }
        logger.info("deleted all restaurants");
        return restaurantDtoList;
    }
    private RestaurantDto convertRestaurantToDto(Restaurant restaurant){
        RestaurantDto restaurantDto = new RestaurantDto();
        convertToDto(restaurant,restaurantDto);
        setMenuToDto(restaurant,restaurantDto);
        return restaurantDto;
    }
    private void convertToDto(Restaurant restaurant, RestaurantDto restaurantDto){
        restaurantDto.setId(restaurant.getId());
        if (restaurant.getManager() != null)
            restaurantDto.setManager(restaurant.getManager().getUserName());
        restaurantDto.setName(restaurant.getName());
    }
    private RestaurantDto convertToDtoForAdmin(Restaurant restaurant){
        RestaurantDto restaurantDto = new RestaurantDto();
        convertToDto(restaurant,restaurantDto);
        setMenuToDtoForAdmin(restaurant,restaurantDto);
        return restaurantDto;
    }
    //all menus
    private void setMenuToDtoForAdmin(Restaurant restaurant, RestaurantDto restaurantDto){
        try {
            if (restaurant.getMenuList() != null) {
                List<Menu> menuList = restaurant.getMenuList();
                List<MenuDto> menuDtoList = new ArrayList<>();
                for (Menu menu : menuList) {
                    menuService.validateMenu(menu);
                    if (!menu.getStatus().getStatus().equals("DELETED")){
                        MenuDto menuDto = menuService.convertMenuToDto(menu);
                        menuDtoList.add(menuDto);
                    }
                }
                restaurantDto.setMenuList(menuDtoList);
            }
        }catch (Exception e){
            restaurantDto.setMenuList(null);
        }
    }
    //active menus
    private void setMenuToDto(Restaurant restaurant, RestaurantDto restaurantDto){
        try {
            if (restaurant.getMenuList() != null) {
                List<Menu> menuList = restaurant.getMenuList();
                List<MenuDto> menuDtoList = new ArrayList<>();
                for (Menu menu : menuList) {
                    menuService.validateMenu(menu);
                    if (menu.getStatus().getStatus().equals("VALID")){
                        MenuDto menuDto = menuService.convertMenuToDto(menu);
                        menuDtoList.add(menuDto);
                    }
                }
                restaurantDto.setMenuList(menuDtoList);
            }
        }catch (Exception e){
            restaurantDto.setMenuList(null);
        }
    }
    private Restaurant setManager(RestaurantDto restaurantDto, Restaurant restaurant, User user) throws Exception {
        Role role = roleRepository.findRoleByRole("RESTAURANT_MANAGER");
        if (user==null)
            throw new InvalidDataException("User does not exist");
        if (!user.getRoles().contains(role))
            throw new InvalidDataException("User is not a manager");
        if (findByManager(user.getUserName()) != null)
            throw new InvalidDataException("Manager already has a restaurant");
        restaurant.setManager(userService.findByUserName(restaurantDto.getManager()));
        return restaurant;
    }
    private Restaurant setName(RestaurantDto restaurantDto, Restaurant  restaurant) throws Exception {
        if (restaurantDto.getName()==null)
            throw new InvalidDataException("Restaurant should have a name");
        if (restaurantRepository.findRestaurantByName(restaurantDto.getName()) != null)
            throw new InvalidDataException("Restaurant exists");
        if (restaurantDto.getName().length()<3)
            throw new InvalidDataException("Restaurant name cannot be that short");
        restaurant.setName(restaurantDto.getName());
        logger.info("Added new restaurant in db");
        return restaurant;
    }
    private Restaurant convertDtoToRestaurantAdd(RestaurantDto restaurantDto) throws Exception {
        Restaurant restaurant = new Restaurant();
        User user;
        if (restaurantDto.getManager() != null) {
            user = userService.findByUserName(restaurantDto.getManager());
            restaurant = setManager(restaurantDto, restaurant, user);
        }
        return setName(restaurantDto,restaurant);
    }
    public Restaurant convertDtoToRestaurantUpdate(RestaurantDto restaurantDto) throws Exception {
        Restaurant restaurant = findByIdd(restaurantDto.getId());
        if (restaurantDto.getManager() != null) {
            User user = userService.findByUserName(restaurantDto.getManager());
            if (user == null)
                throw new InvalidDataException("User doesnt exist");
            if (restaurant.getManager()!=null && restaurant.getManager().getUserName().equals(restaurantDto.getManager()))
                return setRestaurantUpdate(restaurant, restaurantDto);
            else {
                restaurant = setManager(restaurantDto,restaurant,user);
                return setRestaurantUpdate(restaurant, restaurantDto);
            }
        }
        else
            return setRestaurantUpdate(restaurant, restaurantDto);
    }

    private Restaurant setRestaurantUpdate(Restaurant restaurant, RestaurantDto restaurantDto) throws Exception {
        if (restaurantDto.getManager()!=null)
            restaurant.setManager(userService.findByUserName(restaurantDto.getManager()));
        if (restaurantDto.getName()!=null && !restaurant.getName().equals(restaurantDto.getName()))
            return setName(restaurantDto,restaurant);
        logger.info("Updated restaurant with id " + restaurant.getId());
        return restaurant;
    }
}
