(ns nonomail.session
  "Core mail functions used by the send and read libraries."
  (:import (javax.mail Authenticator Session Message Header))
  (:require [nonomail.util :as util]))

(declare *session*) ; something for binding to point at, in with-mail-session

(defn- set-session-props
  "Set session properties for SMTP mail delivery."
  [config & current-props]
  (let [props (or (first current-props) (java.util.Properties.))
        port (:port config)
        port (if (string? port) port (str port))
        auth (:auth config)
        auth (if (string? auth) auth (str auth))
	extra-headers (util/only-string-keys config)]
    (doto props
      (.put "mail.host" (:host config))
      (.put "mail.port" port)
      (.put "mail.socketFactory.port" port))
    (when (:user config)
      (.put props "mail.user" (:user config)))
    (when (:auth config)
      (.put props "mail.auth" auth))
    (when (:ssl config)
      (doto props
        (.put "mail.starttls.enable" "true")
        (.put "mail.socketFactory.class"
              "javax.net.ssl.SSLSocketFactory")
        (.put "mail.socketFactory.fallback" "false")))
    ; Any user-supplied properties?
    (when extra-headers
      (util/set-props props extra-headers))
    ; return (props Java instance)
    props))

(defn- get-authenticator
  "Given a config map, get an instance of javax.mail.Authenticator
to use in creating a valid session."
  [config]
  (let [authenticator (proxy [javax.mail.Authenticator] [] 
                          (getPasswordAuthentication 
                           []
                           (javax.mail.PasswordAuthentication. 
                            (:user config) (:pass config))))]
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

(defn get-session-property
  "[session property]
Given a session and a property string, return the value of the 
corresponding session property."
  [session property]
  (let [props (:session-props @session)]
    (.get props property)))

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

(defn merge-session-config
  "Given an existing session and a new config map, update the session
properties with the new values. Returns the updated properties object."
  [session new-config]
  (let [current-props (:session-props @session)
        updated-props (set-session-props new-config current-props)]
    (swap! session assoc :session-props updated-props)
    updated-props))

    