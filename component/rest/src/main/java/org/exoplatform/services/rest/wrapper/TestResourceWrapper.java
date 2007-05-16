package org.exoplatform.services.rest.wrapper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Map;

import org.exoplatform.services.rest.data.XMLRepresentation;
import org.exoplatform.services.rest.ResourceRouter;
import org.exoplatform.services.rest.wrapper.http.HTTPMethod;
import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.Response;

  public class TestResourceWrapper extends AbstractResourceWrapper {

  public final static String URIPATTERN = "/calculator/{element1}/{operation}/{element2}/";
  public final static String HTTP_METHOD = "GET";
  private ResourceRouter resRouter;

  public TestResourceWrapper(ResourceRouter resRouter) {
    super(resRouter);
    this.resRouter = resRouter;
  }
  
  @HTTPMethod(name=HTTP_METHOD, uri=URIPATTERN)
  public void method(Request req, Response res) {
    Map params = req.getResourceIdentifier().getParameters();
    char oper = params.get(new String("operation")).toString().charAt(0);
    
    float el1 = Float.valueOf(params.get(new String("element1")).toString());
    float el2 = Float.valueOf(params.get(new String("element2")).toString().replace("/", ""));
    float result;
    switch(oper){
    case '+':
      result = el1 + el2;
      System.out.println(el1 + " + " + el2 + " = " + result);
      break;
    case '-':
      result = el1 - el2;
      System.out.println(el1 + " - " + el2 + " = " + result);
      break;
    case '*':
      result = el1 * el2;
      System.out.println(el1 + " * " + el2 + " = " + result);
      break;
    case ':':
      result = el1 / el2;
      System.out.println(el1 + " / " + el2 + " = " + result);
      break;
      default:
        result = 0;
        System.out.println("unknown operation");
    }
    DocumentBuilder docBuilder = null;
    try {
      docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }catch(Exception e) {;}
    Document doc = docBuilder.newDocument();
    Element e = doc.createElement("root");
    doc.appendChild(e);
    e.appendChild(doc.createElement("element1")).setTextContent(String.valueOf(el1));
    e.appendChild(doc.createElement("element2")).setTextContent(String.valueOf(el2));
    e.appendChild(doc.createElement("operation")).setTextContent(String.valueOf(oper));
    e.appendChild(doc.createElement("result")).setTextContent(String.valueOf(result));
    
    XMLRepresentation xmlr = new XMLRepresentation(doc);
    res.setEntity(xmlr);
  }

}
