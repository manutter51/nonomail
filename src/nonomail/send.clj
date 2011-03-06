(ns nonomail.send
  (:use nonomail.session)
  (:use [clojure.contrib.str-utils :only [str-join]])
  (:import [javax.mail Authenticator Session Message Header Message$RecipientType]
	   [javax.mail.internet MimeMessage InternetAddress AddressException])
  (:require [nonomail.util :as util]))

(defn new-message
  "[session]
Creates a new instance of a JavaMail MIME message given a session object"
  [session]
  (let [session (if (instance? Session session)
		  session
		  (:session @session))]
  (MimeMessage. session)))

(defn multipart-body
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

(defn address->InternetAddress
  "[address]
Given an email address in the form of a string, returns
the corresponding javax.mail.internet.InternetAddress object,
or the original string if it could not be converted."
  [address]
  (try
    (InternetAddress. address)
    (catch AddressException _
      address)))

(defn get-addresses
  "[addresses]
Given an email address or a vector of addresses, create the
corresponding javax.mail.internet.InternetAddress objects.
Results are returned as a map, with the key :good mapped to an
array of valid InternetAddress objects, and the key :bad
mapped to a vector of any invalid addresses."
  [addresses]
  (let [addresses (if (seq addresses)
		    addresses
		    (vector addresses))
	addr-obs (map address->InternetAddress addresses)
	bad-list (group-by string? addr-obs)]
    {:good (bad-list false), :bad (bad-list true)}))


(defn- add-recipients!
  "[session msg type recipients]
Adds the list of recipients (if any) to the appropriate email
header (To, CC or BCC) based on the type. If any of the recipients
is not a valid email address, sets the :error parameter on the
session."
  [session msg type recipients]
  (let [{good :good, bad :bad} (get-addresses recipients)]
    (doseq [r good]
      (.addRecipient msg type r))
    (when (and (not (nil? bad))
	       (:require-valid-recipients session))
      (add-error! session (str "Bad address(es): '" (str-join "', '") "'")))))

(defmacro add-any-recipients
  "[key type]
Convenience macro, plugs in boilerplate code. Only valid for send! function."
  [key type]
  `(when-let [v# (~key ~'email)]
     (let [v# (if (string? v#) (vector v#) v#)]
       (add-recipients! ~'session ~'msg (~type) v#))))
	
(defn make-msg
  "[session email]
Given a map of valid parameters for an email message, set up a
JavaMail message and send it. Valid parameters are:
    :from -- the sender email address (defaults to session value)
    :to -- a single recipient or array of recipients (string)
    :cc -- [optional] a CC recipient
    :bcc -- [optional] a BCC recipient
    :subject -- subject heading for message
    :type -- :plain or :multipart, defaults to :plain
    :body -- body of email message, or array of parts if multipart type

If you pass in a string as a map key, (send) assumes it is a valid
email custom header, and adds the key-value pair to the headers of
the email.

Returns the javax.mail.MimeMessage object."
  [session email]
  (let [java-session (:session @session)
	from (get email :from (get-default-sender session))
	from (address->InternetAddress from)
	subject (get email :subject "")
	type (get email :type :plain)
	body (if (= type :plain) 
	       (:body email)
	       (multipart-body (:body email)))
	extra-headers (util/only-string-keys email)
	msg (MimeMessage. java-session)
	]
    (if (string? from)
      (add-error! session (str "Invalid sender address: '" from "'"))
      (.setFrom msg from))
    (add-any-recipients :to Message$RecipientType/TO)
    (add-any-recipients :cc Message$RecipientType/CC)
    (add-any-recipients :bcc Message$RecipientType/BCC)
    (when-not (pos? (count (.getAllRecipients msg)))
      (add-error! session "No valid recipients."))
    (doseq [k (keys extra-headers)]
      (let [hdr k
	    val (extra-headers k)]
	(.addHeader msg hdr val)))
    (.setSubject msg subject)
    (if (= type :plain)
      (.setText msg body)
      (.setContent msg body))
    msg))

(defn send!
  "[session msg]
Sends an email message using the given session. If msg is an instance of
class MimeMessage, sends it immediately. If it's an email map, send! will
first construct a MimeMessage using (make-msg msg), and then send that."
  [session msg]
  (let [msg (if (instance? MimeMessage msg)
	      msg
	      (make-msg session  msg))]
      (when-not (has-error? session)
	(javax.mail.Transport/send msg))))