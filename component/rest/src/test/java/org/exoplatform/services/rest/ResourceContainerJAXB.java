/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import java.math.BigDecimal;

import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.generated.*;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@URITemplate("/test/jaxb/")
public class ResourceContainerJAXB implements ResourceContainer{

  @HTTPMethod("GET")
  @ProducedTransformerFactory("org.exoplatform.services.rest.JAXBEntityTransformerFactory")
  public Response method1() throws Exception {
    System.out.println(">>> JAXBTransformation");
    Book book = new Book();
    book.setTitle("Red Hat Enterprise Linux 5 Administration Unleashed");
    book.setAuthor("Tammy Fox");
    book.setSendByPost(false);
    book.setPrice(createPrice("EUR", 21.75f));
    book.setMemberPrice(createMemberPrice("EUR", 17.25f));
    return Response.Builder.ok(book, "text/xml").build();
  }

  private Price createPrice(String currency, Float value) {
    Price price = new Price();
    price.setCurrency(currency);
    price.setValue(new BigDecimal(value));
    return price;
  }

  private MemberPrice createMemberPrice(String currency, Float value) {
    MemberPrice mprice = new MemberPrice();
    mprice.setCurrency(currency);
    mprice.setValue(new BigDecimal(value));
    return mprice;
  }
  
}
