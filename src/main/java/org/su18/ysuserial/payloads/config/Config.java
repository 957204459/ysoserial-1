package org.su18.ysuserial.payloads.config;


import javassist.ClassPool;

import java.util.HashMap;

/**
 * @author su18
 */
public class Config {


	// 是否使用混淆技术
	public static Boolean IS_OBSCURE = false;

	// 恶意类是否继承 AbstractTranslet
	public static Boolean IS_INHERIT_ABSTRACT_TRANSLET = false;

	// 是否在序列化数据流中的 TC_RESET 中填充脏数据
	public static Boolean IS_DIRTY_IN_TC_RESET = false;

	// 命令执行回显时，传递执行命令的 Header 头
	public static String CMD_HEADER_STRING = "X-Token-Data";

	// 填充的脏数据长度
	public static int DIRTY_LENGTH_IN_TC_RESET = 0;

	// 是否使用落地文件的方式隐藏内存马
	public static Boolean HIDE_MEMORY_SHELL = false;

	// 落地文件姿势，1 charsets.jar 2 classes
	public static int HIDE_MEMORY_SHELL_TYPE = 0;

	// 是否强制使用 org.apache.XXX.TemplatesImpl
	public static Boolean FORCE_USING_ORG_APACHE_TEMPLATESIMPL = false;

	// 在 Transformer[] 中使用 org.mozilla.javascript.DefiningClassLoader
	public static Boolean USING_MOZILLA_DEFININGCLASSLOADER = false;

	// 各种方式的内存马映射的路径
	public static String URL_PATTERN = "/version.txt";

	// DefineClassFromParameter 的路径
	public static String PARAMETER = "dc";

	// 将输入直接写在文件里
	public static String FILE = "out.ser";

	public static Boolean WRITE_FILE = false;

	// 内存马的密码 md5 前 16 位
	public static String PASSWORD = "0f359740bd1cda99";

	// 密码原文
	public static String PASSWORD_ORI = "p@ssw0rd";

	// 哥斯拉的 key，默认是 key
	public static String GODZILLA_KEY = "3c6e0b8a9c15224a";

	// 用于额外校验的 Http Header 头，默认 Referer
	public static String HEADER_KEY = "Referer";

	// 用于额外校验的 Http Header 值，默认值 https://su18.org/
	public static String HEADER_VALUE = "https://su18.org/";

	// 是否生成内存马文件
	public static Boolean GEN_MEM_SHELL = false;

	// 内存马文件名
	public static String GEN_MEM_SHELL_FILENAME = "";

	// ScriptEngineManager 是否为 RHINO 引擎
	public static boolean USING_RHINO = false;

	public static ClassPool POOL = ClassPool.getDefault();

	// 不同类型内存马的父类/接口与其关键参数的映射
	public static HashMap<String, String> KEY_METHOD_MAP = new HashMap<String, String>();


	static {
		// Servlet 型内存马，关键方法 service
		KEY_METHOD_MAP.put("javax.servlet.Servlet", "service");
		// Filter 型内存马，关键方法 doFilter
		KEY_METHOD_MAP.put("javax.servlet.Filter", "doFilter");
		// Listener 型内存马，通常使用 ServletRequestListener， 关键方法 requestInitializedHandle
		KEY_METHOD_MAP.put("javax.servlet.ServletRequestListener", "requestInitializedHandle");
		// Websocket 型内存马，关键方法 onMessage
		KEY_METHOD_MAP.put("javax.websocket.MessageHandler$Whole", "onMessage");
		// Tomcat Upgrade 型内存马，关键方法 accept
		KEY_METHOD_MAP.put("org.apache.coyote.UpgradeProtocol", "accept");
		// Tomcat Executor 型内存马，关键方法 execute
		KEY_METHOD_MAP.put("org.apache.tomcat.util.threads.ThreadPoolExecutor", "execute");
		// Spring Interceptor 型内存马，关键方法 preHandle
		KEY_METHOD_MAP.put("org.springframework.web.servlet.handler.HandlerInterceptorAdapter", "preHandle");
		// Webflux 内存马
		KEY_METHOD_MAP.put("org.springframework.web.server.WebFilter", "executePayload");
	}


}
