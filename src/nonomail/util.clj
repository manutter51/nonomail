(ns nonomail.util)

(defn set-props
  "Sets a java.util.Properties object to the values given in a map.
Map keys can be strings or keywords."
  [p m]
  (doseq [k (keys m)]
    (let [ky (if (keyword? k)
	       (name k)
	       k)
	  v (m k)]
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

(defn only-string-keys
  "[any-map]
Given a map whose keys are a mixture of keywords and strings (and possibly
other types), return a map containing only the key-value pairs whose keys
are strings."
  [a-map]
  (let [str-keys (filter string? (keys a-map))]
    (select-keys a-map str-keys)))

(defn msg->str
  "[msg]
Given a javax.mail.MimeMessage object, return the string representation
of that object, as it would be passed to a Mail Delivery Agent."
  [msg]
  (with-open [out (java.io.ByteArrayOutputStream.)]
      (.writeTo msg out)
      (str out)))

(defn as-file
  "[f]
If f is a java.io.File instance, return it, else convert it to an instance
of java.io.File"
  [f]
  (if (instance? java.io.File f)
    f
    (java.io.File. f)))
  