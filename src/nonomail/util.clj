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

(defn prop-names
  "Given a java.util.Properties object, return a seq of the property names
on the object, as strings."
  [p]
  (enumeration-seq (.propertyNames p)))

(defn p-bean
  "Given a java.util.Properties object, returns a map of the properties
set on the object, with the key names converted to keywords."
  [p]
  (let [keys (prop-names p)
	pull-key (fn [m k] (assoc m (keyword k) (.get p k)))]
    (reduce pull-key {} keys)))
