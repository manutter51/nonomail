(ns nonomail.send
  (:use nonomail.core)
  (:import [javax.mail Authenticator Session Message Header]
	   [javax.mail.internet MimeMessage InternetAddress]))

(defn- new-message
  "[session]
Creates a new instance of a JavaMail MIME message given a session object"
  [session]
  (javax.mail.internet.MimeMessage. session))

(defn get-multipart-body
  "[parts]
Given an array of parts, construct a multipart MIME message. Each
part should be a map with the following keys:
    :type -- (string) the MIME type for this part
    :body -- (mixed) the contents of this body part. If the MIME type
is also a multipart MIME type, the contents can be a vector of parts"
  [parts]
  (let [part nil] ;not implemented yet
    (throw (Exception. "Not Implemented Yet"))))

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
    :type -- :plain or :multipart, defaults to :plain
    :body -- body of email message, or array of parts if multipart type
If you pass in a string as a map key, (send) assumes it is a valid
email custom header, and adds the key-value pair to the headers of
the email.
The (send) function also takes an optional session object, defaulting
to nonomail.core/*session*"
  [email & session]
  (let [session (if (seq session) 
		  (first session) 
		  nonomail.core/*session*)
	java-session (:session-object @session)
	from (get email :from (get-default-sender session))
	to (:to email)
	to (if (string? to) (vec to) to)
	subject (get email :subject "")
	type (get email :type :plain)
	body (if (= type :plain) 
	       (:body email)
	       (get-multipart-body (:body email)))
	extra-keys (filter string? (keys email))
	extra-headers (select-keys email extra-keys)
	msg (MimeMessage. java-session)
	]
    (.setFrom msg (InternetAddress. from))
    (doseq [t to]
      (.setRecipients msg
		      (javax.mail.Message$RecipientType/TO)
		      (InternetAddress/parse t)))
    (doseq [k (keys extra-headers)]
      (let [hdr k
	    val (extra-headers k)]
	(.addHeader msg hdr val)))
    (.setSubject msg subject)
    (if (= type :plain)
      (.setText msg body)
      (throw (Exception. "Multipart not implemented yet.")))
    (javax.mail.Transport/send msg)
    ))
