package internship.lhind.model.enums;

public enum Role {
    ADMIN, RESTAURANT_MANAGER, CLIENT;
    public static Role getAdmin(){
        return Role.ADMIN;
    }
    public static Role getRestaurantManager(){
        return Role.RESTAURANT_MANAGER;
    }
    public static Role getClient(){
        return Role.CLIENT;
    }
}
