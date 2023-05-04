package org.su18.ysuserial;

import java.io.*;
import java.util.concurrent.Callable;

import static org.su18.ysuserial.payloads.config.Config.*;
import static org.su18.ysuserial.payloads.util.Reflections.getFieldValue;
import static org.su18.ysuserial.payloads.util.Reflections.getMethodAndInvoke;

public class Serializer implements Callable<byte[]> {

	private final Object object;

	public Serializer(Object object) {
		this.object = object;
	}

	public byte[] call() throws Exception {
		return serialize(object);
	}

	public static byte[] serialize(final Object obj) throws Exception {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		serialize(obj, out);
		return out.toByteArray();
	}

	public static void serialize(Object obj, final OutputStream out) throws Exception {
		final ObjectOutputStream objOut;

		if (IS_DIRTY_IN_TC_RESET) {
			objOut = new SuObjectOutputStream(out);
		} else {
			objOut = new ObjectOutputStream(out);
		}

		objOut.writeObject(obj);
	}

	public static class SuObjectOutputStream extends ObjectOutputStream {

		public SuObjectOutputStream(OutputStream out) throws IOException {
			super(out);
		}

		@Override
		protected void writeStreamHeader() throws IOException {
			super.writeStreamHeader();
			try {
				// 写入
				for (int i = 0; i < DIRTY_LENGTH_IN_TC_RESET; i++) {
					getMethodAndInvoke(getFieldValue(this, "bout"), "writeByte", new Class[]{int.class}, new Object[]{TC_RESET});
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
