package vn.heistom.api;

import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class ApiCallException extends Exception {

    private final HttpStatus httpStatus;
    private final String message;

    public ApiCallException(final String message, final HttpStatus status) {
        this.message = message;
        this.httpStatus = status;
    }

    public ApiCallException(final List<String> errorMessages, final HttpStatus status) {
        this.message = String.join(" \n", errorMessages);
        this.httpStatus = status;
    }
}