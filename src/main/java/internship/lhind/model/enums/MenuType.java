package internship.lhind.model.enums;

public enum MenuType {
    BREAKFAST, LUNCH, AFTERNOON, DINNER;
    public static MenuType getBreakfast() {
        return MenuType.BREAKFAST;
    }
    public static MenuType getLunch() {
        return MenuType.LUNCH;
    }
    public static MenuType getAfternoon() {
        return MenuType.AFTERNOON;
    }
    public static MenuType getDinner() {
        return MenuType.DINNER;
    }
}
