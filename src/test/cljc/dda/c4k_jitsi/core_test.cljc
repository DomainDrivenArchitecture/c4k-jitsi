(ns dda.c4k-jitsi.core-test
  (:require
   [clojure.test :refer [deftest is are testing run-tests]]
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-jitsi.core :as cut]
   ))


(deftest validate-valid-resources
  (is (s/valid? ::cut/config (yaml/load-as-edn "jitsi-test/valid-config.yaml")))
  (is (s/valid? ::cut/auth (yaml/load-as-edn "jitsi-test/valid-auth.yaml"))))

(deftest test-whole-generation
  (is (= 73
         (count
          (cut/config-objects []
           (yaml/load-as-edn "jitsi-test/valid-config.yaml")))))
  (is (= 6
         (count
          (cut/auth-objects []
           (yaml/load-as-edn "jitsi-test/valid-config.yaml")
           (yaml/load-as-edn "jitsi-test/valid-auth.yaml"))))))