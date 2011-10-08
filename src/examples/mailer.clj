(ns examples.mailer
  (:require [nonomail.send :as mail]))

(def mail-config
  {:user "mailerdude"
   :pass "send-it-all"
   :host "localhost"})

(def simple-message
  {:to ["joe@abc.com" "may@xyz.com" "pat@def.net"] ; Can be single string address or vector of addresses
   :from "michael-mailer@example-mailer.com"
   :subject "A simple message"
   :body "Hi guys, how do you like my simple mail message?"})

(def multipart-message
  {:to ["sam@ghi.com" "joy@uvw.net"]
   :from "michael-mailer@example-mailer.com"
   :subject "A multipart message"
   :type :multipart
   :body [{:type :plain
           :body "Here are the images you requested"}
          {:type :attach
           :body "/photos/i/image200.png"} ; file path on local drive
          {:type :inline
           :body "/logos/logo-for-mail.jpg"}]})

(def all-messages [simple-message multipart-message])

(defn send-messages [config]
  (let [conn (mail/connect config)]
    (if (mail/error? conn)
      (println "Could not send mail, " (apply str (mail/errors conn)))
      (do
        (mail/send! conn simple-message)
        (mail/send! conn multipart-message)
        (mail/send! conn all-messages)))))

(defn -main [& args]
  (send-messages mail-config))