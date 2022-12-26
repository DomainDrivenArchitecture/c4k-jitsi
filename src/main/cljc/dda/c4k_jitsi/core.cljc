(ns dda.c4k-jitsi.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-jitsi.jitsi :as jitsi]))

(def config-defaults {:issuer "staging"})

(defn k8s-objects [config auth]
  (map yaml/to-string
       (filter
        #(not (nil? %))
        (cm/concat-vec
         [(jitsi/generate-secret-jitsi auth)
          (jitsi/generate-jvb-service)
          (jitsi/generate-web-service)
          (jitsi/generate-etherpad-service)
          (jitsi/generate-deployment config)]
         (jitsi/generate-ingress-web config)
         (jitsi/generate-ingress-etherpad config)))))
