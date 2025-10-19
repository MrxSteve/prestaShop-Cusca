package com.cusca.shopmoney_pg.utils.exceptions;

public class InvalidSaleStateException extends RuntimeException {
    public InvalidSaleStateException(String message) {
        super(message);
    }
}
