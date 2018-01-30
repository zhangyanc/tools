package pers.zyc.valid.constraint;

import java.lang.annotation.*;

/**
 * 注释Boolean为FALSE
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface False {
    Basic basic() default @Basic;
}
