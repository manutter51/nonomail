(ns nonomail.core)

(defprotocol MailClient
  "A protocol for things that interact with a mail server to send and/or receive mail."
  (connect [config] "Given a map of params like username, password, and host, return a connection (session).")
  (error? [config] "Predicate: are there any errors on the given connection?")
  (errors [config] "Return the current list of errors"))
