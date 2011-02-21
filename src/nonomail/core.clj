(ns nonomail.core
  (:import (javax.mail Authenticator Session Message Header)))

(defmulti set-session-props (fn [config] (:protocol config)))

(defmethod set-session-props :smtp [config]
  "Set session properties for SMTP mail delivery."
  (let [props (java.util.Properties.)
	port (or (:port config) 25)]
    (doto props
      (.put "mail.smtp.host" (:host config))
      (.put "mail.smtp.port" port)
      (.put "mail.smtp.socketFactor.port" port))
    (when (:user config)
      (.put props (:user config)))
    (when (:auth config)
      (.put props "mail.smtp.auth" "true"))
    (when (:ssl config)
      (doto props
	(.put "mail.smtp.starttls.enable" "true")
	(.put "mail.smtp.socketFactory.class"
	      "javax.net.ssl.SSLSocketFactory")
	(.put "mail.smtp.socketFactory.fallback" "false")))
  
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

  :host       The mail host to connect to [required].
  :port       The port, if different from the default for the given protocol.
  :protocol   One of :smtp (for sending), :imap or :pop (for reading). Defaults to smtp.
  :user       Username [optional].
  :pass       Password [optional].
  :ssl        Boolean: use SSL for connections?
  :auth       Boolean: attempt authorization with :user and :pass?"
  [config]
  (when-not (:host config)
    (throw (Exception. "get-session :host parameter not given")))
  (let [defaults {:protocol :smtp, :ssl true, :auth false}
	config (merge defaults config)
	props (set-session-props config)
	authenticator (get-authenticator config)
	session (javax.mail.Session/getDefaultInstance props authenticator)
	session-map {:config config
		     :session-props props
		     :session-authenticator authenticator
		     :session-object session}]
    (atom session-map)))

