java -Xmx256m -Duser.language=en -Duser.region=us -jar exo.jcr.applications.repoload-1.10.2-SNAPSHOT.jar -conf=".\bin\config\test-configuration.xml" -root="/testStorage/root1/node1/node1" -tree="1-2-5-10" -vdfile=".\bin\img.tif" -repo="repository" -ws="production" -read -readdc -readprop -threads="5" -iteration="1" -concurrent 
