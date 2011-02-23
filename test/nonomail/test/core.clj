(ns nonomail.test.core
  (:use [nonomail.core] :reload)
  (:use midje.sweet))

(def test-config-host-only {:host "127.0.0.1"})

(facts "about core/get-session"
  "defaults and built-ins are correct"
  (let [session (get-session {})
	config (:config @session)
	props (:session-props @session)
	authenticator (:session-authenticator @session)]
    (.get props "mail.host") => "localhost"
    (.get props "mail.port") => 25
    authenticator => #(instance? javax.mail.Authenticator %))
  )
