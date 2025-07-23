(ns dda.c4k-jitsi.core
  (:require
   [clojure.spec.alpha :as s]
   [orchestra.core :refer [defn-spec]]
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

(s/def ::config (s/keys :req-un [::jitsi/fqdn]
                        :opt-un [::jitsi/issuer
                                 ::mon-cfg]))

(s/def ::auth (s/keys :req-un [::jitsi/jvb-auth-password
                               ::jitsi/jicofo-auth-password
                               ::jitsi/jicofo-component-secret]
                      :opt-un [::mon-auth]))

(s/def ::config-select (s/* #{"auth" "deployment"}))

(defn-spec config-objects seq?
  [config-select ::config-select
   config ::config]
  (let [resolved-config (merge config-defaults config)
        {:keys [fqdn max-rate max-concurrent-requests namespace]} resolved-config
        config-parts (if (empty? config-select)
                       ["auth" "deployment"]
                       config-select)]
    (map yaml/to-string
         (cm/concat-vec
          (cm/concat-vec
           (when (some #(= "deployment" %) config-parts)
             (ns/generate resolved-config)
             (cm/concat-vec
              (ns/generate resolved-config)
              (jitsi/prosody-config resolved-config)
              (jitsi/jitsi-config resolved-config)
              (jitsi/jicofo-config resolved-config)
              (jitsi/web-config resolved-config)
              (jitsi/jvb-config resolved-config)
              (jitsi/restart-config resolved-config)
              (jitsi/etherpad-config resolved-config)
              (jitsi/excalidraw-config resolved-config)
              (jitsi/moderator-elector-config resolved-config)
              (jitsi/coturn-config resolved-config)
              (ing/config-objects (merge
                                   {:service-name  "jitsi-meet-web"
                                    :service-port 80
                                    :fqdns [fqdn]
                                    :average-rate max-rate
                                    :burst-rate max-concurrent-requests
                                    :namespace namespace}
                                   resolved-config))
              (ing/config-objects (merge
                                   {:service-name  "etherpad"
                                    :service-port 9001
                                    :fqdns [(str "etherpad." fqdn)]
                                    :average-rate max-rate
                                    :burst-rate max-concurrent-requests
                                    :namespace namespace}
                                   resolved-config))
              (ing/config-objects (merge
                                   {:service-name  "excalidraw"
                                    :service-port 3002
                                    :fqdns [(str "excalidraw." fqdn)]
                                    :average-rate max-rate
                                    :burst-rate max-concurrent-requests
                                    :namespace namespace}
                                   resolved-config))
              (ing/config-objects (merge
                                   {:service-name  "moderator-elector"
                                    :service-port 80
                                    :fqdns [(str "moderator-elector." fqdn)]
                                    :average-rate max-rate
                                    :burst-rate max-concurrent-requests
                                    :namespace namespace}
                                   resolved-config))
              (ing/config-objects (merge
                                   {:service-name  "coturn-turn-tcp"
                                    :service-port 3478
                                    :fqdns [(str "stun." fqdn)]
                                    :average-rate max-rate
                                    :burst-rate max-concurrent-requests
                                    :namespace namespace}
                                   resolved-config))
              (when (:contains? resolved-config :mon-cfg)
                (mon/config-objects resolved-config)))))))))


(defn-spec auth-objects seq?
  [config-select ::config-select
   config ::config
   auth ::auth]
  (let [resolved-config (merge config-defaults config)
        config-parts (if (empty? config-select)
                       ["auth" "deployment" "dashboards"]
                       config-select)]
    (map yaml/to-string
         (if (some #(= "auth" %) config-parts)
           (cm/concat-vec
            (jitsi/prosody-auth resolved-config auth)
            (when (:contains? resolved-config :mon-cfg)
              (mon/auth-objects (:mon-cfg resolved-config) (:mon-auth auth))))
           []))))
