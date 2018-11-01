package pers.zyc.valid.constraint;

import java.lang.annotation.*;

/**
 * (Array,Collection,Map,String)长度必须在给定的范围之内
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Size {
    int min() default 0;
    int max() default Integer.MAX_VALUE;

    Basic basic() default @Basic;
}
