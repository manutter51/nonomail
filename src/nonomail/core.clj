(ns nonomail.core
  "Core mail functions used by the send and read libraries."
  (:import (javax.mail Authenticator Session Message Header)))

(declare *session*) ; something for binding to point at, in with-mail-session

(defn- set-session-props
  "Set session properties for SMTP mail delivery."
  [config]
  (let [props (java.util.Properties.)
	port (or (:port config) 25)]
    (doto props
      (.put "mail.host" (:host config))
      (.put "mail.port" port)
      (.put "mail.socketFactor.port" port))
    (when (:user config)
      (.put props (:user config)))
    (when (:auth config)
      (.put props "mail.auth" "true"))
    (when (:ssl config)
      (doto props
	(.put "mail.starttls.enable" "true")
	(.put "mail.socketFactory.class"
	      "javax.net.ssl.SSLSocketFactory")
	(.put "mail.socketFactory.fallback" "false")))
  
  props))   

(defn- get-authenticator
  "Given a config map, get an instance of javax.mail.Authenticator
to use in creating a valid session."
  [config]
  (let [authenticator (proxy [javax.mail.Authenticator] [] 
                          (getPasswordAuthentication 
                           []
                           (javax.mail.PasswordAuthentication. 
                            (:user config) (:password config))))]
    authenticator))

(defn get-session
  "Given a config map, create a javax.mail.Session object to be
used in other JavaMail functions. The config map should contain
values for the keys listed below.

  :host       The mail host to connect to. Default localhost.
  :port       The port. Default 25.
  :user       Username [optional].
  :pass       Password [optional].
  :ssl        Boolean: use SSL for connections? Default: false
  :auth       Boolean: attempt authorization with :user and :pass? Default: false"
  [config]
  (let [defaults {:host "localhost", :port 25, :ssl false, :auth false}
	config (merge defaults config)
	props (set-session-props config)
	authenticator (get-authenticator config)
	session (javax.mail.Session/getDefaultInstance props authenticator)
	session-map {:config config
		     :session-props props
		     :session-authenticator authenticator
		     :session-object session}]
    (atom session-map)))

(defmacro with-mail-session
  "Sets up a session binding, given either a config map or an existing
session (as created by get-session). When you invoke with-mail-session,
the variable *session* will be bound to the current JavaMail session."
  [config-or-session & body]
  `(let [sess# (if (map? ~config-or-session)
		(get-session ~config-or-session)
		~config-or-session)]
	(binding [*session* sess#]
	   ~@body)))