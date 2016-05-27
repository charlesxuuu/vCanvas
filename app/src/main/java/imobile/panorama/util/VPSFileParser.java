package imobile.panorama.util;

import java.io.InputStream;

public interface VPSFileParser {
	
	/**
	 * 解析输入流 得到VPSFile的对象
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public void parse(InputStream is) throws Exception;
	
	/**
	 * 序列化VPSFile，得到xml格式的字符串
	 * @return
	 * @throws Exception
	 */
	public String serialize() throws Exception;

}
