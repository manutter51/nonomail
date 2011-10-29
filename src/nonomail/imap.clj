(ns nonomail.imap
  (:refer-clojure :exclude [find])
  (:require [nonomail.util :as util])
  (:use nonomail.session)
  (:import [com.sun.mail.imap IMAPStore IMAPFolder IMAPMessage]
           [javax.mail Session URLName]))

;; Convenience functions
(defn connect [config]
  (get-session config))

(defn error? [config]
  (has-error? config))

(defn errors [config]
  (get-errors config))

;; main functions

(defn parse-options [options]
  (let [singles #{:ssl}]
    (util/parse-args options singles)))

(defn get-store
  "Given a session and a list of zero or more options, construct an IMAPStore object
and connect to it. Returns an atom containing a map representing the IMAP store
and its current list of error messages if any.

Example:

    (def my-store (get-store my-session
                             :host \"myhost.com\"
                             :user \"imauser\"
                             :pass \"itsapassword\"
                             :ssl true)
    ;; ==>
    @my-store ; {:store <#IMAPStore-object>
              ;  :errors []}
              ;     - or -
              ; {:store nil
              ;  :errors [\"Invalid password or user unknown\"]}
"
  [session & config]
  (let [j-sess (:session @session)
        config (apply hash-map (parse-options config))
        session-config (:config @session)
        errors []
        host (util/get-first :host config session-config "localhost")
        user (util/get-first :user config session-config nil)
        password (util/get-first :pass config session-config nil)
        is-ssl (util/get-first :ssl config session-config false)
        url (URLName. "imap" host -1 "" "" "")]
    (IMAPStore. ^Session j-sess, ^URLName url)))

(defn find [session & params]
  )