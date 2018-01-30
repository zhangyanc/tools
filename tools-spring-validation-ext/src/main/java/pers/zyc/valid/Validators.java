package pers.zyc.valid;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import pers.zyc.valid.constraint.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public final class Validators {
    private Validators() {
    }

    private static final AnnValidator NULL = new BaseAnnValidator<Null>() {
        @Override
        protected boolean doValidate(Null non, Object fieldVal) {
            return fieldVal == null;
        }
    };

    private static final AnnValidator NOT_NULL = new BaseAnnValidator<NotNull>() {
        @Override
        protected boolean doValidate(NotNull notNull, Object fieldVal) {
            return fieldVal != null;
        }
    };

    private static final AnnValidator NOT_BLANK = new BaseAnnValidator<NotBlank>() {
        @Override
        protected boolean doValidate(NotBlank notBlank, Object fieldVal) {
            String val;
            return fieldVal != null && fieldVal instanceof String &&
                    !(val = (String) fieldVal).isEmpty() && !val.trim().isEmpty();
        }
    };

    private static final ConcurrentMap<String, java.util.regex.Pattern> REGEX_PATTERNS = new ConcurrentHashMap<>();

    private static java.util.regex.Pattern getRegexPattern(String regex) {
        java.util.regex.Pattern pattern = REGEX_PATTERNS.get(regex);
        if (pattern == null) {
            pattern = java.util.regex.Pattern.compile(regex);
            REGEX_PATTERNS.put(regex, pattern);
        }
        return pattern;
    }

    private static final AnnValidator PATTERN = new BaseAnnValidator<Pattern>() {

        @Override
        protected boolean doValidate(Pattern pattern, Object fieldVal) {
            return fieldVal != null && fieldVal instanceof String &&
                    getRegexPattern(pattern.regex()).matcher((String) fieldVal).matches();
        }
    };

    private static final AnnValidator SIZE = new BaseAnnValidator<Size>() {
        @Override
        protected boolean doValidate(Size size, Object fieldVal) {
            int s = -1;
            if (fieldVal instanceof Collection) {
                s = ((Collection<?>) fieldVal).size();
            } else if (fieldVal instanceof Map) {
                s = ((Map<?, ?>) fieldVal).size();
            } else if (fieldVal instanceof String) {
                s = ((String) fieldVal).length();
            } else if (fieldVal.getClass().isArray()) {
                s = Array.getLength(fieldVal);
            }
            return s != -1 && s >= size.min() && s <= size.max();
        }
    };

    private static final AnnValidator TRUE = new BaseAnnValidator<True>() {
        @Override
        protected boolean doValidate(True aTrue, Object fieldVal) {
            return Boolean.TRUE.equals(fieldVal);
        }
    };

    private static final AnnValidator FALSE = new BaseAnnValidator<False>() {
        @Override
        protected boolean doValidate(False aFalse, Object fieldVal) {
            return Boolean.FALSE.equals(fieldVal);
        }
    };

    private static final AnnValidator BETWEEN = new BaseAnnValidator<Between>() {
        @Override
        protected boolean doValidate(Between between, Object fieldVal) {
            if (fieldVal instanceof Date) {
                Date date = (Date) fieldVal;
                SimpleDateFormat sdf = new SimpleDateFormat(between.pattern());
                try {
                    String start = between.start(), end = between.end();
                    return (start.isEmpty() || sdf.parse(start).compareTo(date) <= 0) &&
                            (end.isEmpty() || sdf.parse(end).compareTo(date) >= 0);
                } catch (ParseException ignore) {
                }
            }
            return false;
        }
    };

    private static final AnnValidator PAST = new BaseAnnValidator<Past>() {
        @Override
        protected boolean doValidate(Past past, Object fieldVal) {
            return fieldVal instanceof Date && ((Date) fieldVal).compareTo(new Date()) < 0;
        }
    };

    private static final AnnValidator FUTURE = new BaseAnnValidator<Future>() {
        @Override
        protected boolean doValidate(Future future, Object fieldVal) {
            return fieldVal instanceof Date && ((Date) fieldVal).compareTo(new Date()) > 0;
        }
    };

    private static final AnnValidator DIGITAL = new BaseAnnValidator<Digital>() {
        @Override
        protected boolean doValidate(Digital digital, Object fieldVal) {
            Class<?> type = fieldVal.getClass();
            String minStr = digital.range().min();
            String maxStr = digital.range().max();
            if (Double.class.isAssignableFrom(type)) {
                double number = (double) fieldVal;
                return number >= Double.parseDouble(minStr) && number <= Double.parseDouble(maxStr);
            }
            if (Float.class.isAssignableFrom(type)) {
                float number = (float) fieldVal;
                return number >= Float.parseFloat(minStr) && number <= Float.parseFloat(maxStr);
            }
            if (Long.class.isAssignableFrom(type)) {
                long number = (long) fieldVal;
                return number >= Long.parseLong(minStr) && number <= Long.parseLong(maxStr);
            }
            if (Integer.class.isAssignableFrom(type)) {
                int number = (int) fieldVal;
                return number >= Integer.parseInt(minStr) && number <= Integer.parseInt(maxStr);
            }
            if (Short.class.isAssignableFrom(type)) {
                short number = (short) fieldVal;
                return number >= Short.parseShort(minStr) && number <= Short.parseShort(maxStr);
            }
            return false;
        }
    };

    private static boolean groupMatch(Class<?>[] hitClasses, Class<?>[] annGroups) {
        if (annGroups.length == 0) {
            return true;
        }
        for (Class<?> hitClass : hitClasses) {
            for (Class<?> annGroup : annGroups) {
                if (hitClass.equals(annGroup)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Validators newInstance() {
        return new Validators();
    }

    private Set<AnnValidator> validators = new CopyOnWriteArraySet<AnnValidator>() {
        {
            addAll(Arrays.asList(
                    NULL, NOT_NULL, NOT_BLANK,
                    PATTERN, SIZE, DIGITAL,
                    TRUE, FALSE,
                    BETWEEN, PAST, FUTURE));
        }
    };

    private AnnValidator selectValidator(Class<? extends Annotation> annClass) {
        for (AnnValidator vr : validators) {
            if (vr.supports(annClass)) {
                return vr;
            }
        }
        return null;
    }

    private static Object getFieldVal(Field field, Object obj) {
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, obj);
    }

    public void validate(Object target, Class<?>[] validationHints) throws ValidationError {
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Annotation[] fieldAnns = field.getAnnotations();
            for (Annotation fieldAnn : fieldAnns) {
                AnnValidator validator = selectValidator(fieldAnn.annotationType());
                if (validator != null) {
                    Basic basic = (Basic) AnnotationUtils.getValue(fieldAnn, "basic");
                    if (validationHints == null || basic == null || groupMatch(validationHints, basic.groups())) {
                        boolean valid;
                        try {
                            valid = validator.validate(fieldAnn, getFieldVal(field, target));
                        } catch (Exception e) {
                            throw new ValidationError(e.getClass().getName(), null, e.getMessage());
                        }
                        if (!valid) {
                            String errorCode = basic == null ? "" : basic.errorCode();
                            String defaultErrorMsg = basic == null ? "" : basic.defaultErrorMsg();
                            throw new ValidationError(errorCode, null, defaultErrorMsg);
                        }
                    }
                }
            }
        }
    }

    public void addValidator(AnnValidator validator) {
        validators.add(validator);
    }

    public void addValidator(Collection<AnnValidator> validatorsCollection) {
        validators.addAll(validatorsCollection);
    }
}