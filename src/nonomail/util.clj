(ns nonomail.util)

(defn set-props
  "Sets a java.util.Properties object to the values given in a map.
Map keys can be strings or keywords."
  [p m]
  (doseq [k (keys m)]
    (let [ky (if (keyword? k)
	       (name k)
	       k)
	  v (k m)]
      (.put p ky v))))
