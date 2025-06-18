package com.tebutebu.apiserver.global.errorcode;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getMessage();
    HttpStatus getStatus();
}
