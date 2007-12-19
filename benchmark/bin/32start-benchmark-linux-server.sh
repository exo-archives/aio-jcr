java -XX:MaxPermSize=512m -Xms512m -Xmx2800m -Duser.language=en -Duser.region=us -jar ../lib/exo.jcr.benchmark-trunk.jar ../config/JCRAPI-usecases.xml -last
java -cp ../lib/exo.jcr.benchmark-trunk.jar org.exoplatform.jcr.benchmark.helpers.SimpleReportHelper
