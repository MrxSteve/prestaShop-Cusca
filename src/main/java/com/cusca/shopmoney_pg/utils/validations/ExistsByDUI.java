package com.cusca.shopmoney_pg.utils.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ExistsByDUIValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistsByDUI {
    String message() default "Ya existe un usuario con este DUI, por favor escoja otro!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
