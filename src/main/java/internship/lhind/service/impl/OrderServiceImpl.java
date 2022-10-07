package internship.lhind.service.impl;

import internship.lhind.customException.InvalidDataException;
import internship.lhind.model.dto.OrderDto;
import internship.lhind.model.dto.OrderItemDto;
import internship.lhind.model.dto.OrderStatusDto;
import internship.lhind.model.entity.*;
import internship.lhind.repository.*;
import internship.lhind.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ItemService itemService;
    private final UserService userService;
    private final OrderItemService orderItemService;
    private final RoleRepository roleRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestaurantService restaurantService;
    private final StatusRepository statusRepository;

    private final Logger logger = LoggerFactory.getLogger(RestaurantServiceImpl.class);
    public OrderServiceImpl(OrderRepository orderRepository
            , OrderStatusRepository orderStatusRepository
            , ItemService itemService, UserService userService
            , OrderItemService orderItemService, RoleRepository roleRepository
            , OrderItemRepository orderItemRepository, RestaurantService restaurantService, StatusRepository statusRepository) {
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.itemService = itemService;
        this.userService = userService;
        this.orderItemService = orderItemService;
        this.roleRepository = roleRepository;
        this.orderItemRepository = orderItemRepository;
        this.restaurantService = restaurantService;
        this.statusRepository = statusRepository;
    }
    @Override
    public Order findByIdValid(Integer id){
        Order order = findById(id);
        if (!order.getStatus().getStatus().equals("DELETED"))
            return order;
        return null;
    }
    @Override
    public OrderDto findValidById(Integer id){
        Order order = findByIdValid(id);
        if (order == null)
            throw new NullPointerException("ORDER WITH ID " +id);
        return convertToOrderDto(order);
    }
    //user can delete orders when they are created
    @Override
    public OrderDto deleteByIdUser(Integer id) throws Exception{
        Order order = findByIdValid(id);
        if (order == null)
            throw new NullPointerException("ORDER WITH ID " + id);
        if (!order.getStatus().getStatus().equals("CREATED"))
            throw new InvalidDataException("Can only delete order when it is still created");
        Status status = statusRepository.findStatusByStatus("DELETED");
        OrderStatus orderStatus = orderStatusRepository.findOrderStatusByOrderStatus("DELETED");
        order.setStatus(orderStatus);
        for (OrderItem orderItem:order.getItemOrderList()) {
            orderItem.setStatus(status);
        }
        orderRepository.save(order);
        logger.info("Deleted order with id " + id);
        return convertToOrderDto(order);
    }
    //orders of a user
    @Override
    public List<OrderDto> findOrdersOfAUser(Integer id){
        User user = userService.findByIdd(id);
        List<Order> orderList = new ArrayList<>();
        if (user.getOrders() != null){
            for (Order order: user.getOrders()) {
                if (!order.getStatus().getStatus().equals("DELETED"))
                    orderList.add(order);
            }
            if (orderList.size()>0)
                return orderList.stream().map(this::convertToOrderDto).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
    //orders of a restaurant by status
    @Override
    public List<OrderDto> findOrdersOfARestaurantByStatus(String status, Integer id) throws Exception{
        OrderStatus orderStatus = orderStatusRepository.findOrderStatusByOrderStatus(status);
        if (orderStatus == null)
            throw new InvalidDataException("Status doesnt exist");
        if (findOrdersOfARestaurant(id).size() == 0)
            return new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        List<Integer> orderIds = orderRepository.filterOrderByStatus(orderStatus.getId(), id);
        if (orderIds.isEmpty())
            return new ArrayList<>();
        for (Integer orderId: orderIds) {
            Order order = findById(orderId);
            orders.add(order);
        }
        return orders.stream().map(this::convertToOrderDto).collect(Collectors.toList());
    }
    @Override
    public List<OrderDto> findOrdersOfARestaurant(Integer id){
        Restaurant restaurant = restaurantService.findByIdd(id);
        List<Order> orders = new ArrayList<>();
        if (restaurant.getMenuList() == null)
            return new ArrayList<>();
        for (Menu menu: restaurant.getMenuList()) {
            List<Order> orderList = new ArrayList<>();
            if (menu.getOrders()!=null) {
                for (Order order: menu.getOrders()) {
                    if (!order.getStatus().getStatus().equals("DELETED"))
                        orderList.add(order);
                }
                orders.addAll(orderList);
            }
        }
        if (orders.size() == 0)
            return new ArrayList<>();
        return orders.stream().map(this::convertToOrderDto).collect(Collectors.toList());
    }
    //manager can delete menus not only when they are created
    @Override
    public OrderDto deleteByIdManager(Integer id){
        Order order = findByIdValid(id);
        if (order == null)
            throw new NullPointerException("ORDER WITH ID " + id);
        OrderStatus orderStatus = orderStatusRepository.findOrderStatusByOrderStatus("DELETED");
        order.setStatus(orderStatus);
        Status status = statusRepository.findStatusByStatus("DELETED");
        for (OrderItem orderItem:order.getItemOrderList()) {
            orderItem.setStatus(status);
        }
        orderRepository.save(order);
        logger.info("Manager deleted order with id " + id);
        return convertToOrderDto(order);
    }
    //validating order update status
    private void validate(User user, Order order, OrderStatusDto orderStatusDto) throws Exception{
        if (user == null)
            throw new AuthenticationException();
        Restaurant restaurant = restaurantService.findByManager(user.getUserName());
        if (restaurant == null)
            throw new AuthenticationException();
        Menu menu = order.getMenu();
        if (!restaurant.getMenuList().contains(menu))
            throw new AuthenticationException();
        if (orderStatusDto.getStatus() == null)
            throw new InvalidDataException("Please enter a status");
        String status = orderStatusDto.getStatus();
        OrderStatus orderStatus = orderStatusRepository.findOrderStatusByOrderStatus(status);
        if (orderStatus == null)
            throw new InvalidDataException("Cannot update. Status doesnt exist");
    }
    private void setStatus(OrderStatusDto orderStatusDto,Order order, String status) throws Exception{
        if (!orderStatusDto.getStatus().equals(status))
            throw new InvalidDataException("This order cannot have this status right now");
        order.setStatus(orderStatusRepository.findOrderStatusByOrderStatus(status));
    }
    //even though manager asked for the order to be approved, it is validated if it can be
    private void approveOrder(Order order, boolean hasStock, User user){
        for (OrderItem orderItem : order.getItemOrderList()) {
            if (orderItem.getQuantity() > orderItem.getItem().getInStock()) {
                hasStock = false;
                break;
            }
        }
        if (hasStock) {
            if(order.getAmountPayed() < user.getBalance())
                order.setStatus(orderStatusRepository.findOrderStatusByOrderStatus("APPROVED"));
            else
                order.setStatus(orderStatusRepository.findOrderStatusByOrderStatus("REJECTED"));
        }
        else
            order.setStatus(orderStatusRepository.findOrderStatusByOrderStatus("REJECTED"));
    }
    @Override
    public OrderDto updateStatus(OrderStatusDto orderStatusDto) throws Exception{
        Order order = findById(orderStatusDto.getId());
        if (order == null)
            throw new NullPointerException("ORDER WITH ID " + orderStatusDto.getId());
        User user = userService.findByUserName(orderStatusDto.getUsername());
        validate(user,order,orderStatusDto);
        switch (order.getStatus().getStatus()) {
            case "DELIVERED":
                throw new InvalidDataException("Cannot update. Order is delivered");
            case "REJECTED":
                throw new InvalidDataException("Cannot update. Order is rejected");
            case "DELETED":
                throw new NullPointerException("ORDER WITH ID" + orderStatusDto.getId());
            case "WAITING_FOR_DELIVERY":
                setStatus(orderStatusDto,order,"DELIVERED");
                LocalDateTime deliveredDate = LocalDateTime.now();
                order.setDeliveredDate(deliveredDate);
                break;
            case "PREPARED":
                setStatus(orderStatusDto,order,"WAITING_FOR_DELIVERY");
                break;
            case "APPROVED":
                setStatus(orderStatusDto,order,"PREPARED");
                break;
            case "CREATED":
                if (!orderStatusDto.getStatus().equals("APPROVED")
                        && !orderStatusDto.getStatus().equals("REJECTED"))
                    throw new InvalidDataException("Status can only be updated to APPROVED or REJECTED right now");
                boolean hasStock = true;
                if (orderStatusDto.getStatus().equals("REJECTED"))
                    order.setStatus(orderStatusRepository.findOrderStatusByOrderStatus("REJECTED"));
                 else
                    approveOrder(order,hasStock,user);
                break;
        }
        return saveOrder(user,order);
    }
    private OrderDto saveOrder(User user, Order order){
        //when order is delivered user balance and stock of items decrements
        if (order.getStatus().getStatus().equals("DELIVERED")) {
            User user1 = order.getUser();
            user1.setBalance(user1.getBalance() - order.getAmountPayed());
            userService.saveFromRepository(user);
            for (OrderItem orderItem : order.getItemOrderList())
                orderItem.getItem().setInStock(orderItem.getItem().getInStock()-orderItem.getQuantity());
        }
        orderRepository.save(order);
        logger.info("Order status updated");
        return convertToOrderDto(order);
    }
    @Override
    public Order findById(Integer id){
        Optional<Order> optionalOrder = orderRepository.findById(id);
        return optionalOrder.orElse(null);
    }
    @Override
    public Order findByIdCreated(Integer id){
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()){
            Order order = optionalOrder.get();
            if (!order.getStatus().getStatus().equals("CREATED"))
                return null;
            return order;
        }
        return null;
    }

    @Override
    public OrderDto save(OrderDto orderDto) throws Exception{
        Order order;
        if (orderDto.getId() == null)
            order = convertDtoToOrderAdd(orderDto);
        else
            order = convertDtoToOrderUpdate(orderDto);
        OrderStatus orderStatus = orderStatusRepository.findOrderStatusByOrderStatus("CREATED");
        order.setStatus(orderStatus);
        LocalDateTime createdDate = LocalDateTime.now();
        order.setCreatedDate(createdDate);
        orderRepository.save(order);
        return convertToOrderDto(order);
    }
    //adding order
    private Order convertDtoToOrderAdd(OrderDto orderDto) throws Exception{
        User user = userService.findByUserName(orderDto.getUsername());
        Role role = roleRepository.findRoleByRole("CLIENT");
        if (!user.getRoles().contains(role))
            throw new AuthenticationException();
        if (orderDto.getOrderList() == null || orderDto.getOrderList().isEmpty())
            throw new InvalidDataException("Order cannot be empty");
        List<OrderItem> orderItemList = new ArrayList<>();
        List<Menu> menuList = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        Order order = new Order();
        Double amountPayed = 0D;
        for (OrderItemDto orderItemDto: orderDto.getOrderList()) {
            Item item = setItems(orderItemDto,items,menuList);
            order.setMenu(item.getMenu());
            amountPayed = setOrderItem(amountPayed,item,order,orderItemDto,orderItemList);
        }
        return setOrder(amountPayed,user,order,orderItemList);
    }
    private Double setOrderItem(Double amountPayed, Item item, Order order,
                              OrderItemDto orderItemDto, List<OrderItem> orderItemList){
        amountPayed = amountPayed + (item.getPrice() * orderItemDto.getQuantity());
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        Status status = statusRepository.findStatusByStatus("VALID");
        orderItem.setStatus(status);
        orderItem.setQuantity(orderItemDto.getQuantity());
        orderItem.setOrder(order);
        orderItemList.add(orderItem);
        return amountPayed;
    }
    private Order setOrder(Double amountPayed, User user, Order order, List<OrderItem> orderItemList) throws Exception{
        if (amountPayed> user.getBalance())
            throw new InvalidDataException("You cannot afford this order");
        order.setItemOrderList(orderItemList);
        order.setUser(user);
        order.setAmountPayed(amountPayed);
        logger.info("Added new order");
        return order;
    }
    //validating items of the order
    private Item setItems(OrderItemDto orderItemDto, List<Item> items, List<Menu> menuList) throws Exception{
        if (orderItemDto.getItemId() == null)
            throw new InvalidDataException("Item is required");
        if (orderItemDto.getQuantity() == null)
            throw new InvalidDataException("Quantity is required");
        Item item = itemService.findByIdValid(orderItemDto.getItemId());
        if (item == null)
            throw new InvalidDataException("Item is not valid");
        if (orderItemDto.getQuantity()<=0)
            throw new InvalidDataException("Quantity cannot be negative");
        if (items.size() != 0) {
            if (items.contains(item)) {
                throw new InvalidDataException("Item cannot be twice in the order");
            }
        }
        items.add(item);
        Menu menu = item.getMenu();
        if (menuList.size() != 0) {
            if (!menuList.contains(menu)) {
                throw new InvalidDataException("Cannot order from different menus");
            }
        }
        menuList.add(menu);
        return item;
    }
    private void validations(OrderItemDto orderItemDto, List<OrderItem> orderItemList
            , List<Item> items) throws Exception{
        updateValidations(orderItemDto);
        Item item = itemService.findByIdValid(orderItemDto.getItemId());
        itemValidations(item, orderItemList, items);
        items.add(item);
    }
    private void removeItemsFromOrder(List<OrderItem> orderItemList){
        OrderItem orderItem = orderItemList.get(orderItemList.size()-1);
        orderItemList.remove(orderItem);
        orderItemRepository.delete(orderItem);
    }
    private void addItemsToOrder(Order order, List<OrderItem> orderItemList){
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItemList.add(orderItem);
    }
    //validating order before updating it
    private List<OrderItem> checkSize(List<OrderItem> orderItemList, OrderDto orderDto
            , Order order, Double amountPayed, User user) throws Exception{
        List<Item> items = new ArrayList<>();
        for (OrderItemDto orderItemDto: orderDto.getOrderList()) {
            validations(orderItemDto, orderItemList, items);
            Item item = itemService.findByIdValid(orderItemDto.getItemId());
            amountPayed = amountPayed + (item.getPrice() * orderItemDto.getQuantity());
        }
        if (amountPayed> user.getBalance())
            throw new InvalidDataException("You cannot afford this order");
        if (orderItemList.size()>orderDto.getOrderList().size()){
            int difference = orderItemList.size() - orderDto.getOrderList().size();
            for (int j = 0; j < difference; j++)
                removeItemsFromOrder(orderItemList);
        }
        if (orderDto.getOrderList().size()>orderItemList.size()){
            int difference = orderDto.getOrderList().size() - orderItemList.size();
            for (int j = 0; j < difference; j++)
                addItemsToOrder(order,orderItemList);
        }
        return orderItemList;
    }
    private void updateValidations(OrderItemDto orderItemDto) throws Exception{
        if (orderItemDto.getItemId() == null) {
            throw new InvalidDataException("Please provide the id of the item");
        }
        if (orderItemDto.getQuantity() != null) {
            if (orderItemDto.getQuantity() <=0) {
                throw new InvalidDataException("Quantity should only be positive");
            }
        }
    }
    private void itemValidations(Item item, List<OrderItem> orderItemList, List<Item> items) throws Exception{
        if (item == null)
            throw new InvalidDataException("Item is not valid");
        if (!orderItemList.get(0).getItem().getMenu().equals(item.getMenu()))
            throw new InvalidDataException("Cannot order from different menus");
        if (items.size() != 0) {
            if (items.contains(item))
                throw new InvalidDataException("Cannot order the same item twice");
        }
    }
    //updating order when it is still created
    private Order convertDtoToOrderUpdate(OrderDto orderDto) throws Exception{
        Order order = findById(orderDto.getId());
        if (order == null)
            throw new NullPointerException("ORDER WITH ID " + orderDto.getId());
        if (!orderDto.getUsername().equals(order.getUser().getUserName()))
            throw new AuthenticationException();
        if (!order.getStatus().getStatus().equals("CREATED"))
            throw new InvalidDataException("Cannot update. Order status has been updated by the manager");
        User user = order.getUser();
        if (orderDto.getOrderList() == null || orderDto.getOrderList().isEmpty())
            return order;
        Double amountPayed = 0D;
        List<OrderItem> orderItemList = order.getItemOrderList();
        int i = 0;
        orderItemList = checkSize(orderItemList,orderDto,order, amountPayed, user);
        for (OrderItem orderItem: orderItemList) {
            OrderItemDto orderItemDto = orderDto.getOrderList().get(i);
            if (orderItemDto == null)
                continue;
            Item item = itemService.findByIdValid(orderItemDto.getItemId());
            order.setMenu(item.getMenu());
            orderItem.setQuantity(orderItemDto.getQuantity());
            amountPayed = setOrderUpdate(amountPayed,item,orderItem,order,user);
            i++;
        }
        return setFinalOrder(order,amountPayed,orderItemList,user);
    }
    private Order setFinalOrder(Order order, Double amountPayed
            , List<OrderItem> orderItemList, User user){
        order.setItemOrderList(orderItemList);
        order.setUser(user);
        order.setAmountPayed(amountPayed);
        logger.info("Updated order with id " + order.getOrder_id());
        return order;
    }
    private Double setOrderUpdate(Double amountPayed, Item item, OrderItem orderItem
            , Order order, User user) throws Exception{
        amountPayed = amountPayed + (item.getPrice() * orderItem.getQuantity());
        if (amountPayed> user.getBalance())
            throw new InvalidDataException("You cannot afford this order");
        Status status = statusRepository.findStatusByStatus("VALID");
        orderItem.setStatus(status);
        orderItem.setItem(item);
        orderItem.setOrder(order);
        return amountPayed;
    }
    private OrderDto convertToOrderDto(Order order){
        OrderDto orderDto = new OrderDto();
        if (order.getUser()!=null)
            orderDto.setUsername(order.getUser().getUserName());
        if (order.getStatus() != null)
            orderDto.setStatus(order.getStatus().getStatus());
        if (order.getCreatedDate() != null)
            orderDto.setCreatedDate(order.getCreatedDate());
        if (order.getDeliveredDate() != null)
            orderDto.setDeliveredDate(order.getDeliveredDate());
        List<OrderItemDto> orderItemDtoList = new ArrayList<>();
        if (order.getItemOrderList() != null){
            for (OrderItem orderItem : order.getItemOrderList()) {
                OrderItemDto orderItemDto = orderItemService.convertToOrderItemDto(orderItem);
                orderItemDtoList.add(orderItemDto);
            }
        }
        orderDto.setOrderList(orderItemDtoList);
        if (order.getAmountPayed() != null)
            orderDto.setAmountPayed(order.getAmountPayed());
        return orderDto;
    }
}
