(ns nonomail.session
  "Core mail functions used by the send and read libraries."
  (:import (javax.mail Authenticator Session Message Header))
  (:require [nonomail.util :as util]))

(declare *session*) ; something for binding to point at, in with-mail-session

(defn- set-session-props
  "[config-map & current-props]
Set session properties for SMTP mail delivery. The config-map is a
standard map with keyword-value pairs.  If you need to add a custom
property to the property list, you can use the property name as the
key. If you pass in a java.util.Properties object as the value of
current-props, this function will update the properties on that object
rather than creating a new one."
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
  "[config-map]
Given a config map, get an instance of javax.mail.Authenticator
to use in creating a valid session."
  [config]
  (let [authenticator (proxy [javax.mail.Authenticator] [] 
                          (getPasswordAuthentication 
                           []
                           (javax.mail.PasswordAuthentication. 
                            (:user config) (:pass config))))]
    authenticator))

(defn get-session
  "[config-map]
Given a config map, create a session object to be used in other
JavaMail functions. The config map should contain values for
the keys listed below.

    :host       The mail host to connect to. Default localhost.
    :port       The port. Default 25.
    :user       Username [optional].
    :pass       Password [optional].
    :ssl        Boolean: use SSL for connections? Default: false
    :auth       Boolean: attempt authorization with :user and :pass? Default: false

This function returns an atom containing a map with the following keys:
    :config          the original config map
    :props           the java.util.Properties object
    :authenticator   the javax.mail.Authenticator object
    :session         the javax.mail.Session object"
  [config]
  (let [defaults {:host "localhost", :port 25, :ssl false, :auth false}
        config (merge defaults config)
        props (set-session-props config)
        authenticator (get-authenticator config)
        session (javax.mail.Session/getDefaultInstance props authenticator)
        session-map {:config config
                     :props props
                     :authenticator authenticator
                     :session session}]
    (atom session-map)))

(defn get-session-property
  "[session property]
Given a session and a property string, return the value of the 
corresponding session property."
  [session property]
  (let [props (:props @session)]
    (.get props property)))

(defmacro with-mail-session
  "[config-or-session & body]
Sets up a session binding, given either a config map or an existing
session (as created by get-session). When you invoke with-mail-session,
the variable nonomail.session/*session* will be bound to the current
JavaMail session."
  [config-or-session & body]
  `(let [sess# (if (map? ~config-or-session)
		(get-session ~config-or-session)
		~config-or-session)]
	(binding [*session* sess#]
      ~@body)))

(defn merge-session-config
  "[session new-config]
Given an existing session and a new config map, update the session
properties with the new values. Returns the updated properties object."
  [session new-config]
  (let [current-props (:props @session)
        updated-props (set-session-props new-config current-props)]
    (swap! session assoc :props updated-props)
    updated-props))

    