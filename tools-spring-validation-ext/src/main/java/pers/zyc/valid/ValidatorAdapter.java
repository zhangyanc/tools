package pers.zyc.valid;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;

import java.util.List;

public class ValidatorAdapter implements SmartValidator, InitializingBean {

    private List<Validator> manualValidators;
    private List<AnnValidator> annValidators;
    private Validators validators = Validators.newInstance();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!CollectionUtils.isEmpty(annValidators)) {
            validators.addValidator(annValidators);
        }
    }

    @Override
    public void validate(Object target, Errors errors, Object... validationHints) {
        try {
            validators.validate(target, (Class<?>[]) validationHints);
        } catch (ValidationError error) {
            errors.reject(error.getErrorCode(), error.getErrorArgs(), error.getDefaultMessage());
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }
        for (Validator validator : manualValidators) {
            if (validator.supports(target.getClass())) {
                validator.validate(target, errors);
                return;
            }
        }
        validate(target, errors, (Object[]) null);
    }

    public void setValidators(List<Validator> validators) {
        this.manualValidators = validators;
    }

    public void setAnnValidators(List<AnnValidator> annValidators) {
        this.annValidators = annValidators;
    }
}