How to run simple exo jcr rmi client:
1. Run "rmiregistry 9999" command.
2. Run "mvn clean install antrun:run" command within "rmi-server" folder. Rmi server will start.
4. Run "mvn clean install antrun:run" command within "rmi-client" folder. Simple rmi client will run. (It just get access to the root node, then exit).
