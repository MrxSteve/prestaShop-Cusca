package com.cusca.shopmoney_pg.services.auth;

public interface IUserService {
    boolean existsByEmail(String email);
    boolean existsByDui(String dui);
}
