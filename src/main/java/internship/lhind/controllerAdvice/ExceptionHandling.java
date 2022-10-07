package internship.lhind.controllerAdvice;

import internship.lhind.customException.*;
import internship.lhind.model.dto.ErrorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;

@ControllerAdvice
public class ExceptionHandling {
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointerException(NullPointerException nullPointerException){
        ErrorDto errorDto = new ErrorDto();
        if (nullPointerException.getMessage() == null)
            errorDto.setMessage("DATA NOT FOUND");
        else
            errorDto.setMessage(nullPointerException.getMessage() + " NOT FOUND");
        errorDto.setStatus("NOT_FOUND");
        return  ResponseEntity.status(404).body(errorDto);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(){
        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage("You are not authorized to access this API");
        errorDto.setStatus("UNAUTHORIZED");
        return  ResponseEntity.status(401).body(errorDto);
    }
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<?> handleInvalidDataException(InvalidDataException invalidDataException){
        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage(invalidDataException.getMessage());
        errorDto.setStatus("BAD_REQUEST");
        return  ResponseEntity.status(400).body(errorDto);
    }
    @ExceptionHandler(DeleteException.class)
    public ResponseEntity<?> handleDeleteException(DeleteException deleteException){
        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage("Could not delete " + deleteException.getMessage());
        errorDto.setStatus("NOT_FOUND");
        return  ResponseEntity.status(404).body(errorDto);
    }
    @ExceptionHandler(SaveException.class)
    public ResponseEntity<?> handleSaveException(SaveException saveException){
        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage("Could not save " + saveException.getMessage());
        errorDto.setStatus("BAD_REQUEST");
        return  ResponseEntity.status(400).body(errorDto);
    }
    @ExceptionHandler(UpdateException.class)
    public ResponseEntity<?> handleUpdateException(UpdateException updateException){
        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage("Could not update " + updateException.getMessage());
        errorDto.setStatus("BAD_REQUEST");
        return  ResponseEntity.status(400).body(errorDto);
    }
}
