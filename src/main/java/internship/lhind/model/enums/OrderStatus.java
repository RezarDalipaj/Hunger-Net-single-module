package internship.lhind.model.enums;

public enum OrderStatus {
    CREATED,APPROVED,REJECTED,PREPARED,
            WAITING_FOR_DELIVERY,DELIVERED, DELETED;
    public static OrderStatus getCreated() {
        return OrderStatus.CREATED;
    }
    public static OrderStatus getApproved() {
        return OrderStatus.APPROVED;
    }
    public static OrderStatus getRejected() {
        return OrderStatus.REJECTED;
    }
    public static OrderStatus getPrepared() {
        return OrderStatus.PREPARED;
    }
    public static OrderStatus getWaitingForDelivery() {
        return OrderStatus.WAITING_FOR_DELIVERY;
    }
    public static OrderStatus getDelivered() {
        return OrderStatus.DELIVERED;
    }
    public static OrderStatus getDeleted() {
        return OrderStatus.DELETED;
    }
}
