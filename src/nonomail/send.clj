(ns nonomail.send
  (:use nonomail.core)
  (:import [javax.mail Authenticator Session Message Header]))

(defn- new-message
  "[session]
Creates a new instance of a JavaMail MIME message given a session object"
  [session]
  (javax.mail.internet.MimeMessage. session))

(defn- get-default-sender
  "[session]
Return a default email address by grabbing the session user and mail
host, and sticking an '@' sign between them"
  [session]
  (let [user (get-session-property session "mail.user")
	host (get-session-property session "mail.host")]
    (str user "@" host)))

(defn send!
  "[email & session]
Given a map of valid parameters for an email message, set up a
JavaMail message and send it. Valid parameters are:
    :from -- the sender email address (defaults to session value)
    :to -- a single recipient or array of recipients (string)
    :subject -- subject heading for message
    :type -- plain|multipart, defaults to plain
    :body -- body of email message, or array of parts if multipart type
If you pass in a string as a map key, (send) assumes it is a valid
email custom header, and adds the key-value pair to the headers of
the email.
The (send) function also takes an optional session object, defaulting
to nonomail.core/*session*"
  [email & session]
  (let [session (if (seq? session) 
		  (first session) 
		  nonomail.core/*session*)
	from (get email :from (get-default-sender session))
	]
    nil ; not done yet...
    ))
