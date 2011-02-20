# nonomail

A Clojure interface to the javax.mail API

## Usage

Leiningen project.clj settings:

    :dependencies [[javax.mail/mail "1.4.4"]]

## Installation

The Leiningen dependency should take care of things, but in my experience,
it doesn't. I had to download the package from [1][the JavaMail home page]
and then run the Maven install line suggested by Leiningen (from inside the
package directory):

    mvn install:install-file -DgroupId=javax.mail -DartifactId=mail -Dversion=1.4.4 -Dpackaging=jar -Dfile=./mail.jar

Once installed in the local Maven repository, the `lein deps` command worked as expected.

[1]: <http://java.sun.com/products/javamail/> "JavaMail Home Page"

## License

Copyright (C) 2011 by Mark Nutter

Distributed under the Eclipse Public License, the same as Clojure.
