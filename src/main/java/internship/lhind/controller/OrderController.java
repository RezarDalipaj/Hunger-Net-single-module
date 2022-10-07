package internship.lhind.controller;

import internship.lhind.model.dto.OrderDto;
import internship.lhind.model.dto.OrderStatusDto;
import internship.lhind.model.entity.Menu;
import internship.lhind.model.entity.Order;
import internship.lhind.model.entity.Restaurant;
import internship.lhind.model.entity.User;
import internship.lhind.service.OrderService;
import internship.lhind.service.RestaurantService;
import internship.lhind.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final JwtAuthenticationController authController;
    private final OrderService orderService;
    private final UserService userService;
    private final RestaurantService restaurantService;

    public OrderController(JwtAuthenticationController authController, OrderService orderService, UserService userService, RestaurantService restaurantService) {
        this.authController = authController;
        this.orderService = orderService;
        this.userService = userService;
        this.restaurantService = restaurantService;
    }
    private void validate(Order order, HttpServletRequest request) throws Exception{
        Menu menu = order.getMenu();
        if (menu.getRestaurant() == null)
            throw new AuthenticationException();
        Restaurant restaurant = menu.getRestaurant();
        if (restaurant.getManager() == null)
            throw new AuthenticationException();
        if (!restaurant.getManager().getUserName().equals(authController.usernameFromToken(request)))
            throw new AuthenticationException();
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable(name = "id") Integer id, HttpServletRequest request) throws Exception{
        Order order = orderService.findByIdValid(id);
        if (order == null)
            throw new NullPointerException("ORDER WITH ID " + id);
        User user = order.getUser();
        if (user.getUserName().equals(authController.usernameFromToken(request)))
            return ResponseEntity.ok(orderService.findValidById(id));
        //the manager of the restaurant of this order can view the order too
        validate(order, request);
        return ResponseEntity.ok(orderService.findValidById(id));
    }
    @GetMapping("/restaurant/{id}")
    public ResponseEntity<?> getOrdersOfARestaurant(@PathVariable(name = "id") Integer id
            , HttpServletRequest request) throws Exception{
        Restaurant restaurant1 = restaurantService.findByIdd(id);
        if (restaurant1 == null)
            throw new NullPointerException("RESTAURANT WITH ID " + id);
        if (restaurant1.getManager() == null)
            throw new AuthenticationException();
        User manager = restaurant1.getManager();
        //only the manager views all the orders
        if (manager.getUserName().equals(authController.usernameFromToken(request)))
            return ResponseEntity.ok(orderService.findOrdersOfARestaurant(id));
        throw new AuthenticationException();
    }
    @GetMapping("/restaurant/{id}/{status}")
    public ResponseEntity<?> getOrdersOfARestaurantByStatus(@PathVariable(name = "id") Integer id
            , @PathVariable(name = "status") String status, HttpServletRequest request) throws Exception{
        Restaurant restaurant1 = restaurantService.findByIdd(id);
        if (restaurant1 == null)
            throw new NullPointerException("RESTAURANT WITH ID " + id);
        if (restaurant1.getManager() == null)
            throw new AuthenticationException();
        User manager = restaurant1.getManager();
        if (manager.getUserName().equals(authController.usernameFromToken(request)))
            return ResponseEntity.ok(orderService.findOrdersOfARestaurantByStatus(status, id));
        throw new AuthenticationException();
    }
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getOrdersOfAUser(@PathVariable(name = "id") Integer id
            , HttpServletRequest request) throws Exception{
        User user = userService.findByIdd(id);
        if (user == null)
            throw new NullPointerException("USER WITH ID " + id);
        if (user.getUserName().equals(authController.usernameFromToken(request)))
            return ResponseEntity.ok(orderService.findOrdersOfAUser(id));
        throw new AuthenticationException();
    }
    @PostMapping
    public ResponseEntity<?> saveOrder(@RequestBody OrderDto orderDto, HttpServletRequest request) throws Exception{
        orderDto.setId(null);
        String username = authController.usernameFromToken(request);
        orderDto.setUsername(username);
        return ResponseEntity.ok(orderService.save(orderDto));
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@RequestBody OrderDto orderDto
            , @PathVariable(name = "id") Integer id, HttpServletRequest request) throws Exception{
        orderDto.setId(id);
        String username = authController.usernameFromToken(request);
        orderDto.setUsername(username);
        return ResponseEntity.ok(orderService.save(orderDto));
    }
    @PutMapping("/{id}/update/status")
    public ResponseEntity<?> updateOrderStatus(@RequestBody OrderStatusDto orderStatusDto
            , @PathVariable(name = "id") Integer id, HttpServletRequest request) throws Exception{
        orderStatusDto.setId(id);
        String username = authController.usernameFromToken(request);
        orderStatusDto.setUsername(username);
        return ResponseEntity.ok(orderService.updateStatus(orderStatusDto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable(name = "id") Integer id, HttpServletRequest request) throws Exception{
        Order order = orderService.findByIdValid(id);
        if (order == null)
            throw new NullPointerException("ORDER WITH ID " + id);
        User user = order.getUser();
        if (user.getUserName().equals(authController.usernameFromToken(request)))
            return ResponseEntity.ok(orderService.deleteByIdUser(id));
        validate(order, request);
        return ResponseEntity.ok(orderService.deleteByIdManager(id));
    }
}
