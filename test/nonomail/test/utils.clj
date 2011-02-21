(ns nonomail.test.utils
  (:use nonomail.util)
  (:use midje.sweet)
  )

(defn new-props
  []
  (java.util.Properties.))

(def sample-map {:a "foo" :b "bar" :c "baz"})

(facts "about utilities"
  "set-props"
  (let [p1 (new-props)]
    (set-props p1 sample-map)
    p1 => truthy
    (str (class p1)) => "class java.util.Properties")

  "prop-names"
  (let [p2 (new-props)]
    (set-props p2 sample-map)
    (prop-names p2) => (just ["a" "b" "c"] :in-any-order))


  "p-bean"
  (let [p3 (new-props)]
    (set-props p3 sample-map)
    (p-bean p3) => (just {:a "foo" :b "bar" :c "baz"})))
