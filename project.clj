(defproject nonomail "0.0.1-SNAPSHOT"
  :description "A simple interface to javax.mail from clojure."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [javax.mail/mail "1.4.4"]
                 ]
  :dev-dependencies [[swank-clojure "1.2.0"]
		     [marginalia "0.5.0"]
                     [midje "1.2.0"]]
)
