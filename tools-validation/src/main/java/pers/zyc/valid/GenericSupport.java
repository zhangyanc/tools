package pers.zyc.valid;


import java.lang.reflect.ParameterizedType;

public abstract class GenericSupport<S> {
    Class<S> sptClass;

    @SuppressWarnings("unchecked")
    public boolean supports(Class<?> clazz) {
        if (sptClass == null) {
            sptClass = (Class<S>) ((ParameterizedType)
                    getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return sptClass != null && sptClass.equals(clazz);
    }
}