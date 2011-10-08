(ns nonomail.send
  (:use nonomail.session)
  (:use [clojure.string :only [join]])
  (:import [javax.mail Authenticator Session Message Header
	    Message$RecipientType]
	   [javax.mail.internet MimeMessage MimeMultipart MimeBodyPart
	    InternetAddress AddressException])
  (:require [nonomail.util :as util]))

;; Convenience functions
(defn connect [config]
  (get-session config))

(defn error? [config]
  (has-error? config))

(defn errors [config]
  (get-errors config))

;; send-specific code

(def plain-type #{:plain :html "text/plain" "text/html"})

(defn new-message
  "Creates a new instance of a JavaMail MIME message given a session object"
  [session]
  (let [session (if (instance? Session session)
		  session
		  (:session @session))]
    (MimeMessage. session)))

(defn multipart
  "Given an array of parts, construct a multipart MIME message. Each
part should be a map with the following keys:
    :type -- (string) the MIME type for this part
    :content -- (mixed) the contents of this body part. If the MIME type
is also a multipart MIME type, the contents can be a vector of parts"
  [parts subtype]
  (let [jmulti (if subtype
                 (MimeMultipart. subtype)
                 (MimeMultipart.))]
    (doseq [{:keys [type body]} parts]
      (let [inner-part (MimeBodyPart.)]
        (cond
         (#{:plain "text/plain"} type) (.setText inner-part body)
         (#{:html "text/html"} type) (.setContent inner-part body "text/html")
         (#{:attach :inline} type) (doto inner-part
                                     (.attachFile (util/as-file body))
                                     (.setDisposition (name type)))
         (= :multipart type) (.setContent inner-part 
                                          (multipart body) 
                                          "multipart/mixed")
         (= :multipart-alternative type) (.setContent inner-part 
                                                      (multipart body) 
                                                      "multipart/alternative")
         :else
         (.setContent inner-part (util/as-file body) type))
        (.addBodyPart jmulti inner-part)))
    jmulti))

(defn- get-default-sender
  "Return a default email address by grabbing the session user and mail
host, and sticking an '@' sign between them"
  [session]
  (let [user (get-session-property session "mail.user")
	host (get-session-property session "mail.host")]
    (str user "@" host)))

(defn address->InternetAddress
  "Given an email address in the form of a string, returns
the corresponding javax.mail.internet.InternetAddress object,
or the original string if it could not be converted."
  [address]
  (try
    (InternetAddress. address)
    (catch AddressException _
      address)))

(defn get-addresses
  "Given an email address or a vector of addresses, create the
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
  "Adds the list of recipients (if any) to the appropriate email
header (To, CC or BCC) based on the type. If any of the recipients
is not a valid email address, sets the :error parameter on the
session."
  [session msg type recipients]
  (let [{good :good, bad :bad} (get-addresses recipients)]
    (doseq [r good]
      (.addRecipient msg type r))
    (when (and (not (nil? bad))
	       (:require-valid-recipients session))
      (add-error! session (str "Bad address(es): '" (join "', '") "'")))))

(defmacro add-any-recipients
  "Convenience macro, plugs in boilerplate code. Only valid for send! function."
  [key type]
  `(when-let [v# (~key ~'email)]
     (let [v# (if (string? v#) (vector v#) v#)]
       (add-recipients! ~'session ~'msg (~type) v#))))
	
(defn make-msg
  "Given a map of valid parameters for an email message, set up a
JavaMail message and send it. Valid parameters are:
    :from -- the sender email address (defaults to session value)
    :to -- a single recipient or array of recipients (string)
    :cc -- [optional] a CC recipient
    :bcc -- [optional] a BCC recipient
    :subject -- subject heading for message
    :type -- :plain or :multipart, defaults to :plain
    :subtype -- (used by multipart messages only)
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
	body (if (plain-type type) 
	       (:body email)
	       (multipart (:body email) (get email :subtype nil)))
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
  "Sends an email message using the given session. If msg is an instance of
class MimeMessage, sends it immediately. If it's an email map, send! will
first construct a MimeMessage using (make-msg msg), and then send that."
  [session msg & debug]
  (let [msg (if (instance? MimeMessage msg)
	      msg
	      (make-msg session  msg))]
    (when (seq debug)
      (pr msg))
    (when-not (has-error? session)
      (javax.mail.Transport/send msg))))