package com.cusca.shopmoney_pg.utils.exceptions;

public class UserHasPendingBalanceException extends RuntimeException {
    public UserHasPendingBalanceException(String message) {
        super(message);
    }
}
