(ns examples.browser
  (:require [nonomail.imap :as mail]))

(def mail-config
  {:user "readerdude"
   :pass "read-it-all"
   :host "192.168.56.101"})

(defn get-new-mail
  [conn]
  (let [inbox (mail/find :type :folder
                         :name "Inbox")]
    (mail/find :type :message
               :status :unread
               :folder inbox)))

(defn print-first-msg
  [conn]
  (let [msgs (get-new-mail conn)
        msg (first msgs)]
    (if-not msg
      (print "No new messages\n")
      (print (str (interpose "\n"
                       "To: " (:to msg)
                       "From: " (:from msg)
                       "Subject: " (:subject msg)
                       ""
                       (:body msg)))))))

(defn print-multipart-msg
  [conn]
  (let [msgs (get-new-mail conn)
        msg (first msgs)]
    (if-not msg
      (print "No new messages\n")
      (print (str (interpose "\n"
                       "To: " (:to msg)
                       "From: " (:from msg)
                       "Subject: " (:subject msg)
                       ""
                       (concat (filter #(#{:plain} (:type %)) (:body msg)))))))))

(defn read-all [config]
  (let [conn (mail/connect config)]
    (if (mail/error? conn)
      (println "Could not connect to mail server, " (apply str (mail/errors conn)))
      (do
        (print-first-msg conn)
        (print-multipart-msg conn)))))