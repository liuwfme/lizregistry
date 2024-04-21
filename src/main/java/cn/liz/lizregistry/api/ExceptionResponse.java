package cn.liz.lizregistry.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {
    private HttpStatus httpStatus;
    private String message;

}
