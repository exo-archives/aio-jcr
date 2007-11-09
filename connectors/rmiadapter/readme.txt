
[PN] 30.03.2007

There are a problem with current package logic.

eXo JCR core comp. and eXo RMI comp. contains API and Impl in whole package(s).
But RMI adapter don't need Impl stuff by definition. It's a RMI client only.

NOTE: Resource archive produced by the project contains all dependencies from Implementations like eXo container etc.

So, the project in current state hasn't an interest for a future support.
