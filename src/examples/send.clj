(ns example.mailer
  (:require [nonomail.session :as sess]
            [nonomail.smtp :as mail]))

(def mail-config
  {:user "mailerdude"
   :pass "send-it-all"
   :host "localhost"
   :port 25 ; optional, uses protocol-specific defaults (25 for smtp)
   :auth    ; optional, if present indicates server requires login
   :ssl     ; optional, if present indicates server uses SSL
                                        ; TODO Research SSL vs TLS
   ; You can also use any standard Java property list accepted by a javax.mail.Session
   "mail.socketFactory.fallback" "false" ; 
   })

(def simple-message
  {:to ["joe@abc.com" "may@xyz.com" "pat@def.net"]
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
           :body "/photos/i/image200.png"}
          {:type :inline
           :body "/logos/logo-for-mail.jpg"}]})

(defn -main [& args]
  (let [session (sess/get-session mail-config)]
    (if (sess/has-error?)
      (do ; something...
        )
      (doseq [msg [simple-message multipart-message]]
        (mail/send! msg)))))