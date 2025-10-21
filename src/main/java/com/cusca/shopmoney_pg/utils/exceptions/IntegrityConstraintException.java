package com.cusca.shopmoney_pg.utils.exceptions;

public class IntegrityConstraintException extends RuntimeException {
    public IntegrityConstraintException(String message) {
        super(message);
    }
}
