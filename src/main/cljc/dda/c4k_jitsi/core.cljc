(ns dda.c4k-jitsi.core
  (:require
   [clojure.spec.alpha :as s]
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.predicate :as cp]
   [dda.c4k-common.ingress :as ing]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-jitsi.jitsi :as jitsi]
   [dda.c4k-common.namespace :as ns]))

(def config-defaults {:issuer "staging", 
                      :namespace "jitsi"})

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
  (let [resolved-config (merge config-defaults config)]
    (map yaml/to-string
         (filter
          #(not (nil? %))
          (cm/concat-vec
           (ns/generate resolved-config)
           (jitsi/prosody-config resolved-config)
           (jitsi/jitsi-config resolved-config)
           (jitsi/jicofo-config resolved-config)
           (jitsi/web-config resolved-config)
           (jitsi/jvb-config resolved-config)
           (jitsi/jibri-config resolved-config)
           (jitsi/etherpad-config resolved-config)
           (jitsi/excalidraw-config resolved-config)
           (ing/generate-ingress-and-cert (merge
                                           {:service-name "jitsi-meet-web"
                                            :service-port 80
                                            :fqdns [(:fqdn resolved-config)]}
                                           resolved-config))
           (ing/generate-ingress-and-cert (merge
                                           {:service-name "etherpad"
                                            :service-port 9001
                                            :fqdns [(str "etherpad." (:fqdn resolved-config))]}
                                           resolved-config))
            (ing/generate-ingress-and-cert (merge
                                            {:service-name "excalidraw"
                                             :service-port 3002
                                             :fqdns [(str "excalidraw." (:fqdn resolved-config))]}
                                            resolved-config))
           (ing/generate-ingress-and-cert (merge
                                           {:service-name "moderator-elector"
                                            :service-port 80
                                            :fqdns [(str "moderator-elector." (:fqdn resolved-config))]}
                                           resolved-config))
           (when (:contains? resolved-config :mon-cfg)
             (mon/generate-config)))))))

(defn-spec auth-objects cp/map-or-seq?
  [config config?
   auth auth?]
  (let [resolved-config (merge config-defaults config)]
  (map yaml/to-string
       (filter
        #(not (nil? %))
        (cm/concat-vec
         (jitsi/prosody-auth config auth)
         (when (:contains? config :mon-cfg)
           (mon/generate-auth (:mon-cfg config) (:mon-auth auth))))))))
