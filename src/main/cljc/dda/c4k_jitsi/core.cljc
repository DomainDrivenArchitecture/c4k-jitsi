(ns dda.c4k-jitsi.core
  (:require
   [clojure.spec.alpha :as s]
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.predicate :as cp]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-jitsi.jitsi :as jitsi]
   [dda.c4k-common.namespace :as ns]))

(def config-defaults {:issuer "staging", :namespace "jitsi"})

(s/def ::mon-cfg ::mon/mon-cfg)
(s/def ::mon-auth ::mon/mon-auth)

(def config? (s/keys :req-un [::jitsi/fqdn]
                     :opt-un [::jitsi/issuer
                              ::mon-cfg]))

(def auth? (s/keys :req-un [::jitsi/jvb-auth-password
                            ::jitsi/jicofo-auth-password
                            ::jitsi/jicofo-component-secret]
                   :opt-un [::mon-auth]))

(defn-spec config-objects cp/map-or-seq?
  [config config?]
  (map yaml/to-string
       (filter
        #(not (nil? %))
        (cm/concat-vec
         (ns/generate config)
         [(jitsi/generate-jvb-service config)
          (jitsi/generate-web-service config)
          (jitsi/generate-etherpad-service config)
          (jitsi/generate-excalidraw-backend-service config)
          (jitsi/generate-modelector-service config)
          (jitsi/generate-deployment config)
          (jitsi/generate-excalidraw-deployment config)
          (jitsi/generate-modelector-deployment config)]
         (jitsi/generate-ingress-web config)
         (jitsi/generate-ingress-etherpad config)
         (jitsi/generate-ingress-excalidraw-backend config)
         (jitsi/generate-ingress-modelector config)
         (when (:contains? config :mon-cfg)
           (mon/generate-config))))))

(defn-spec auth-objects cp/map-or-seq?
  [config config?
   auth auth?]
  (map yaml/to-string
       (filter
        #(not (nil? %))
        (cm/concat-vec
         [(jitsi/generate-secret-jitsi config auth)]
         (when (:contains? config :mon-cfg)
           (mon/generate-auth (:mon-cfg config) (:mon-auth auth)))))))

