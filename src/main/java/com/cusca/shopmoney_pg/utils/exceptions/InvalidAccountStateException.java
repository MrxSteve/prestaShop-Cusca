package com.cusca.shopmoney_pg.utils.exceptions;

public class InvalidAccountStateException extends RuntimeException {
    public InvalidAccountStateException(String message) {
        super(message);
    }
}
