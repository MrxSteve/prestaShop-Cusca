package com.cusca.shopmoney_pg.utils.validations.user;

import com.cusca.shopmoney_pg.services.auth.IUserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistsByEmailValidator implements ConstraintValidator<ExistsByEmail, String> {
    private final IUserService iUserService;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        return !iUserService.existsByEmail(email);
    }
}
