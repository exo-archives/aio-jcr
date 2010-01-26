JCR core
========
Goto JCR /component/core and call:
mvn clean test                - to run eXo JCR internal tests (all now)
mvn clean test -Prun-tck      - to run eXo JCR internal tests + TCK (tck files are automatically downloaded. No manual task needed).
mvn clean test -Drun-tck      - to run TCK only (tck files are automatically downloaded. No manual task needed).
mvn clean test -Prun-devtests - to run custom set of tests, useful for on-task development.

JCR Ext
=======
Goto JCR /component/ext and call:
mvn clean test                - to run eXo JCR Ext tests... tests set managed by surefire-plugin configuration.
