/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.util;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * @author jfischer
 * 
 */
public final class Currency_CustomFieldSerializer {

	public static void deserialize(SerializationStreamReader streamReader, Currency instance)
    throws SerializationException {
		//do nothing
	}

	public static void serialize(SerializationStreamWriter streamWriter, Currency instance)
    throws SerializationException {
		streamWriter.writeString(instance.getCurrencyCode());
	}
	
	public static Currency instantiate(SerializationStreamReader streamReader)
    throws SerializationException {
	    return Currency.getInstance(streamReader.readString());
	}
}
