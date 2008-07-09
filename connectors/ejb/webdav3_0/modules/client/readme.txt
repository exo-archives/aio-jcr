NOTE To try this example you need upload and install easybeans container for jonas.
See http:\\easybeans.org .
This example tried with easybeans-rar_for_jonas-openjpa-1.0.1.rar

To run example:
1. build client and ejb project.
2. build ear (run it in folder webdav3_0, see parent folders)
    
    mvn clean install -f build-ear.xml
    
3. copy file in folder JONAS_ROOT/easybeans_deploy
4. check new beans are deployed

    jonas admin -j
    
5. go back in current folder.
6. extract file easybeans-rar_for_jonas-openjpa-1.0.1.rar (or whar version you have) in folder target/rar
7. run example

    for linux:
    sh run.sh
    
    for windows:
    run.bat