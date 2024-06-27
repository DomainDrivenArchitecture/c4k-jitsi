(ns dda.c4k-jitsi.core
 (:require
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.predicate :as cp]
  [dda.c4k-common.monitoring :as mon]
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-jitsi.jitsi :as jitsi]))

(def config-defaults {:issuer "staging"})

(s/def ::mon-cfg ::mon/mon-cfg)
(s/def ::mon-auth ::mon/mon-auth)

(def config? (s/keys :req-un [::jitsi/fqdn]
                     :opt-un [::jitsi/issuer
                              ::mon-cfg]))

(def auth? (s/keys :req-un [::jitsi/jvb-auth-password
                            ::jitsi/jicofo-auth-password
                            ::jitsi/jicofo-component-secret]
                   :opt-un [::mon-auth]))

(defn-spec k8s-objects cp/map-or-seq?
  [config config?
   auth auth?]
  (map yaml/to-string
       (filter
        #(not (nil? %))
        (cm/concat-vec
         [(jitsi/generate-secret-jitsi auth)
          (jitsi/generate-jvb-service)
          (jitsi/generate-web-service)
          (jitsi/generate-etherpad-service)
          (jitsi/generate-excalidraw-backend-service)
          (jitsi/generate-meapp-fullstack-service)
          (jitsi/generate-deployment config)
          (jitsi/generate-excalidraw-deployment)
          (jitsi/generate-meapp-deployment)]  
         (jitsi/generate-ingress-web config)
         (jitsi/generate-ingress-etherpad config)
         (jitsi/generate-ingress-meapp-fullstack config)
         (when (:contains? config :mon-cfg)
           (mon/generate (:mon-cfg config) (:mon-auth auth)))))))
