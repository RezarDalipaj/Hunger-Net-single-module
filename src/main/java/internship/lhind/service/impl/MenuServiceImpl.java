package internship.lhind.service.impl;

import internship.lhind.customException.InvalidDataException;
import internship.lhind.model.entity.*;
import internship.lhind.model.dto.ItemDto;
import internship.lhind.model.dto.MenuDto;
import internship.lhind.repository.MenuRepository;
import internship.lhind.repository.MenuTypeRepository;
import internship.lhind.repository.StatusRepository;
import internship.lhind.service.MenuService;
import internship.lhind.service.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {
    private final MenuRepository menuRepository;
    private final MenuTypeRepository menuTypeRepository;
    private final RestaurantService restaurantService;
    private final ItemServiceImpl itemService;
    private final StatusRepository statusRepository;
    private final Logger logger = LoggerFactory.getLogger(RestaurantServiceImpl.class);
    public MenuServiceImpl(MenuRepository menuRepository, MenuTypeRepository menuTypeRepository
            , @Lazy RestaurantService restaurantService, @Lazy ItemServiceImpl itemService
            , StatusRepository statusRepository) {
        this.menuRepository = menuRepository;
        this.menuTypeRepository = menuTypeRepository;
        this.restaurantService = restaurantService;
        this.itemService = itemService;
        this.statusRepository = statusRepository;
    }
    @Override
    public Menu findByIdd(Integer id){
        Optional<Menu> optionalMenu = menuRepository.findById(id);
        if (optionalMenu.isPresent()){
            Menu menu = optionalMenu.get();
            validateMenu(menu);
            if (menu.getStatus().getStatus().equals("VALID"))
                return menu;
        }
        return null;
    }

    @Override
    public Menu findByIdAll(Integer id) {
        Optional<Menu> optionalMenu = menuRepository.findById(id);
        if (optionalMenu.isPresent()){
            Menu menu = optionalMenu.get();
            if (!menu.getStatus().getStatus().equals("DELETED"))
                return menu;
        }
        return null;
    }
    @Override
    public List<ItemDto> findAllItemsOfAMenu(Integer id) {
        Optional<Menu> optionalMenu = menuRepository.findById(id);
        if (optionalMenu.isPresent()){
            Menu menu = optionalMenu.get();
            if (!menu.getStatus().getStatus().equals("DELETED")) {
                if(menu.getItems() != null)
                    return menu.getItems().stream().map(itemService::convertItemToDto).collect(Collectors.toList());
            }        }
        throw new NullPointerException("ITEMS");
    }
    //finding all items of an active menu
    @Override
    public List<ItemDto> findAllValidItemsOfAMenu(Integer id) {
        Optional<Menu> optionalMenu = menuRepository.findById(id);
        if (optionalMenu.isPresent()){
            Menu menu = optionalMenu.get();
            validateMenu(menu);
            if (menu.getStatus().getStatus().equals("VALID"))
                return menu.getItems().stream().map(itemService::convertItemToDto).collect(Collectors.toList());
        }
        throw new NullPointerException("VALID ITEMS");
    }

    @Override
    public MenuDto findById(Integer id){
        Optional<Menu> optionalMenu = menuRepository.findById(id);
        if (optionalMenu.isPresent()){
            Menu menu = optionalMenu.get();
            validateMenu(menu);
            if (menu.getStatus().getStatus().equals("VALID"))
                return convertMenuToDto(menu);
        }
        throw new NullPointerException("MENU WITH ID" + id);
    }
    //find also invalid menus for manager
    @Override
    public MenuDto findByIdForManager(Integer id){
        Optional<Menu> optionalMenu = menuRepository.findById(id);
        if (optionalMenu.isPresent()){
            Menu menu = optionalMenu.get();
            if (!menu.getStatus().getStatus().equals("DELETED"))
                return convertMenuToDto(menu);
        }
        throw new NullPointerException("MENU WITH ID" + id);
    }
    @Override
    public List<MenuDto> findAll(){
        for (Menu menu: menuRepository.findAll())
            validateMenu(menu);
        Status status = statusRepository.findStatusByStatus("VALID");
        if (status.getMenuList() == null)
            return new ArrayList<>();
        List<Menu> menuList = status.getMenuList();
        return menuList.stream().map(this::convertMenuToDto).collect(Collectors.toList());
    }
    @Override
    public List<MenuDto> findAllForAdmin(){
        Status statusValid = statusRepository.findStatusByStatus("VALID");
        Status statusInvalid = statusRepository.findStatusByStatus("INVALID");
        if (statusInvalid.getMenuList() == null && statusValid.getMenuList() == null)
            return new ArrayList<>();
        List<Menu> menuList = statusValid.getMenuList();
        menuList.addAll(statusInvalid.getMenuList());
        return menuList.stream().map(this::convertMenuToDto).collect(Collectors.toList());
    }
    //deleting items of a deleted menu
    private void deleteItemsFromMenu(Menu menu){
        if (menu.getItems()!=null){
            List<Item> itemList = menu.getItems();
            for (Item item:itemList) {
                if (!menu.getStatus().getStatus().equals("DELETED")
                        &&!item.getStatus().getStatus().equals("DELETED")) {
                    itemService.deleteById(item.getId());
                    logger.info("Deleting items from menu");
                    itemService.saveFromRepository(item);
                }
            }
        }
    }
    @Override
    public void deleteById(Integer id) {
        Menu menu = findByIdAll(id);
        if (menu == null)
            throw new NullPointerException("MENU WITH ID " + id);
        Status status = statusRepository.findStatusByStatus("DELETED");
        deleteItemsFromMenu(menu);
        menu.setStatus(status);
        logger.info("Deleted menu with id " + id);
        menuRepository.save(menu);
    }
    @Override
    public List<MenuDto> deleteAll() {
        List<MenuDto> menuDtoList = findAll();
        List<Menu> menuList = menuRepository.findAll();
        Status status = statusRepository.findStatusByStatus("DELETED");
        for (Menu menu: menuList) {
            deleteItemsFromMenu(menu);
            menu.setStatus(status);
            menuRepository.save(menu);
        }
        logger.info("Deleted all menus");
        return menuDtoList;
    }
    private void breakfastValidation(Menu menu, LocalTime localTime, Status valid, Status invalid){
        LocalTime startBreakfast = LocalTime.of(7,0);
        LocalTime endBreakfast = LocalTime.of(11,0);
        if (localTime.isAfter(startBreakfast) && localTime.isBefore(endBreakfast))
            menu.setStatus(valid);
        else
            menu.setStatus(invalid);
    }
    private void lunchValidation(Menu menu, LocalTime localTime, Status valid, Status invalid){
        LocalTime startLunch = LocalTime.of(11,0);
        LocalTime endLunch = LocalTime.of(16,0);
        if (localTime.isAfter(startLunch) && localTime.isBefore(endLunch))
            menu.setStatus(valid);
        else
            menu.setStatus(invalid);
    }
    private void afternoonValidation(Menu menu, LocalTime localTime, Status valid, Status invalid){
        LocalTime startAfternoon = LocalTime.of(16,0);
        LocalTime endAfternoon = LocalTime.of(19,0);
        if (localTime.isAfter(startAfternoon) && localTime.isBefore(endAfternoon))
            menu.setStatus(valid);
        else
            menu.setStatus(invalid);
    }
    private void dinnerValidation(Menu menu, LocalTime localTime, Status valid, Status invalid){
        LocalTime startDinner = LocalTime.of(19,0);
        LocalTime endDinner = LocalTime.of(23,59);
        if (localTime.isAfter(startDinner) && localTime.isBefore(endDinner))
            menu.setStatus(valid);
        else
            menu.setStatus(invalid);
    }
    //validating activity of a menu
    @Override
    public void validateMenu(Menu menu){
        Status validStatus = statusRepository.findStatusByStatus("VALID");
        Status invalidStatus = statusRepository.findStatusByStatus("INVALID");
        LocalTime localTime = LocalTime.now();
        if (menu.getStatus().getStatus().equals("DELETED"))
            return;
        if (menu.getMenuType().getMenuType().equals("BREAKFAST"))
            breakfastValidation(menu,localTime,validStatus,invalidStatus);
        else if (menu.getMenuType().getMenuType().equals("LUNCH"))
            lunchValidation(menu,localTime,validStatus,invalidStatus);
        else if (menu.getMenuType().getMenuType().equals("AFTERNOON"))
            afternoonValidation(menu,localTime,validStatus,invalidStatus);
        else if (menu.getMenuType().getMenuType().equals("DINNER"))
            dinnerValidation(menu,localTime,validStatus,invalidStatus);
        logger.info("Menu validated");
        menuRepository.save(menu);
    }
    //saving or updating a menu
    @Override
    public MenuDto save(MenuDto menuDto) throws Exception{
        Menu menu;
        if (menuDto.getId() == null)
            //save
            menu = convertDtoToMenuAdd(menuDto);
        else
            //update
            menu = convertDtoToMenuUpdate(menuDto);
        Status status = statusRepository.findStatusByStatus("INVALID");
        menu.setStatus(status);
        //validating menu when saving it
        validateMenu(menu);
        menuRepository.save(menu);
        return convertMenuToDto(menu);
    }
    @Override
    public MenuDto saveFromRepository(Menu menu){
        validateMenu(menu);
        menu = menuRepository.save(menu);
        return convertMenuToDto(menu);
    }
    private Menu convertDtoToMenuAdd(MenuDto menuDto) throws Exception{
        Menu menu = new Menu();
        if (menuDto.getName() == null || menuDto.getMenuType() == null || menuDto.getRestaurant() == null)
            throw new InvalidDataException("All fields are required");
        return setMenu(menu,menuDto);
    }
    private void setMenuName(Menu menu,MenuDto menuDto) throws Exception{
        List<Menu> menuList = menuRepository.findAllByName(menuDto.getName());
        if (menuList != null){
            for (Menu menu1:menuList) {
                if (menu1.getStatus().getStatus().equals("DELETED"))
                    continue;
                if (menu1.getRestaurant() != null) {
                    if (menu1.getRestaurant().getName().equals(menuDto.getRestaurant()))
                        throw new InvalidDataException("Menu with this name exists in this restaurant");
                }
            }
        }
        if (menuDto.getName().length()<3)
            throw new InvalidDataException("Menu name cannot be that short");
        menu.setName(menuDto.getName());
    }
    private void setMenuType(MenuDto menuDto, Menu menu) throws Exception{
        MenuType menuType = menuTypeRepository.findMenuTypeByMenuType(menuDto.getMenuType());
        //for updating the menu type
        if (menu.getRestaurant()!=null)
            menuDto.setRestaurant(menu.getRestaurant().getName());
        if (menuType == null || menuDto.getRestaurant() == null)
            throw new InvalidDataException("Menu type doesnt exist");
        Restaurant restaurant = restaurantService.findByName(menuDto.getRestaurant());
        if (restaurant == null)
            throw new InvalidDataException("Restaurant doesnt exist");
        if (restaurant.getMenuList()!=null){
            for (Menu menu1: restaurant.getMenuList()) {
                if (menu1.getMenuType().equals(menuType)
                        && !menu1.getStatus().getStatus().equals("DELETED"))
                    throw new InvalidDataException("Restaurant already has this type of menu");
            }
        }
        menu.setMenuType(menuType);
    }
    private void setMenuRestaurant(Menu menu, MenuDto menuDto) throws Exception{
        Restaurant restaurant = restaurantService.findByName(menuDto.getRestaurant());
        if (restaurant == null)
            throw new InvalidDataException("Restaurant doesnt exist");
        menu.setRestaurant(restaurant);
    }
    private Menu setMenu(Menu menu, MenuDto menuDto) throws Exception{
        setMenuName(menu,menuDto);
        setMenuType(menuDto,menu);
        setMenuRestaurant(menu,menuDto);
        logger.info("Added new menu");
        return menu;
        }
        private Menu convertDtoToMenuUpdate(MenuDto menuDto) throws Exception{
            Menu menu = findByIdAll(menuDto.getId());
            if (menu == null)
                throw new NullPointerException("MENU WITH ID" + menuDto.getId());
            if (menuDto.getName() != null)
                setMenuName(menu, menuDto);
            if (menuDto.getMenuType() != null)
                setMenuType(menuDto, menu);
            if (menuDto.getRestaurant() != null)
                setMenuRestaurant(menu, menuDto);
            logger.info("Updated menu with id" + menuDto.getId());
            return menu;
        }
        @Override
        public MenuDto convertMenuToDto(Menu menu){
            MenuDto menuDto = new MenuDto();
            if (!menu.getStatus().getStatus().equals("DELETED"))
                validateMenu(menu);
            if (menu.getMenuType()!=null)
                menuDto.setMenuType(menu.getMenuType().getMenuType());
            if (menu.getName()!=null)
                menuDto.setName(menu.getName());
            if (menu.getRestaurant()!=null)
                menuDto.setRestaurant(menu.getRestaurant().getName());
            if (menu.getItems() != null){
                setItems(menu,menuDto);
            }
            return menuDto;
        }
        private void setItems(Menu menu, MenuDto menuDto){
            List<Item> items = menu.getItems();
            List<ItemDto> itemDtoList = new ArrayList<>();
            for (Item item:items) {
                if (item.getStatus().getStatus().equals("VALID")) {
                    ItemDto itemDto = itemService.convertItemToDto(item);
                    itemDtoList.add(itemDto);
                }
            }
            menuDto.setItemList(itemDtoList);
        }
    }

