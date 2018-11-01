package pers.zyc.valid.constraint;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Basic {
    String errorCode() default "";
    String defaultErrorMsg() default "";
    Class<?>[] groups() default {};
}
