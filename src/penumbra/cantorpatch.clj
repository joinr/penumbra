;;This is a dumb patch to extend cantor's
;;operations to java.lang.Long, which
;;is clojure's default type for ints now,
;;and was causing incompatibilities
;;with legacy code.
(ns penumbra.cantorpatch
  (:require [cantor]))

(in-ns 'cantor)

(defmacro- extend-numbers [& body]
  `(do
     (extend-type java.lang.Double ~@body)
     (extend-type java.lang.Long ~@body)
     (extend-type java.lang.Integer ~@body)
     (extend-type java.lang.Float ~@body)
     (extend-type clojure.lang.Ratio ~@body)))

(extend-numbers
 vec/Arithmetic
 (add
  ([a] a)
  ([a b] (+ a b)))
 (sub
  ([a] (- a))
  ([a b] (- a b)))
 (mul
  ([a] a)
  ([a b] (* a b)))
 (div
  ([a] a)
  ([a b] (/ a b))))

(extend-numbers
 vec/Tuple
 (map-
  ([n f] (f n))
  ([a b f] (f a b))
  ([a b rest f] (apply f (list* a b rest))))
 (all-
  ([n f] (f n))
  ([a b f] (f a b)))
 (dimension [_] 1))

(extend-numbers
 vec/Polar
(cartesian [n] (cartesian (polar2 n 1))))

(in-ns 'penumbra.cantorpatch)
