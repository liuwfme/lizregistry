package cn.liz.lizregistry.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LizExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handlerException(Exception e) {
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
