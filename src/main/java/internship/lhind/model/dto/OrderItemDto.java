package internship.lhind.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

@Component
public class OrderItemDto {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer itemId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String itemName;
    private Integer quantity;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String restaurant;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String menu;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String menuType;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

}
