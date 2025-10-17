package com.cusca.shopmoney_pg.utils.validations.user;

import com.cusca.shopmoney_pg.services.auth.IUserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistsByDUIValidator implements ConstraintValidator<ExistsByDUI, String> {
    private final IUserService iUserService;

    @Override
    public boolean isValid(String dui, ConstraintValidatorContext constraintValidatorContext) {
        if (dui == null || dui.trim().isEmpty()) {
            return true;
        }

        return !iUserService.existsByDui(dui);
    }
}
