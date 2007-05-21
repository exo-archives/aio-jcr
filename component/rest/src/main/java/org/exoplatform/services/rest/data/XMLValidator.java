package org.exoplatform.services.rest.data;

import javax.xml.validation.Validator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.exoplatform.container.xml.*;

public class  XMLValidator {
	private Map <String, Schema> schemas; 

	public XMLValidator(InitParams params) throws SAXException {
		this.schemas = new HashMap<String, Schema>();
		SchemaFactory schfactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    Iterator<ValueParam> i = params.getValueParamIterator();
  	while(i.hasNext()) {
  		ValueParam v = i.next();
  		File schemaLocation = new File(v.getValue());
			schemas.put(v.getName(), schfactory.newSchema(schemaLocation));
  	}
	}
	
	public String validate(InputStream in) {
		Source source = new StreamSource(in);
		Set<String> keys = schemas.keySet();
		String key = null;
		for(String k : keys) {
			Schema schema = schemas.get(k);
			Validator validator = schema.newValidator();
			try {
				validator.validate(source);
				key = k;
				break;
			} catch (SAXException saxe) {
				return null;
			} catch (IOException ioe) {
				return null;
			}
		}
		return key; 
	}
}
