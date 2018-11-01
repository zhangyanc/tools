package pers.zyc.valid.constraint;

import java.lang.annotation.*;

/**
 * 注释Boolean为TRUE
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface True {
    Basic basic() default @Basic;
}
