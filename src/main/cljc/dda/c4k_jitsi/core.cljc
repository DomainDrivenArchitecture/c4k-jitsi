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

(def auth? (s/keys :req-un [::jitsi/jvb-auth-password ::jitsi/jicofo-auth-password ::jitsi/jicofo-component-secret]))

(defn k8s-objects [config]
   (map (fn [x] (yaml/to-string x))
   [(jitsi/generate-ingress config)
    (jitsi/generate-secret config)
    (jitsi/generate-jicofo-deployment)
    (jitsi/generate-jvb-deployment)
    (jitsi/generate-jvb-service)
    (jitsi/generate-prosody-deployment)
    (jitsi/generate-web-deployment)
    (jitsi/generate-web-service)]))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (let [resulting-config (merge config-defaults my-config my-auth)]
    (cs/join
     "\n---\n"
     (k8s-objects resulting-config))))
