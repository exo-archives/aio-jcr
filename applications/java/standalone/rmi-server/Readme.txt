How to run simple exo jcr rmi client:
1. Run "rmiregistry 9999" command.
2. mvn clean install
3. Go to target\rmi-server-{jcr.version}.dir\bin and run run.cmd.
4  Run "mvn clean install " command within "rmi-client" folder.
4. Go to rmi-client\target\rmi-client-{jcr.version}.dir\bin and run run.cmd. Simple rmi client will run. (It just get access to the root node, then exit).
