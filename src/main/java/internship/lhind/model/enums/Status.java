package internship.lhind.model.enums;

public enum Status {
    VALID, INVALID, DELETED;

    public static Status getValid() {
        return Status.VALID;
    }

    public static Status getInvalid() {
        return Status.INVALID;
    }

    public static Status getDeleted() {
        return Status.DELETED;
    }
}
