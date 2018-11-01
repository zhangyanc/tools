package pers.zyc.valid.constraint;

import java.lang.annotation.*;

/**
 * 不为null
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotNull {
    Basic basic() default @Basic;
}
