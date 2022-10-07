package internship.lhind.service.impl;

import internship.lhind.customException.InvalidDataException;
import internship.lhind.model.dto.ItemDto;
import internship.lhind.model.entity.*;
import internship.lhind.repository.ItemRepository;
import internship.lhind.repository.StatusRepository;
import internship.lhind.service.ItemService;
import internship.lhind.service.MenuService;
import internship.lhind.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final MenuService menuService;
    private final ItemRepository itemRepository;
    private final StatusRepository statusRepository;
    private final OrderService orderService;
    private final Logger logger = LoggerFactory.getLogger(RestaurantServiceImpl.class);
    public ItemServiceImpl(MenuService menuService, ItemRepository itemRepository
            , StatusRepository statusRepository,@Lazy OrderService orderService) {
        this.menuService = menuService;
        this.itemRepository = itemRepository;
        this.statusRepository = statusRepository;
        this.orderService = orderService;
    }
    //finding not deleted items by id
    @Override
    public ItemDto findById(Integer id) {
        Optional<Item> optionalItem = itemRepository.findById(id);
        if (optionalItem.isPresent()){
            Item item = optionalItem.get();
            if (item.getStatus().getStatus().equals("VALID"))
                return convertItemToDto(item);
            else
                throw new NullPointerException("ITEM WITH ID " + id);
        }
        else
            throw new NullPointerException("ITEM WITH ID " + id);
    }
    @Override
    public Item findByIdd(Integer id) {
        Optional <Item> optionalItem = itemRepository.findById(id);
        if (optionalItem.isPresent()){
            Item item = optionalItem.get();
            if (item.getStatus().getStatus().equals("VALID"))
                return item;
            else
                return null;
        }
        else
            return null;
    }
    //finding items by id from active menus
    @Override
    public Item findByIdValid(Integer id) {
        Item item = findByIdAll(id);
        if (item.getMenu() == null)
            return null;
        menuService.validateMenu(item.getMenu());
        if (item.getStatus().getStatus().equals("VALID")
                && item.getMenu().getStatus().getStatus().equals("VALID"))
            return item;
        else
            return null;
    }
    //finding valid and invalid items
    public Item findByIdAll(Integer id) {
        Optional <Item> optionalItem = itemRepository.findById(id);
        if (optionalItem.isPresent()){
            Item item = optionalItem.get();
            return item;
        }
        else
            return null;
    }
    //finding all valid items
    @Override
    public List<ItemDto> findAllValid(){
        Status status = statusRepository.findStatusByStatus("VALID");
        if (status.getItems() == null)
            return new ArrayList<>();
        List<Item> items = status.getItems();
        return items.stream().map(this::convertItemToDto).collect(Collectors.toList());
    }
    //finding all items from all active menus
    @Override
    public List<ItemDto> findAllForClients(){
        Status status = statusRepository.findStatusByStatus("VALID");
        List<Item> items = status.getItems();
        List<Item> validItems = new ArrayList<>();
        for (Item item:items) {
            menuService.validateMenu(item.getMenu());
            if (item.getMenu() != null && item.getMenu().getStatus().getStatus().equals("VALID"))
                validItems.add(item);
        }
        if (validItems.size() == 0)
            return new ArrayList<>();
        return validItems.stream().map(this::convertItemToDto).collect(Collectors.toList());
    }
    //deleting orders of a deleted item
    private void deleteOrdersOfAnItem(Item item){
        if (item.getItemOrderList() != null) {
            for (OrderItem orderItem : item.getItemOrderList()) {
                Order order = orderItem.getOrder();
                if (!order.getStatus().getStatus().equals("DELIVERED")
                        && !order.getStatus().getStatus().equals("REJECTED")
                        && !order.getStatus().getStatus().equals("DELETED")) {
                    orderService.deleteByIdManager(order.getOrder_id());
                    logger.info("Deleting order of this item");
                }
            }
        }
    }
    //deleting items by id
    @Override
    public void deleteById(Integer id){
        Item item = findByIdAll(id);
        if (item == null)
            throw new NullPointerException("ITEM WITH ID " + id);
        Status status = statusRepository.findStatusByStatus("DELETED");
        deleteOrdersOfAnItem(item);
        item.setStatus(status);
        logger.info("Deleted order with id " + id);
        itemRepository.save(item);
    }
    @Override
    public List<ItemDto> deleteAll(){
        List<ItemDto> itemDtoList = findAllValid();
        List<Item> itemList = itemRepository.findAll();
        Status status = statusRepository.findStatusByStatus("DELETED");
        for (Item item: itemList) {
            deleteOrdersOfAnItem(item);
            item.setStatus(status);
            itemRepository.save(item);
        }
        logger.info("Deleted all items");
        return itemDtoList;
    }
    //saving and updating items
    @Override
    public ItemDto save(ItemDto itemDto) throws Exception{
        Item item;
        if (itemDto.getId() == null)
            //save
            item = convertDtoToItemAdd(itemDto);
        else
            //update
            item = convertDtoToItemUpdate(itemDto);
        Status status = statusRepository.findStatusByStatus("VALID");
        item.setStatus(status);
        itemRepository.save(item);
        return convertItemToDto(item);
    }
    public ItemDto saveFromRepository(Item item){
        item = itemRepository.save(item);
        return convertItemToDto(item);
    }
    private Item convertDtoToItemAdd(ItemDto itemDto) throws Exception{
        Item item = new Item();
        if (itemDto.getName() == null || itemDto.getMenuId() == null
                || itemDto.getPrice() == null || itemDto.getInStock() == null)
            throw new InvalidDataException("All fields are required");
        if (itemDto.getName().length() < 3)
            throw new InvalidDataException("Item name cannot be that short");
        item.setName(itemDto.getName());
        setPrice(item,itemDto);
        setStock(item,itemDto);
        setMenu(item,itemDto);
        logger.info("Added new item to db");
        return item;
    }
    private Item convertDtoToItemUpdate(ItemDto itemDto) throws Exception{
        Item item = findByIdd(itemDto.getId());
        if (item == null)
            throw new NullPointerException();
        if (itemDto.getName()!=null)
            item.setName(itemDto.getName());
        if (itemDto.getPrice()!=null)
            setPrice(item,itemDto);
        if (itemDto.getInStock()!=null)
            setStock(item,itemDto);
        if (itemDto.getMenuId()!=null)
            setMenu(item,itemDto);
        logger.info("Updated item with id " + itemDto.getId());
        return item;
    }
    //checking price validity
    private void setPrice(Item item, ItemDto itemDto) throws Exception{
        if (itemDto.getPrice()<=0)
            throw new InvalidDataException("Price should not be negative");
        item.setPrice(itemDto.getPrice());
    }
    //checking stock validity
    private void setStock(Item item, ItemDto itemDto) throws Exception{
        if (itemDto.getInStock() <= 0)
            throw new InvalidDataException("Stock should not be negative");
        item.setInStock(itemDto.getInStock());
    }
    //checking menu validity
    private Item setMenu(Item item, ItemDto itemDto) throws Exception{
        Menu menu = menuService.findByIdAll(itemDto.getMenuId());
        if (menu == null)
            throw new InvalidDataException("Menu does not exist");
        item.setMenu(menu);
        return item;
    }
    public ItemDto convertItemToDto(Item item){
        ItemDto itemDto = new ItemDto();
        if (item.getName()!=null)
            itemDto.setName(item.getName());
        if (item.getMenu() != null) {
            itemDto.setMenuName(item.getMenu().getName());
            if (item.getMenu().getRestaurant() != null)
                itemDto.setRestaurant(item.getMenu().getRestaurant().getName());
        }
        if (item.getPrice()!=null)
            itemDto.setPrice(item.getPrice());
        if (item.getInStock() != null)
            itemDto.setInStock(item.getInStock());
        return itemDto;
    }
}
