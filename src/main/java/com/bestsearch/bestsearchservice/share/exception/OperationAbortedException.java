package com.bestsearch.bestsearchservice.share.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.OK)
public class OperationAbortedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OperationAbortedException(String msg) {
        super(msg);
    }
}
