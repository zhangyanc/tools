package pers.zyc.valid.constraint;

import java.lang.annotation.*;

/**
 * Date必须在规定时间段内(start、end为有效的日期pattern)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Between {
    String pattern() default "yyyy-MM-dd";
    String start() default "";
    String end() default "";
    Basic basic() default @Basic;
}
