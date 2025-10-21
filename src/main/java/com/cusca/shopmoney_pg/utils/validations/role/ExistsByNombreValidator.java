package com.cusca.shopmoney_pg.utils.validations.role;

import com.cusca.shopmoney_pg.services.auth.IRolService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistsByNombreValidator implements ConstraintValidator<ExistsByNombre, String> {
    private final IRolService iRolService;

    @Override
    public boolean isValid(String nombre, ConstraintValidatorContext constraintValidatorContext) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return true;
        }

        return !iRolService.existePorNombre(nombre);
    }
}