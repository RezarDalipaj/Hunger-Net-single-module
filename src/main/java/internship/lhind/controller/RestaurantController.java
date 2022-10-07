package internship.lhind.controller;

import internship.lhind.model.dto.RestaurantDto;
import internship.lhind.model.entity.Restaurant;
import internship.lhind.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final JwtAuthenticationController authController;
    RestaurantController(RestaurantService restaurantService, JwtAuthenticationController authController){
        this.restaurantService = restaurantService;
        this.authController = authController;
    }

    @GetMapping
    public ResponseEntity<?> get(HttpServletRequest request){
        //admins views all menus, others view only active menus
        if (authController.isAdmin(request))
            return ResponseEntity.ok(restaurantService.findAllValidForAdmin());
        return ResponseEntity.ok(restaurantService.findAllValid());
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable(name = "id") Integer id, HttpServletRequest request){
        RestaurantDto restaurantDto = restaurantService.findById(id);
        //admin and manager view all menus of the restaurant
        if (authController.isAdmin(request) ||
                authController.usernameFromToken(request).equals(restaurantDto.getManager()))
            return ResponseEntity.ok(restaurantService.findByIdForManager(id));
        else
            return ResponseEntity.ok(restaurantDto);
    }
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> post(@RequestBody RestaurantDto restaurantDto) throws Exception{
        restaurantDto.setId(null);
        return ResponseEntity.ok(restaurantService.save(restaurantDto));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateRestaurant(@PathVariable(name = "id") Integer id
            , @RequestBody RestaurantDto restaurantDto) throws Exception{
        restaurantDto.setId(id);
        return ResponseEntity.ok(restaurantService.save(restaurantDto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") Integer id, HttpServletRequest request) throws Exception{
        Restaurant restaurant = restaurantService.findByIdd(id);
        RestaurantDto restaurantDto = restaurantService.findByIdForManager(id);
        if (authController.isAdmin(request)) {
            restaurantService.deleteById(id);
            return ResponseEntity.ok(restaurantDto);
        }
        if (!(authController.usernameFromToken(request)).equals(restaurant.getManager().getUserName()))
            throw new AuthenticationException();
        restaurantService.deleteById(id);
        return ResponseEntity.ok(restaurantDto);
    }
    @DeleteMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteAll() {
        return ResponseEntity.ok(restaurantService.deleteAll());
    }
}
