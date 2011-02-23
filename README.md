# nonomail

A Clojure interface to the javax.mail API

This project is not yet ready for prime time. I wouldn't use it yet if I were you.

## Usage

Leiningen project.clj settings:

    :dependencies [[javax.mail/mail "1.4.4"]]

## Installation

The Leiningen dependency should take care of things, but in my experience,
it doesn't. I had to download the package from 
[the JavaMail home page](http://java.sun.com/products/javamail/)
and then run the Maven install line suggested by Leiningen (from inside the
package directory):

    mvn install:install-file -DgroupId=javax.mail -DartifactId=mail -Dversion=1.4.4 -Dpackaging=jar -Dfile=./mail.jar

Once installed in the local Maven repository, the `lein deps` command worked as expected.

## License

Copyright (C) 2011 by Mark Nutter

Distributed under the Eclipse Public License, the same as Clojure.
