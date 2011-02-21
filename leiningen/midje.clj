;; -*- indent-tabs-mode: nil -*-

(ns leiningen.midje
  (:refer-clojure :exclude [test])
  (:use [leiningen.util.ns :only [namespaces-in-dir]]
        [leiningen.compile :only [eval-in-project]]))

(defn require-namespaces-form [namespaces]
  `(do
     (require 'clojure.test)
     (require 'clojure.string)

     ;; This turns off "Testing ...." lines, which I hate, especially
     ;; when there's no failure output.
     (defmethod clojure.test/report :begin-test-ns [m#])

     (alter-var-root (var clojure.test/*report-counters*)
		     (fn [_#] (ref clojure.test/*initial-report-counters*)))
     (doseq [n# '~namespaces] (require n#))

     (let [midje-passes#    (:pass @clojure.test/*report-counters*)
	   midje-fails#     (:fail @clojure.test/*report-counters*)
           midje-failure-message#
	      (condp = midje-fails#
		  0 (format "All claimed facts (%d) have been confirmed." midje-passes#)
		  1 (format "FAILURE: %d fact was not confirmed." midje-fails#)
		  (format "FAILURE: %d facts were not confirmed." midje-fails#))
	   potential-consolation#
	       (condp = midje-passes#
		   0 ""
		   1 "(But 1 was.)"
		   (format "(But %d were.)" midje-passes#))
	   midje-consolation#
	           (if (> midje-fails# 0) potential-consolation# "")

	   ; Stashed clojure.test output
	   clojure-test-output-catcher#
	           (java.io.StringWriter.)
	   clojure-test-result#
   	           (binding [clojure.test/*test-out* clojure-test-output-catcher#]
		     (apply ~'clojure.test/run-tests '~namespaces))
	   clojure-test-output#
    	           (-> clojure-test-output-catcher# .toString clojure.string/split-lines)]


       (when (> (+ (:fail clojure-test-result#) (:error clojure-test-result#))
		0)
	 ;; For some reason, empty lines are swallowed, so I use >>> to
         ;; demarcate sections.
	 (println ">>> Output from clojure.test tests:")
	 (dorun (map println (drop-last 2 clojure-test-output#))))

       (when (> (:test clojure-test-result#) 0)
	 (println ">>> clojure.test summary:")
	 (dorun (map println (take-last 2 clojure-test-output#)))
	 (println ">>> Midje summary:"))
       
       (println midje-failure-message# midje-consolation#)

       ;; A non-nil return value is printed, so I'll just exit here.
       (System/exit (+ midje-fails#
                       (:error clojure-test-result#)
                       (:fail clojure-test-result#))))))

(defn midje [project & namespaces]
  (let [desired-namespaces  (if (empty? namespaces)
                              (concat (namespaces-in-dir (:test-path project))
                                      (namespaces-in-dir (:source-path project)))
                              (map symbol namespaces))]
    (eval-in-project project (require-namespaces-form desired-namespaces))))
