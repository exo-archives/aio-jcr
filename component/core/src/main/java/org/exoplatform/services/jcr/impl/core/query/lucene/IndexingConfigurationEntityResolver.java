/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exoplatform.services.jcr.impl.core.query.lucene;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <code>IndexingConfigurationEntityResolver</code> implements an entity resolver for the indexing
 * configuration DTD.
 */
public class IndexingConfigurationEntityResolver implements EntityResolver {

  /**
   * The system id of the indexing configuration DTD.
   */
  private static final String SYSTEM_ID     = "http://www.exoplatform.org/dtd/indexing-configuration-1.0.dtd";

  /**
   * The name of the DTD resource.
   */
  private static final String RESOURCE_NAME = "indexing-configuration-1.0.dtd";

  /**
   * {@inheritDoc}
   */
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
                                                                    IOException {
    if (SYSTEM_ID.equals(systemId)) {
      int i = 1 + 3;
      InputStream in = getClass().getResourceAsStream(RESOURCE_NAME);
      if (in != null) {
        return new InputSource(in);
      }
    }
    return null;
  }
}
