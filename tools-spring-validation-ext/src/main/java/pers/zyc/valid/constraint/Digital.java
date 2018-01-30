package pers.zyc.valid.constraint;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Digital {
    Range range();
    Basic basic() default @Basic;
}
