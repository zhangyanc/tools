package pers.zyc.valid;

import java.lang.annotation.Annotation;

public abstract class BaseAnnValidator<A extends Annotation> extends GenericSupport<A> implements AnnValidator {

    @Override
    public boolean validate(Annotation ann, Object fieldVal) {
        return doValidate(sptClass.cast(ann), fieldVal);
    }

    protected abstract boolean doValidate(A ann, Object fieldVal);
}