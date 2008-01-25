package org.exoplatform.services.jcr.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

public class AddNodeTypePlugin extends BaseComponentPlugin {

  /**
   * We have list of node types in order of its adding
   */
  private Map<String, List<String>> nodeTypes    = new LinkedHashMap<String, List<String>>();

  public  static final String       AUTO_CREATED = "autoCreatedInNewRepository";

  public AddNodeTypePlugin(InitParams params) {

    Iterator<ValuesParam> vparams = params.getValuesParamIterator();
    while (vparams.hasNext()) {
      ValuesParam nodeTypeParam = vparams.next();
      nodeTypes.put(nodeTypeParam.getName(), nodeTypeParam.getValues());
    }
  }

  public List<String> getNodeTypesFiles(String repositoryName) {
    return nodeTypes.get(repositoryName);
  }
}
