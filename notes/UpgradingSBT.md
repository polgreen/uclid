Instructions for Creating an SBT Binary Package
===============================================

Ensure you have sbt v1.0 or greater installed. Install instructions 
are available at http://www.scala-sbt.org/1.0/docs/Setup.html.

If all goes well, running update, clean, compile and test in sbt should 
all work. The command we need is:

    $ sbt update clean compile test

If everything above works, we can now try to create an SBT binary package.

    $ sbt universal:packageBin

This will create uclid/target/universal/uclid-0.7.zip, which contains the uclid
binary in the bin/ subdirectory. 
