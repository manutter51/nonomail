(ns nonomail.test.core
  (:use [nonomail.core] :reload)
  (:use midje.sweet))

(facts "about core/get-session"
       
  "defaults and built-ins are correct"
  (let [session (get-session {})
	config (:config @session)
	props (:session-props @session)
	authenticator (:session-authenticator @session)]
    (.get props "mail.host") => "localhost"
    (.get props "mail.port") => "25"
    authenticator => #(instance? javax.mail.Authenticator %))

  "parameters are mapped correctly"
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
      host => (just "myhost")
      port => (just "123")
      port => string?
      user => (just "joeuser")
      pass => nil?
      auth => (just "true")
      auth => string?
      ssl-enable => (just "true")
      factory => (just "javax.net.ssl.SSLSocketFactory")
      fallback => (just "false")))

(facts "about extra parameters"
  "extra parameters passed in correctly"
  (let [config {:host "anyhost"
		"mail.imap.host" "imaphost"}
	session (get-session config)
	props (:session-props @session)
	]
    props => truthy
    (.get props "mail.host") => (just "anyhost")
    (.get props "mail.imap.host") => (just "imaphost")))

(facts "about get-session-property"
  "get-session-property gets correct property values"
  (let [session (get-session {:host "my-host"
			      "mail.smtp.host" "my-smtp"})
	; for convenience, to keep lines shorter
	get-prop (partial get-session-property session)]
    (get-prop "mail.host") => (just "my-host")
    (get-prop "mail.smtp.host") => (just "my-smtp")))

(facts "about updating parameters"

  "updated parameters replace initial parameters"
  (let [config {:host "localhost"
                "mail.smtp.host" "remote.smtp.host"
                "mail.imap.host" "remote.imap.host"}
        new-config {:host "remote.host"
                    "mail.smtp.user" "joe-user"
                    "mail.smtp.host" "other.smtp.host"}
        session (get-session config)
        props (:session-props @session)
        old-host (.get props "mail.host")
        old-smtp-host (.get props "mail.smtp.user")
        old-imap-host (.get props "mail.imap.user")
        updated-props (merge-session-config session new-config)
        new-props (:session-props @session)
        updated-host (.get props "mail.host")
        updated-smtp-host (.get updated-props "mail.smtp.host")
        updated-imap-host (.get updated-props "mail.imap.host")
        updated-smtp-user (.get updated-props "mail.smtp.user")
        new-host (.get new-props "mail.host")
        new-smtp-host (.get new-props "mail.smtp.host")
        new-imap-host (.get new-props "mail.imap.host")
        new-smtp-user (.get updated-props "mail.smtp.user")]
    props => truthy
    updated-props => truthy
    new-props => truthy
    updated-host => (just "remote.host")
    updated-smtp-host => (just "other.smtp.host")
    updated-imap-host => (just "remote.imap.host")
    updated-smtp-user => (just "joe-user")
    new-host => (just "remote.host")
    new-smtp-host => (just "other.smtp.host")
    new-imap-host => (just "remote.imap.host")
    new-smtp-user => (just "joe-user")))
