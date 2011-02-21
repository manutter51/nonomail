(ns nonomail.test.core
  (:use [nonomail.core] :reload)
  (:use midje.sweet))

(def test-config-host-only {:host "127.0.0.1"})

(facts "about core/get-session"
  "host is required"
  (get-session {}) => (throws Exception "get-session :host parameter not given")

  "defaults and built-ins are correct"
  (let [session (get-session {:host "localhost"})
	config (:config @session)
	props (:session-props @session)
	authenticator (:session-authenticator @session)]
    (:protocol config) => :smtp
    (.get props "mail.smtp.host") => "localhost"
    authenticator => #(instance? javax.mail.Authenticator %))
  )
