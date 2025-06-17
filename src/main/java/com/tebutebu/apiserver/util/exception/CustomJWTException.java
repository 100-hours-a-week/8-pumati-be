package com.tebutebu.apiserver.util.exception;

import io.jsonwebtoken.JwtException;

public class CustomJWTException extends JwtException {

    public CustomJWTException(String message) {
        super(message);
    }

}
