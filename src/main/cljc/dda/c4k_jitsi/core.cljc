(ns dda.c4k-jitsi.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-jitsi.jitsi :as jitsi]))

(def config-defaults {:issuer :staging})

(defn k8s-objects [config]
  (map yaml/to-string
       [(jitsi/generate-secret-jitsi config)
        (jitsi/generate-certificate-jitsi config)
        (jitsi/generate-certificate-etherpad config)
        (jitsi/generate-jvb-service)
        (jitsi/generate-web-service)
        (jitsi/generate-etherpad-service)
        (jitsi/generate-ingress-jitsi config)
        (jitsi/generate-ingress-etherpad config)
        (jitsi/generate-deployment config)]))

(defn-spec generate any?
  [my-config jitsi/config?
   my-auth jitsi/auth?]
  (cm/concat-vec
   (map yaml/to-string
        (filter #(not (nil? %))
                (merge config-defaults my-config my-auth)))))
