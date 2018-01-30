package pers.zyc.valid;

import org.junit.Assert;
import org.junit.Test;
import pers.zyc.valid.constraint.*;

import java.lang.annotation.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ValidationTest {
    private Validators validators = Validators.newInstance();

    private static class NullOrBlankObj {
        @Null
        private Object o1;
        @Null(basic = @Basic(errorCode = "nullOrBlankObj.o2", defaultErrorMsg = "o2 not null!"))
        private Object o2;

        @NotNull
        private Object o3 = new Object();
        @NotNull(basic = @Basic(errorCode = "nullOrBlankObj.o4", defaultErrorMsg = "o4 null!"))
        private Object o4 = o3;

        @NotBlank
        private String s0 = "s0";
        @NotBlank(basic = @Basic(errorCode = "nullOrBlankObj.s1", defaultErrorMsg = "s1 blank!"))
        private String s1 = "s1";
    }

    @Test
    public void nullOrBlankTestCase0() {
        NullOrBlankObj obj = new NullOrBlankObj();
        try {
            validators.validate(obj, null);
        } catch (ValidationError validationError) {
            Assert.fail();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nullOrBlankTestCase1() {
        NullOrBlankObj obj = new NullOrBlankObj();
        obj.o1 = new Object();
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nullOrBlankTestCase2() {
        NullOrBlankObj obj = new NullOrBlankObj();
        obj.o2 = new Object();
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("nullOrBlankObj.o2", validationError.getErrorCode());
            Assert.assertEquals("o2 not null!", validationError.getDefaultMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nullOrBlankTestCase4() {
        NullOrBlankObj obj = new NullOrBlankObj();
        obj.o3 = null;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nullOrBlankTestCase5() {
        NullOrBlankObj obj = new NullOrBlankObj();
        obj.o4 = null;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("nullOrBlankObj.o4", validationError.getErrorCode());
            Assert.assertEquals("o4 null!", validationError.getDefaultMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nullOrBlankTestCase6() {
        NullOrBlankObj obj = new NullOrBlankObj();
        obj.s0 = null;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nullOrBlankTestCase7() {
        NullOrBlankObj obj = new NullOrBlankObj();
        obj.s0 = "";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
    @Test
    public void nullOrBlankTestCase8() {
        NullOrBlankObj obj = new NullOrBlankObj();
        obj.s0 = "  ";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nullOrBlankTestCase9() {
        NullOrBlankObj obj = new NullOrBlankObj();
        obj.s1 = null;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("nullOrBlankObj.s1", validationError.getErrorCode());
            Assert.assertEquals("s1 blank!", validationError.getDefaultMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nullOrBlankTestCase10() {
        NullOrBlankObj obj = new NullOrBlankObj();
        obj.s1 = "";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("nullOrBlankObj.s1", validationError.getErrorCode());
            Assert.assertEquals("s1 blank!", validationError.getDefaultMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nullOrBlankTestCase11() {
        NullOrBlankObj obj = new NullOrBlankObj();
        obj.s1 = "  ";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("nullOrBlankObj.s1", validationError.getErrorCode());
            Assert.assertEquals("s1 blank!", validationError.getDefaultMessage());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private static class DateObj {
        private static final SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM");
        private static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        private static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH");
        private static final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        private static final SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Past(basic = @Basic(errorCode = "DateObj.past", defaultErrorMsg = "not past!"))
        private Date past = sdf1.parse("2000-01-01");

        @Future(basic = @Basic(errorCode = "DateObj.future", defaultErrorMsg = "not future!"))
        private Date future = sdf1.parse("3000-01-01");

        @Between(pattern = "yyyy-MM", start = "2014-01", end = "2014-02",
                basic = @Basic(errorCode = "DateObj.limit0"))
        private Date limit0 = sdf0.parse("2014-01");

        @Between(pattern = "yyyy-MM-dd", start = "2014-01-01", end = "2014-02-02",
                basic = @Basic(errorCode = "DateObj.limit1"))
        private Date limit1 = sdf1.parse("2014-01-01");

        @Between(pattern = "yyyy-MM-dd HH", start = "2014-01-01 10", end = "2014-02-02 20",
                basic = @Basic(errorCode = "DateObj.limit2"))
        private Date limit2 = sdf2.parse("2014-01-01 10");

        @Between(pattern = "yyyy-MM-dd HH:mm", start = "2014-01-01 10:10", end = "2014-02-02 20:20",
                basic = @Basic(errorCode = "DateObj.limit3"))
        private Date limit3 = sdf3.parse("2014-01-01 10:10");

        @Between(pattern = "yyyy-MM-dd HH:mm:ss", start = "2014-01-01 10:10:10", end = "2014-02-02 20:20:20",
                basic = @Basic(errorCode = "DateObj.limit4"))
        private Date limit4 = sdf4.parse("2014-01-01 10:10:10");

        private DateObj() throws ParseException {
        }
    }

    @Test
    public void dateTestCase0() {
        try {
            DateObj obj = new DateObj();
            validators.validate(obj, null);
        } catch (ValidationError validationError) {
            Assert.fail();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase1() {
        try {
            DateObj obj = new DateObj();
            obj.past = DateObj.sdf0.parse("3100-01");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.past", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase2() {
        try {
            DateObj obj = new DateObj();
            obj.future = DateObj.sdf0.parse("2000-01");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.future", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase3() {
        try {
            DateObj obj = new DateObj();
            obj.limit0 = DateObj.sdf0.parse("2013-12");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.limit0", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase4() {
        try {
            DateObj obj = new DateObj();
            obj.limit0 = DateObj.sdf0.parse("2014-03");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.limit0", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase5() {
        try {
            DateObj obj = new DateObj();
            obj.limit1 = DateObj.sdf1.parse("2013-12-01");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.limit1", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase6() {
        try {
            DateObj obj = new DateObj();
            obj.limit1 = DateObj.sdf1.parse("2014-02-03");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.limit1", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase7() {
        try {
            DateObj obj = new DateObj();
            obj.limit2 = DateObj.sdf2.parse("2014-01-01 09");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.limit2", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase8() {
        try {
            DateObj obj = new DateObj();
            obj.limit2 = DateObj.sdf2.parse("2014-02-02 21");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.limit2", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase9() {
        try {
            DateObj obj = new DateObj();
            obj.limit3 = DateObj.sdf3.parse("2014-01-01 10:09");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.limit3", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase10() {
        try {
            DateObj obj = new DateObj();
            obj.limit3 = DateObj.sdf3.parse("2014-02-02 20:21");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.limit3", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase11() {
        try {
            DateObj obj = new DateObj();
            obj.limit4 = DateObj.sdf4.parse("2014-01-01 10:10:09");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.limit4", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dateTestCase12() {
        try {
            DateObj obj = new DateObj();
            obj.limit4 = DateObj.sdf4.parse("2014-02-02 20:20:21");
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DateObj.limit4", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private static class TrueOrFalseObj {
        @True(basic = @Basic(errorCode = "TrueOrFalseObj.t"))
        private boolean t = true;
        @False(basic = @Basic(errorCode = "TrueOrFalseObj.f"))
        private boolean f = false;
    }

    @Test
    public void tureOrFalseTestCase0() {
        TrueOrFalseObj obj = new TrueOrFalseObj();
        try {
            validators.validate(obj, null);
        } catch (ValidationError validationError) {
            Assert.fail();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void tureOrFalseTestCase1() {
        TrueOrFalseObj obj = new TrueOrFalseObj();
        obj.t = false;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("TrueOrFalseObj.t", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void tureOrFalseTestCase2() {
        TrueOrFalseObj obj = new TrueOrFalseObj();
        obj.f = true;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("TrueOrFalseObj.f", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private static class SizeObj {
        @Size(min = 2, max = 4, basic = @Basic(errorCode = "SizeObj.str"))
        private String str = "abc";
        @Size(min = 2, max = 4, basic = @Basic(errorCode = "SizeObj.arr"))
        private String[] arr = {"a", "b", "c"};
        @Size(min = 2, max = 4, basic = @Basic(errorCode = "SizeObj.map"))
        private Map<String, String> map = new HashMap<String, String>() {
            {
                put("a", "");
                put("b", "");
                put("c", "");
            }
        };
        @Size(min = 2, max = 4, basic = @Basic(errorCode = "SizeObj.list"))
        private List<String> list = new ArrayList<String>() {
            {
                add("a");
                add("b");
                add("c");
            }
        };
    }

    @Test
    public void sizeTestCase0() {
        SizeObj obj = new SizeObj();
        try {
            validators.validate(obj, null);
        } catch (ValidationError validationError) {
            Assert.fail();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void sizeTestCase1() {
        SizeObj obj = new SizeObj();
        obj.str = "a";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("SizeObj.str", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void sizeTestCase2() {
        SizeObj obj = new SizeObj();
        obj.str = "abcde";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("SizeObj.str", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void sizeTestCase3() {
        SizeObj obj = new SizeObj();
        obj.arr = new String[]{"a"};
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("SizeObj.arr", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void sizeTestCase4() {
        SizeObj obj = new SizeObj();
        obj.arr = new String[]{"a", "b", "c", "d", "e"};
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("SizeObj.arr", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void sizeTestCase5() {
        SizeObj obj = new SizeObj();
        obj.map.put("d", "");
        obj.map.put("e", "");
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("SizeObj.map", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void sizeTestCase6() {
        SizeObj obj = new SizeObj();
        obj.map.remove("c");
        obj.map.remove("b");
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("SizeObj.map", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void sizeTestCase7() {
        SizeObj obj = new SizeObj();
        obj.list.add("d");
        obj.list.add("e");
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("SizeObj.list", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void sizeTestCase8() {
        SizeObj obj = new SizeObj();
        obj.list.remove("b");
        obj.list.remove("c");
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("SizeObj.list", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private static class PatternObj {
        @Pattern(regex = "[a-zA-z0-9\\-_\\.]{6,10}")
        private String p = "abcdef";
    }

    @Test
    public void patternTestCase0() {
        PatternObj obj = new PatternObj();
        try {
            validators.validate(obj, null);
        } catch (ValidationError validationError) {
            Assert.fail();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void patternTestCase1() {
        PatternObj obj = new PatternObj();
        obj.p = "abcde";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void patternTestCase2() {
        PatternObj obj = new PatternObj();
        obj.p = "abcde ";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void patternTestCase3() {
        PatternObj obj = new PatternObj();
        obj.p = "abcdefabcdef";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void patternTestCase4() {
        PatternObj obj = new PatternObj();
        obj.p = "abcd%&";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private static class DigitalObj {
        @Digital(range = @Range(min = "3.1415926", max = "3.1415927"), basic = @Basic(errorCode = "DigitalObj.d"))
        private double d = 3.1415926;

        @Digital(range = @Range(min = "3.141592", max = "3.141593"), basic = @Basic(errorCode = "DigitalObj.f"))
        private float f = 3.141592F;

        @Digital(range = @Range(min = "31415926", max = "31415927"), basic = @Basic(errorCode = "DigitalObj.l"))
        private long l = 31415926L;

        @Digital(range = @Range(min = "31415926", max = "31415927"), basic = @Basic(errorCode = "DigitalObj.i"))
        private int i = 31415926;

        @Digital(range = @Range(min = "31415", max = "31416"), basic = @Basic(errorCode = "DigitalObj.s"))
        private short s = 31415;
    }

    @Test
    public void digitalTestCase0() {
        DigitalObj obj = new DigitalObj();
        try {
            validators.validate(obj, null);
        } catch (ValidationError validationError) {
            Assert.fail();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void digitalTestCase1() {
        DigitalObj obj = new DigitalObj();
        obj.d = 3.1415925;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DigitalObj.d", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void digitalTestCase2() {
        DigitalObj obj = new DigitalObj();
        obj.d = 3.1415928;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DigitalObj.d", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void digitalTestCase3() {
        DigitalObj obj = new DigitalObj();
        obj.f = 3.141591F;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DigitalObj.f", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void digitalTestCase4() {
        DigitalObj obj = new DigitalObj();
        obj.f = 3.141594F;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DigitalObj.f", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void digitalTestCase5() {
        DigitalObj obj = new DigitalObj();
        obj.l = 31415925L;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DigitalObj.l", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void digitalTestCase6() {
        DigitalObj obj = new DigitalObj();
        obj.l = 31415928L;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DigitalObj.l", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void digitalTestCase7() {
        DigitalObj obj = new DigitalObj();
        obj.i = 31415925;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DigitalObj.i", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void digitalTestCase8() {
        DigitalObj obj = new DigitalObj();
        obj.i = 31415928;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DigitalObj.i", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void digitalTestCase9() {
        DigitalObj obj = new DigitalObj();
        obj.s = 31414;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DigitalObj.s", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void digitalTestCase10() {
        DigitalObj obj = new DigitalObj();
        obj.s = 31417;
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("DigitalObj.s", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private static class GroupObj {
        interface Group1{}
        interface Group2{}
        interface Group3{}


        @Null(basic = @Basic(groups = Group1.class, errorCode = "GroupObj.s0"))
        private String s0;

        @Null(basic = @Basic(groups = {Group2.class, Group3.class}, errorCode = "GroupObj.s1"))
        private String s1;

        @Null(basic = @Basic(groups = {Group3.class}, errorCode = "GroupObj.s2"))
        private String s2;
    }

    @Test
    public void groupCase0() {
        GroupObj obj = new GroupObj();
        obj.s0 = "notnull";
        obj.s1 = "notnull";
        try {
            validators.validate(obj, new Class<?>[]{GroupObj.Group2.class});
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("GroupObj.s1", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void groupCase1() {
        GroupObj obj = new GroupObj();
        obj.s0 = "notnull";
        obj.s1 = "notnull";
        try {
            validators.validate(obj, new Class<?>[]{GroupObj.Group3.class});
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("GroupObj.s1", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void groupCase2() {
        GroupObj obj = new GroupObj();
        obj.s0 = "notnull";
        try {
            validators.validate(obj, new Class<?>[]{GroupObj.Group1.class});
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("GroupObj.s0", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void groupCase3() {
        GroupObj obj = new GroupObj();
        obj.s0 = "notnull";
        obj.s2 = "notnull";
        try {
            validators.validate(obj, new Class<?>[]{GroupObj.Group3.class});
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("GroupObj.s2", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Equals {
        String value();
    }

    public static class EqualsValidator extends BaseAnnValidator<Equals> {

        @Override
        protected boolean doValidate(Equals ann, Object fieldVal) {
            return ann.value().equals(fieldVal);
        }
    }

    private static class EqualsObj {
        @Equals("s0")
        String s0 = "s0";
    }

    @Test
    public void extValidatorCase0() {
        validators.addValidator(new EqualsValidator());

        EqualsObj obj = new EqualsObj();
        try {
            validators.validate(obj, null);
        } catch (ValidationError validationError) {
            Assert.fail();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void extValidatorCase1() {
        validators.addValidator(new EqualsValidator());

        EqualsObj obj = new EqualsObj();
        obj.s0 = "not s0!";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Inverse {
        String value();
        Basic basic();
    }

    public static class EqualsAndInverseValidator implements AnnValidator {

        @Override
        public boolean supports(Class<?> clazz) {
            return Inverse.class.isAssignableFrom(clazz) || Equals.class.isAssignableFrom(clazz);
        }

        @Override
        public boolean validate(Annotation ann, Object fieldVal) {
            if (fieldVal instanceof  String) {
                String val = (String) fieldVal;
                if (ann instanceof Equals) {
                    return doEqualsValidate((Equals) ann, val);
                }
                if (ann instanceof Inverse) {
                    return doInverseValidate((Inverse) ann, val);
                }
            }
            return false;
        }

        private boolean doEqualsValidate(Equals equals, String fieldVal) {
            return equals.value().equals(fieldVal);
        }

        private boolean doInverseValidate(Inverse inverse, String fieldVal) {
            return new StringBuilder(inverse.value()).reverse().toString().equals(fieldVal);
        }
    }

    private static class EqualsOrInverseObj {
        @Equals("equals")
        String s0 = "equals";

        @Inverse(value = "esrevni", basic = @Basic(errorCode = "EqualsOrInverseObj.s1"))
        String s1 = "inverse";
    }

    @Test
    public void equalsOrInverseCase0() {
        validators.addValidator(new EqualsAndInverseValidator());

        EqualsOrInverseObj obj = new EqualsOrInverseObj();
        obj.s0 = "not equals!";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void equalsOrInverseCase1() {
        validators.addValidator(new EqualsAndInverseValidator());

        EqualsOrInverseObj obj = new EqualsOrInverseObj();
        obj.s1 = "not inverse!";
        try {
            validators.validate(obj, null);
            Assert.fail();
        } catch (ValidationError validationError) {
            Assert.assertEquals("EqualsOrInverseObj.s1", validationError.getErrorCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}