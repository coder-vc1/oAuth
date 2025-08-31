package com.cloudEagle.DropboxOAuth.error;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApiError {

    private LocalDateTime timeStamp;
    private String error;
    private HttpStatus statusCode;

    public ApiError() {
        this.timeStamp = LocalDateTime.now();
    }

    public ApiError(String error, HttpStatus statusCode) {
        this();
        this.error = error;
        this.statusCode = statusCode;
    }
}
