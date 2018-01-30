package pers.zyc.valid;


import java.lang.annotation.Annotation;

public interface AnnValidator {
    boolean supports(Class<?> clazz);
    boolean validate(Annotation ann, Object fieldVal);
}
