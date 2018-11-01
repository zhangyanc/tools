package pers.zyc.valid;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class ManualValidator<T> extends GenericSupport<T> implements Validator {

    @Override
    public void validate(Object target, Errors errors) {
        try {
            doValidate(sptClass.cast(target));
        } catch (ValidationError error) {
            errors.reject(error.getErrorCode(), error.getErrorArgs(), error.getDefaultMessage());
        }
    }

    protected abstract void doValidate(T target) throws ValidationError;
}