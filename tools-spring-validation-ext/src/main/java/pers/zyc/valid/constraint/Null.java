package pers.zyc.valid.constraint;

import java.lang.annotation.*;

/**
 * 为null
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Null {
    Basic basic() default @Basic;
}
