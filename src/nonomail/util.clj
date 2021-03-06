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
  "Given a map whose keys are a mixture of keywords and strings (and possibly
other types), return a map containing only the key-value pairs whose keys
are strings."
  [a-map]
  (let [str-keys (filter string? (keys a-map))]
    (select-keys a-map str-keys)))

(defn msg->str
  "Given a javax.mail.MimeMessage object, return the string representation
of that object, as it would be passed to a Mail Delivery Agent."
  [msg]
  (with-open [out (java.io.ByteArrayOutputStream.)]
      (.writeTo msg out)
      (str out)))

(defn as-file
  "If f is a java.io.File instance, return it, else convert it to an instance
of java.io.File"
  [f]
  (if (instance? java.io.File f)
    f
    (java.io.File. f)))

(defn get-first
  "Usage: (get-first :foo map1 map2 map3 \"default\")

Given a key, plus one or more maps, plus a default value, return the value in the first
map that has a matching key, else return the default."
  [k & maps+default]
  (loop [k k, m (first maps+default) r (next maps+default)]
    (let [answer (if (map? m) (get m k) m)]
      (if answer
        answer
        (if r
          (recur k (first r) (next r))
          answer)))))

(defn parse-args
  "Parses a list of arguments, optionally containing singleton keywords. For
example, if my-list looks like this:

    [:foo 1
     :bar :b
     :baz
     :quux 4]

then (parse-args my-list #{:foo}) will return {:foo 1, :bar :b, :baz :exists,
 :quux 4}. The optional second argument is a set of keywords that are legal to
use as singletons; if you leave off the #{:foo}, parse-args will assume that
:baz and :quux are a key-value pair, and will return {:foo 1, :bar :b,
:baz :quux, 4 :exists) instead of the map you wanted."
  [coll & singles]
  (let [singleton? (if (set? (first singles))
                     (first singles)
                     (into #{} singles))]
    (loop [m {} k (first coll) r (rest coll)]
      (if (nil? k)
        m
        (let [v (first r)
              r2 (next r)]
          (cond
           (nil? v) (recur (assoc m k :exists) nil nil)
           (keyword? v) (if (singleton? k)
                          (recur (assoc m k :exists) v r2)
                          (recur (assoc m k v) (first r2) (rest r2)))
           :else (recur (assoc m k v) (first r2) (rest r2))))))))
        