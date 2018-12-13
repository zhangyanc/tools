package pers.zyc.tools.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhangyancheng
 */
public class RegexTest {

	@Test
	public void case_DATETIME_FORMAT() {
		Assert.assertTrue(Regex.DATETIME_FORMAT.matches("0000-01-01 00:00:00"));
		Assert.assertTrue(Regex.DATETIME_FORMAT.matches("00000101 000000"));

		Assert.assertTrue(Regex.DATETIME_FORMAT.matches("9999-12-31 23:59:59"));
		Assert.assertTrue(Regex.DATETIME_FORMAT.matches("99991231 235959"));

		Assert.assertTrue(Regex.DATETIME_FORMAT.matches("2018-12-12 14:46:38"));
		Assert.assertTrue(Regex.DATETIME_FORMAT.matches("20181212 144638"));

		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-12-1a 14:46:38"));
		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-12-00 14:46:38"));
		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-13-12 14:46:38"));
		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-12-32 14:46:38"));
		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-12-12 24:46:38"));
		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-12-12 14:60:38"));
		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-12-12 14:46:60"));
		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-12-12 14:64:381"));


		Assert.assertTrue(Regex.DATETIME_FORMAT.matches("2018-12-31"));
		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-13-13"));
		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-12-32"));
		Assert.assertFalse(Regex.DATETIME_FORMAT.matches("2018-12-31 "));
	}
}
