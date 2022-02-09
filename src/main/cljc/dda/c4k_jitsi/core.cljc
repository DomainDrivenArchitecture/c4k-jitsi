(ns dda.c4k-jitsi.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-jitsi.jitsi :as jitsi]))

(def config-defaults {:issuer :staging})

(def config? (s/keys :req-un [::jitsi/fqdn]
                     :opt-un [::jitsi/issuer ::jitsi/ingress-type]))

(def auth? (s/keys :req-un []))

(defn k8s-objects [config]
   (map (fn [x] (yaml/to-string x))
   [(jitsi/generate-jicofo-deployment config)
    (jitsi/generate-jicofo-pvc)
    (jitsi/generate-jvb-deployment config)
    (jitsi/generate-jvb-pvc)
    (jitsi/generate-jvb-service)
    (jitsi/generate-networkpolicy)
    (jitsi/generate-prosody-deployment config)
    (jitsi/generate-prosody-pvc-config)
    (jitsi/generate-prosody-pvc-plugins)
    (jitsi/generate-prosody-service)
    (jitsi/generate-web-deployment config)
    (jitsi/generate-web-pvc-config)
    (jitsi/generate-web-pvc-crontabs)
    (jitsi/generate-web-pvc-transcripts)]))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (let [resulting-config (merge config-defaults my-config my-auth)]
    (cs/join
     "\n---\n"
     (k8s-objects resulting-config))))
