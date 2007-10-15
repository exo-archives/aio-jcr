cd ..
call mvn -Dexo.test.skip=true assembly:assembly

cd bin
call eXoDavBrowser-certification.bat
