package com.cusca.shopmoney_pg.utils.validations.role;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ExistsByNombreValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistsByNombre {
    String message() default "Ya existe un rol con ese nombre, por favor escoja otro!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
