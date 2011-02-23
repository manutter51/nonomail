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
    (.get props "mail.port") => "25"
    authenticator => #(instance? javax.mail.Authenticator %))

  "parameters are passed in correctly"
  (let [config {:host "myhost"
                :port 123
                :user "joeuser"
                :pass "big-secret"
                :ssl true
                :auth true}
        session (get-session config)
        props (:session-props @session)
        host (.get props "mail.host")
        port (.get props "mail.port")
        user (.get props "mail.user")
        pass (.get props "mail.password") ; should not exist, but we'll check anyway
        auth (.get props "mail.auth")
        ssl-enable (.get props "mail.starttls.enable")
        factory (.get props "mail.socketFactory.class")
        fallback (.get props "mail.socketFactory.fallback")]
      host => (exactly "myhost")
      port => (exactly "123")
      port => string?
      user => (exactly "joeuser")
      pass => nil?
      auth => (exactly "true")
      auth => string?
      ssl-enable => (exactly "true")
      factory => (exactly "javax.net.ssl.SSLSocketFactory")
      fallback => (exactly "false"))
  )
