package org.exoplatform.services.rest;

//import java.math.BigDecimal;
import junit.framework.TestCase;

import java.io.FileInputStream;
//import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;

import org.exoplatform.services.rest.generated.*;


public class JAXBGeneratedTest extends TestCase {

  private String inputFile = "src/test/resources/book-in.xml";

  protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testUnmarshalling () throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(Book.class.getPackage().getName());
		Book book = (Book)jaxbContext.createUnmarshaller().unmarshal(new FileInputStream(inputFile));
		assertEquals("Java and XML Data Binding", book.getTitle());
		assertEquals("Brett McLaughlin", book.getAuthor());
		assertEquals("US", book.getPrice().getCurrency());
		assertEquals("US", book.getMemberPrice().getCurrency());
	}
	
}
