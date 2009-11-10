;   Copyright (c) Zachary Tellman. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns penumbra.opengl.core
  (:use [clojure.contrib.def :only (defn-memo defmacro-)])
  (:import (javax.media.opengl GL2))
  (:import (javax.media.opengl.glu.gl2 GLUgl2))
  (:import (com.sun.opengl.util.gl2 GLUT))
  (:import (java.lang.reflect Field)))

(def #^GL2 *gl* nil)
(def #^GLUgl2 *glu* (new GLUgl2))
(def #^GLUT *glut* (new GLUT))

(def *inside-begin-end* false)
(def *intra-primitive-transform* (atom false))
(def *transform-matrix* (atom nil))
(def *program* nil)
(def *uniforms* nil)

(def *texture-pool* nil)
(def *tex-mem-threshold* 100e6)
(def *tex-count-threshold* 100)

;;;;;;;;;;;;;;;;;;;;;;

(def *check-errors* true) ;makes any OpenGL error throw an exception
(defn check-errors [] *check-errors*)

(defn enum-name
  "Takes the numeric value of a gl constant (i.e. GL_LINEAR), and gives the name"
  [enum-value]
  (if (= 0 enum-value)
    "NONE"
    (let [fields (seq (.. *gl* (getClass) (getFields)))]
      (.getName #^Field (some #(if (= enum-value (.get #^Field % *gl*)) % nil) fields)))))     

(defn check-error []
  (let [error (.glGetError *gl*)]
    (if (not (zero? error))
      (throw (Exception. (str "OpenGL error: " (enum-name error)))))))

(defn enum-macro [k]
 (if (keyword? k)
   (let [gl (str "GL_" (.. (name k) (replace \- \_) (toUpperCase)))]
    `(. GL2 ~(symbol gl)))
   k))

(defn-memo enum [k]
  (let [gl (str "GL_" (.. (name k) (replace \- \_) (toUpperCase)))]
    (eval `(. GL2 ~(symbol gl)))))

(defmacro gl-import
  "Imports an OpenGL function, transforming all :keywords into GL_KEYWORDS"
  [import-from import-as]
  `(defmacro ~import-as [& args#]
    `(do
      (let [~'value# (. #^GL2 *gl* ~'~import-from ~@(map enum-macro args#))]
        (if (and (check-errors) (not *inside-begin-end*)) (check-error))
        ~'value#))))

(defmacro gl-import-
  "Private version of gl-import"
  [name & decls]
  (list* `gl-import (with-meta name (assoc (meta name) :private true)) decls))

(defmacro glu-import [import-from import-as]
  `(defmacro ~import-as [& args#]
      `(. *glu* ~'~import-from ~@(map enum-macro args#))))

(defmacro glu-import-
  "Private version of glu-import"
  [name & decls]
  (list* `glu-import (with-meta name (assoc (meta name) :private true)) decls))

(defmacro glut-import [import-from import-as]
  `(defmacro ~import-as [& args#]
      `(. *glut* ~'~import-from ~@(map enum-macro args#))))

(defmacro glut-import-
  "Private version of glu-import"
  [name & decls]
  (list* `glut-import (with-meta name (assoc (meta name) :private true)) decls))

;;;;;;;;;;;;;;;;;;;;;;
