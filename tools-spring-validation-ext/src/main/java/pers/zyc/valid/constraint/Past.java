package pers.zyc.valid.constraint;

import java.lang.annotation.*;

/**
 * Date必须是过去的时间
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Past {
    Basic basic() default @Basic;
}
