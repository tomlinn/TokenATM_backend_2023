package io.renren.modules.tokenatm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Resource Not Found")
public class BadRequestException extends Exception{
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException() {}
}
