Summary

    * Status: Problem with webdav, https and apache
    * CCP Issue: CCP-581, Product Jira Issue: JCR-1434.
    * Complexity: medium

The Proposal
Problem description

What is the problem to fix?

    * How to reproduce:
      To reproduce this issue, it's necessary to use WebDAV through an Apache server (mod_jk) and using https.
      For that, it's necessary to use this architecture:
      The Apache (httpd-2.2.15-win32-x86-openssl-0.9.8m-r2.msi) forwards requests to the JBoss (4.2.2-GA) by the AJP 1.3 protocol using mod_jk (mod_jk-1.2.30-httpd-2.2.3.so). The Apache listens on the port 443 for https requests.
      The scenario to reproduce the problem:
         1. You must have two users: an administrator (root) and a simple user, User A, who has only read permission on all files.
         2. The root copies a word document (test.doc) into a directory B and makes sure that User A has a read permission on it.
         3. User A connects to WebDAV through a webfolder in Windows Explorer and opens test.doc in directory B.
         4. If user A closes the file but not the Word editor (CTRL+F4), (s)he can't reopen it.
         5. If user A tries to open another file after opening the first one, (s)he can't either (but (s)he can open one by one).
         6. If this file was opened by root, the steps 3 and 4 don't occur for a certain time (as if there was an inheritance of permissions)
         7. If the file was locked by root, the steps 3 and 4 don't occur either. But, if the root retrieves the lock on it, the problem appears again.

    * The conf_apache.zip (in JCR-1434) contains the necessary configurations, to put them under the repository conf of the Apache server and to make necessary changes.

    * Environment: Apache server (httpd-2.2.15-win32-x86-openssl-0.9.8m-r2.msi), Jboss server (4.2.2-GA)

Fix description

How is the problem fixed?

    * LockCommand returns LOCKED status instead of FORBIDDEN status if the permission is read only and AccessDeniedException occurs.
      How it works: Word tries to lock document. User has only "read" permission, so AccesDeniedException occurs. Then LOCKED status is returned, and Word will ask user whether (s)he wants to open the document as read only because the document has already been locked. This workaround is acceptable for both MS Word versions 2003 and 2007.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
There are currently no attachments on this page.
Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel

    * Manual testing WebDav via https server

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

    * It can cause some unexpected behavior for other document types that support FORBIDDEN status.

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * New patch approved by the PM

Support Comment

    * Patch of workaround validated by Support

QA Feedbacks
*

