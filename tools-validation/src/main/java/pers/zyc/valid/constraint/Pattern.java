package pers.zyc.valid.constraint;

import java.lang.annotation.*;

/**
 * string必须符合指定的正则
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Pattern {
    String regex();
    Basic basic() default @Basic;
}
