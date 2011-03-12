(ns nonomail.session
  "Core mail functions used by the send and read libraries."
  (:import (javax.mail Authenticator Session Message Header))
  (:require [nonomail.util :as util]))

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

The config map can also contain some error-handling directives:

    :require-valid-recipients    Don't send mail unless all recipients are valid [default true]

This function returns an atom containing a map with the following keys:
    :config          the original config map
    :props           the java.util.Properties object
    :authenticator   the javax.mail.Authenticator object
    :session         the javax.mail.Session object
    :error           the last error encountered by this session"
  [config]
  (let [defaults {:host "localhost", :port 25, :ssl false, :auth false}
        config (merge defaults config)
        props (set-session-props config)
        authenticator (get-authenticator config)
        session (javax.mail.Session/getDefaultInstance props authenticator)
        session-map {:config config
                     :props props
                     :authenticator authenticator
                     :session session
		     :require-valid-recipients (get config :require-valid-recipients true)
		     :error []}]
    (atom session-map)))

(defn get-session-property
  "[session property]
Given a session and a property string, return the value of the 
corresponding session property."
  [session property]
  (let [props (:props @session)]
    (.get props property)))

(defn merge-session-config
  "[session new-config]
Given an existing session and a new config map, update the session
properties with the new values. Returns the updated properties object."
  [session new-config]
  (let [current-props (:props @session)
        updated-props (set-session-props new-config current-props)]
    (swap! session assoc :props updated-props)
    updated-props))

(defn reset-error!
  "[session error]
Sets the session :error to error. This will typically be either a string
message or a Java exception object."
  [session error]
  (swap! session assoc :error []))

(defn add-error!
  "[session error]
Adds the given error to a list of errors associated with the session's
:error parameter."
  [session error]
  (let [current (:error @session)
	newer (conj current error)]
    (swap! session assoc :error newer)))

(defn get-errors
  [session]
  (:error @session))

(defn has-error?
  "[session]
Returns truthy if the :error param is not nil or an empty array."
  [session]
  (pos? (count (:error @session))))

