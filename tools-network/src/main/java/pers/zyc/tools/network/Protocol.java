package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

/**
 * @author zhangyancheng
 */
public interface Protocol {

	Charset UTF_8 = Charset.forName("UTF8");

	/**
	 * 协议类型
	 */
	int getType();

	/**
	 * 预计长度(编码时使用)
	 */
	int getEstimatedSize();

	/**
	 * 数据校验, 在编码前以及解码后调用
	 *
	 * @throws Exception 校验异常
	 */
	void validate() throws Exception;

	/**
	 * 协议实体编码到ByteBuf
	 */
	void encode(ByteBuf byteBuf) throws Exception;

	/**
	 * 从ByteBuf解码出协议实体
	 */
	void decode(ByteBuf byteBuf) throws Exception;
}
