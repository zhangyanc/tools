package pers.zyc.valid.constraint;

import java.lang.annotation.*;

/**
 * 注释的string非"空(null、length==0、trim().length==0)"
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotBlank {
    Basic basic() default @Basic;
}
