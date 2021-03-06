package pers.zyc.tools.utils;

import java.util.regex.Pattern;

/**
 * 常用正则
 */
public enum Regex {
	/*
	 *   1: 250-255：特点：三位数，百位是2，十位是5，个位是0~5，用正则表达式可以写成：25[0-5]
	 *   2: 200-249：特点：三位数，百位是2，十位是0~4，个位是0~9，用正则表达式可以写成：2[0-4]\d
	 * 3.0:   0-199：这个可以继续分拆，这样写起来更加简单明了
	 * 3.1:     0-9：特点：一位数，个位是0~9，用正则表达式可以写成：\d
	 * 3.2:   10-99：特点：二位数，十位是1~9，个位是0~9，用正则表达式可以写成：[1-9]\d
	 * 3.3: 100-199：特点：三位数，百位是1，十位是0~9，个位是0~9，用正则表达式可以写成：1\d{2}
	 * 于是:
	 *     0-99的正则表达式可以合写为[1-9]?\d
	 *     0-199的正则表达式就可以写成1\d{2}|[1-9]?\d
	 *     0~255的正则表达式就可以写成(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)
	 * 最后:
	 *     后面3段加上句点.可以使用{3}重复得到，第1段再来一次同样的匹配，得到IP地址的正则表达式
	 *     (25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)){3}
	 */
	IPV4("(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}"),

	/**
	 * 增强IP正则,一般用于设计机房的ip段,以支持如下ip表达式
	 * 1. *.0.*.0: 4段中的任一段都可以是通配符
	 * 2. 127-192.0.0.0: 4段中的任一段都可以是一段范围区间,不过范围区间没有校验前后大小!
	 */
	IPV4_SECTIONS("(((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\-(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d))?)|\\*)(" +
			"\\.(((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\-(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d))?)|\\*)){3}"),

	/**
	 * 算术表达式字符, 包括数字、四则运算符和左右括号
	 */
	ARITHMETIC_EXPRESSION_CHARACTER("([1-9]\\d*(\\.\\d*)?)|[\\(\\)\\+\\-\\*\\/]|\\s"),

	/**
	 * 域名正则
	 * 1: label段长度为1到63
	 * 2: label段合法字符为大小写字母、数字、中划线、下划线, 首段可以为通配符(*)
	 * 3: 最后段可以是点号(.)
	 */
	DOMAIN("(\\*|[a-zA-Z0-9\\-_]{1,63})(\\.[a-zA-Z0-9\\-_]{1,63})*\\.?"),

	/**
	 * 一般性code正则(不限制长度)
	 */
	COMMON_CODE("[a-zA-z0-9\\-_\\.]+"),

	/**
	 * zookeeper连接地址正则
	 * host1:2181,host2:2181,host3:2181/root/a
	 */
	ZK_ADDRESS("([a-zA-Z_\\d\\-\\.]+:\\d+,)*[a-zA-Z_\\d\\-\\.]+:\\d+(/[a-zA-Z_\\d\\-]+)*"),

    /**
     * uri中query参数对简单正则(未限制长度、特殊字符等)
     *
     * 1. 首先使用=号分名、值两部分
     * 2. 名部分匹配字母、_、$开头的字符串(数字开头的名将匹配非数字开始的部分8f3=abc匹配到f3)
     * 3. 值部分懒惰匹配&或结尾前的字符串
     */
    URI_QUERY_PAIR("([a-zA-Z_\\$]+\\w*)=(.*?)(&|$)"),

	/**
	 * 日期-时间正则(未限制2月只能有28、29天)
	 */
	DATETIME_FORMAT("\\d{4}\\-?(0[1-9]|1[0-2])\\-?(0[1-9]|[12][0-9]|3[01])( ([01][0-9]|2[0-3])(:?[0-5][0-9]){2})?"),





	;

	private Pattern pattern;

	Regex(String patternStr) {
		this.pattern = Pattern.compile(patternStr);
	}

	/**
	 * @return 正则pattern
	 */
	public Pattern pattern() {
		return pattern;
	}

	/**
	 * @param input 要匹配的文本
	 * @return 是否匹配成功
	 */
	public boolean matches(String input) {
		return pattern.matcher(input).matches();
	}
}
