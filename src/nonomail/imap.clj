(ns nonomail.imap
  (:require [nonomail.util :as util])
  (:import [com.sun.mail.imap IMAPStore IMAPFolder IMAPMessage]))

(defn- pre-hash
  [ary]
  (apply concat (partition 2 ary)))

(defn get-store
  "[session & config]
Given a session and a list of zero or more options, construct an IMAPStore object
and connect to it. Returns an atom containing a map representing the IMAP store
and its current list of error messages if any.

Example:

    (def my-store (get-store my-session
                             :host \"myhost.com\"
                             :user \"imauser\"
                             :pass \"itsapassword\"
                             :ssl true)
    @my-store ; {:store <#IMAPStore-object>
              ;  :errors []}
              ;     - or -
              ; {:store nil
              ;  :errors [\"Invalid password or user unknown\"]}
"
  [session & config]
  (let [j-sess (:session @session)
        config (apply hash-map (pre-hash config))
        session-config (:config @session)
        errors []
        host (get-first :host config session-config "localhost")
        user (get-first :user config session-config nil)
        password (get-first :pass config session-config nil)
        is-ssl (get-first :ssl config session-config false)
        ]
    ))