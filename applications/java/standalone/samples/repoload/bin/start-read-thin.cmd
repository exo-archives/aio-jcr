java -Xmx256m -Duser.language=en -Duser.region=us -jar exo.jcr.applications.repoload-1.8.3.1.jar -conf=".\bin\config\configuration-thin.xml" -root="/testStorage/root1/node1/node1" -tree="10-100-100-100" -vdfile=".\bin\img.tif" -repo="repository" -ws="production" -read -readdc -threads="5" -iteration="1" -concurrent 


rem    -threads="5" -iteration="1" -concurrent > /dev/null &
