(ns dda.c4k-jitsi.core-test
  (:require
   #?(:cljs [shadow.resource :as rc])
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-jitsi.core :as cut]))

#?(:cljs
   (defmethod yaml/load-resource :jitsi-test [resource-name]
     (case resource-name
       "jitsi-test/valid-auth.yaml"   (rc/inline "jitsi-test/valid-auth.yaml")
       "jitsi-test/valid-config.yaml" (rc/inline "jitsi-test/valid-config.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(deftest validate-valid-resources
  (is (s/valid? cut/config? (yaml/load-as-edn "jitsi-test/valid-config.yaml")))
  (is (s/valid? cut/auth? (yaml/load-as-edn "jitsi-test/valid-auth.yaml")))
  )