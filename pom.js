eXo.require("eXo.projects.Project")  ;

function JCR(version) {
  this.version =  version ;
  this.relativeMavenRepo =  "org/exoplatform/jcr" ;
  this.relativeSRCRepo =  "jcr/trunk" ;
  this.name =  "jcr" ;
  
  this.services = {}
  this.services.jcr = 
    new Project("org.exoplatform.jcr", "exo.jcr.component.core", "jar", version).
    addDependency(new Project("org.exoplatform.jcr", "exo.jcr.component.ext", "jar", version)).
    addDependency(new Project("org.exoplatform.jcr", "exo.jcr.component.webdav", "jar", version)).
    addDependency(new Project("org.exoplatform.jcr", "exo.jcr.component.ftp", "jar", version)).
    addDependency(new Project("jcr", "jcr", "jar", "1.0")).
    addDependency(new Project("concurrent", "concurrent", "jar", "1.3.2")).
    addDependency(new Project("javagroups", "jgroups-all", "jar", "2.4")).
    addDependency(new Project("lucene", "lucene", "jar", "1.4.3"));

  this.frameworks = {}
  this.frameworks.web = 
    new Project("org.exoplatform.jcr", "exo.jcr.framework.web", "jar", version).
    addDependency(new Project("org.exoplatform.jcr", "exo.jcr.component.rest", "jar", version)).
    addDependency(new Project("commons-chain", "commons-chain", "jar", "1.0")).
    addDependency(new Project("log4j", "log4j", "jar", "1.2.8"));

  this.frameworks.command = new Project("org.exoplatform.jcr", "exo.jcr.framework.command", "jar", version) ; 
}

eXo.module.jcr = new JCR('1.7') ;
