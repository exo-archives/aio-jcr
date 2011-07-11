Summary

    * Status: Impossible to move files in Webdav when the destination path contains space (Windows)
    * CCP Issue: N/A, Product Jira Issue: JCR-1628
    * Complexity: Low

The Proposal
Problem description

What is the problem to fix?

    * Unable to move files in Webdav when the destination path contains space (Windows).

Fix description

How is the problem fixed?

    * Destination header might contain unescaped characters. If Exception has come during URI building, try to escape characters and build URI again.

Tests to perform

Reproduction test

    * Steps to reproduce:

   1. Create a web folder pointing to http://localhost:8080/rest/private/jcr/repository/collaboration/
   2. Go to http://localhost:8080/portal/rest/private/jcr/repository/collaboration/Users/root/Public
   3. Upload some file to that web folder, for example metro.pdf
   4. Create another folder containing a space in its name, for example "New Folder"
   5. Copy and paste metro.pdf to "New Folder" -> Error popup and error message on the server console:

          java.net.URISyntaxException: Illegal character in path at index 35: collaboration/Users/root/Public/New Folder/metro.pdf
          	at java.net.URI$Parser.fail(URI.java:2816)
          	at java.net.URI$Parser.checkChars(URI.java:2989)
          	at java.net.URI$Parser.parseHierarchical(URI.java:3073)
          	at java.net.URI$Parser.parse(URI.java:3031)
          	at java.net.URI.<init>(URI.java:578)
          	at org.exoplatform.services.jcr.webdav.WebDavServiceImpl.copy(WebDavServiceImpl.java:270)
          	at org.exoplatform.services.cms.webdav.WebDavServiceImpl.copy(WebDavServiceImpl.java:175)
          ...

Tests performed at DevLevel

    * Manual testing with AIO 1.6.9

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*

