How to run simple exo jcr rmi client:
1. Run "rmiregistry 9999" command.
2. Uncomment maven-antrun-plugin within "rmi-server" folder (if you want to run server after compilation).
3. Run "mvn clean install" command within "rmi-server" folder. Rmi server will start.
4. Run "mvn clean install" command within "rmi-client" folder. Simple rmi client will run. (It just get access to the root node, then exit).
