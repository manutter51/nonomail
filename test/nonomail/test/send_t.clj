(ns nonomail.test.send-t
  (:import [javax.mail.internet MimeMessage InternetAddress])
  (:use nonomail.send
	nonomail.session
	nonomail.util
	midje.sweet))

(def *session*
     (get-session {:host "localhost"
		   :port 25
		   :user "mark"}))

(facts "about new-message"
  (let [m (new-message *session*)]
    (instance? MimeMessage m) => true))

(facts "about mapping to InternetAddress"
  (let [f address->InternetAddress  ; just for convenience
	a1 "me@here.com"
	a2 "me@here"
	bad "Oh, I'm a bad address."]
    (f a1)  => #(instance? InternetAddress %)
    (f a2)  => #(instance? InternetAddress %)
    (f bad) => (just bad)))

(facts "about creating email messages"
  (let [m1 {:to "me@localhost"
	    :from "myself@localhost"
	    :subject "Creating tests"
	    :body "This is a test"}
	jm1 (make-msg *session* m1)
	jm1-str (msg->str jm1)]
    jm1-str => #"To: me@localhost"
    jm1-str => #"From: myself@localhost"
    jm1-str => #"Subject: Creating tests"
    jm1-str => #"This is a test"
    jm1-str => #"Message-ID: "
    jm1-str => #"Content-Type: text/plain;"))