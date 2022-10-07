package internship.lhind.model.dto;

import org.springframework.stereotype.Component;

@Component
public class LogOutDto {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
