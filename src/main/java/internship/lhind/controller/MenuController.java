package internship.lhind.controller;

import internship.lhind.customException.InvalidDataException;
import internship.lhind.model.dto.MenuDto;
import internship.lhind.model.entity.Menu;
import internship.lhind.model.entity.Restaurant;
import internship.lhind.service.MenuService;
import internship.lhind.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/menu")
public class MenuController {
    private final JwtAuthenticationController authController;
    private final MenuService menuService;
    private final RestaurantService restaurantService;

    public MenuController(JwtAuthenticationController authController, MenuService menuService
            , RestaurantService restaurantService) {
        this.authController = authController;
        this.menuService = menuService;
        this.restaurantService = restaurantService;
    }
    public Restaurant auth(MenuDto menuDto) throws Exception{
        if (menuDto.getRestaurant() == null)
            throw new InvalidDataException("Menu should have a restaurant");
        Restaurant restaurant = restaurantService.findByName(menuDto.getRestaurant());
        if (restaurant == null || !restaurant.getStatus().getStatus().equals("VALID"))
            throw new InvalidDataException("Restaurant with name " + menuDto.getRestaurant() + " does not exist");
        return restaurant;
    }

    @PostMapping
    public ResponseEntity<?> saveMenu(@RequestBody MenuDto menuDto, HttpServletRequest request) throws Exception{
        String username = authController.usernameFromToken(request);
        menuDto.setId(null);
        Restaurant restaurant = auth(menuDto);
        //manager can add a menu only if the restaurant belongs to him
        if (restaurant.getManager() != null && restaurant.getManager().getUserName().equals(username))
            return ResponseEntity.ok(menuService.save(menuDto));
        throw new AuthenticationException();
    }
    @GetMapping
    public ResponseEntity<?> getAllMenus(HttpServletRequest request){
        //admin gets inactive menus too
        if (authController.isAdmin(request))
            return ResponseEntity.ok(menuService.findAllForAdmin());
        return ResponseEntity.ok(menuService.findAll());
    }
    private Restaurant menuQuery(Integer id) throws InvalidDataException{
        Menu menu = menuService.findByIdAll(id);
        if (menu == null)
            throw new NullPointerException("MENU WITH ID " + id);
        if (menu.getRestaurant() == null)
            throw new InvalidDataException("Menu doesnt have a restaurant");
        return menu.getRestaurant();
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable(name = "id") Integer id
            , HttpServletRequest request) throws Exception{
        String username = authController.usernameFromToken(request);
        Restaurant restaurant = menuQuery(id);
        //manager and admin get the menu even if it is invalid
        if (restaurant.getManager() != null && (restaurant.getManager().getUserName().equals(username)
                || authController.isAdmin(request)))
            return ResponseEntity.ok(menuService.findByIdForManager(id));
        return ResponseEntity.ok(menuService.findById(id));
    }
    @GetMapping("/{id}/items")
    public ResponseEntity<?> getItemsOfAMenu(@PathVariable(name = "id") Integer id
            , HttpServletRequest request) throws Exception{
        String username = authController.usernameFromToken(request);
        Restaurant restaurant = menuQuery(id);
        //manager gets all items
        if (restaurant.getManager() != null && restaurant.getManager().getUserName().equals(username))
            return ResponseEntity.ok(menuService.findAllItemsOfAMenu(id));
        return ResponseEntity.ok(menuService.findAllValidItemsOfAMenu(id));
    }
    @PutMapping( "/{id}")
    public ResponseEntity<?> update(@PathVariable(name = "id") Integer id
            ,@RequestBody MenuDto menuDto, HttpServletRequest request) throws Exception{
        menuDto.setId(id);
        String username = authController.usernameFromToken(request);
        Menu menu = menuService.findByIdAll(id);
        if (menu == null)
            throw new NullPointerException("MENU WITH ID " + id);
        if (menu.getRestaurant() == null)
            throw new AuthenticationException();
        Restaurant restaurant = menu.getRestaurant();
        if (restaurant.getManager() != null && (restaurant.getManager().getUserName().equals(username))) {
            if (menuDto.getRestaurant() != null) {
                //validating if the restaurant is owned by this manager
                Restaurant restaurant1 = restaurantService.findByName(menuDto.getRestaurant());
                if (!restaurant1.getManager().getUserName().equals(username))
                    throw new AuthenticationException();
            }
            return ResponseEntity.ok(menuService.save(menuDto));
        }
        throw new AuthenticationException();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") Integer id, HttpServletRequest request) throws Exception{
        String username = authController.usernameFromToken(request);
        Menu menu = menuService.findByIdAll(id);
        if (menu == null)
            throw new NullPointerException("MENU WITH ID " + id);
        MenuDto menuDto = menuService.convertMenuToDto(menu);
        return validate(menu, id, menuDto, username);
    }
    @DeleteMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteAll() {
        return ResponseEntity.ok(menuService.deleteAll());
    }

    private ResponseEntity<?> validate(Menu menu, Integer id, MenuDto menuDto, String username) throws Exception{
        Restaurant restaurant = menu.getRestaurant();
        if (restaurant == null)
            throw new AuthenticationException();
        if (restaurant.getManager() != null && (restaurant.getManager().getUserName().equals(username))) {
            menuService.deleteById(id);
            return ResponseEntity.ok(menuDto);
        }
        throw new AuthenticationException();
    }
}
