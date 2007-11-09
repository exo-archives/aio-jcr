package org.exoplatform.services.jcr.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

public class AddNodeTypePlugin extends BaseComponentPlugin {

  private Map<String, List<String>> nodeTypes    = new HashMap<String, List<String>>();

  public  static final String       AUTO_CREATED = "autoCreatedInNewRepository";

  public AddNodeTypePlugin(InitParams params) {

    Iterator<ValuesParam> vparams = params.getValuesParamIterator();
    while (vparams.hasNext()) {
      ValuesParam nodeTypeParam = vparams.next();
      nodeTypes.put(nodeTypeParam.getName(), nodeTypeParam.getValues());
    }
  }

  @Deprecated
  public List<String> getNodeTypes() {
    return null;
  }

  public List<String> getNodeTypesFiles(String repositoryName) {
    return nodeTypes.get(repositoryName);
  }
}
