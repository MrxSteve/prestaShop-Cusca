package com.cusca.shopmoney_pg.utils.exceptions;

public class InvalidSaleTypeException extends RuntimeException {
    public InvalidSaleTypeException(String message) {
        super(message);
    }
}
