package pers.zyc.valid.constraint;

import java.lang.annotation.*;

/**
 * Date必须是将来的时间
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Future {
    Basic basic() default @Basic;
}
