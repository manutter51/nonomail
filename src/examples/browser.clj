(ns examples.browser
  (:require [nonomail.session :as sess]
            [nonomail.imap :as mail]))

(def mail-config
  {:user "readerdude"
   :pass "read-it-all"
   :host "localhost"
   })

