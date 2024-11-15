(ns dda.c4k-jitsi.jitsi
  (:require
   [clojure.spec.alpha :as s]
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.ingress :as ing]
   [dda.c4k-common.base64 :as b64]
   [dda.c4k-common.predicate :as cp]
   #?(:cljs [dda.c4k-common.macros :refer-macros [inline-resources]])))

(s/def ::fqdn cp/fqdn-string?)
(s/def ::issuer cp/letsencrypt-issuer?)
(s/def ::namespace string?)
(s/def ::jvb-auth-password cp/bash-env-string?)
(s/def ::jicofo-auth-password cp/bash-env-string?)
(s/def ::jicofo-component-secret cp/bash-env-string?)

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer
                              ::namespace]))

(def auth? (s/keys :req-un [::jvb-auth-password
                            ::jicofo-auth-password
                            ::jicofo-component-secret]))
#?(:cljs
   (defmethod yaml/load-resource :jitsi [resource-name]
     (get (inline-resources "jitsi") resource-name)))

(defn-spec generate-ingress-web cp/map-or-seq?
  [config config?]
  (ing/generate-ingress-and-cert
   (merge
    {:service-name "web"
     :service-port 80
     :fqdns [(:fqdn config)]}
    config)))

(defn-spec generate-ingress-etherpad cp/map-or-seq?
  [config config?]
  (ing/generate-ingress-and-cert
   (merge
    {:service-name "etherpad"
     :service-port 9001
     :fqdns [(str "etherpad." (:fqdn config))]}
    config)))

(defn-spec generate-ingress-excalidraw-backend cp/map-or-seq?
  [config config?]
  (ing/generate-ingress-and-cert
   (merge
    {:service-name "excalidraw-backend"
     :service-port 3002
     :fqdns [(str "excalidraw-backend." (:fqdn config))]}
    config)))

(defn-spec generate-ingress-modelector cp/map-or-seq?
  [config config?]
  (ing/generate-ingress-and-cert
   (merge
    {:service-name "modelector"
     :service-port 80
     :fqdns [(str "modelector." (:fqdn config))]}
    config)))

(defn-spec generate-secret-jitsi cp/map-or-seq?
  [config config?
   auth auth?]
  (let [{:keys [namespace]} config
        {:keys [jvb-auth-password jicofo-auth-password jicofo-component-secret]} auth]
    (->
     (yaml/from-string (yaml/load-resource "jitsi/secret.yaml"))
     (cm/replace-all-matching "NAMESPACE" namespace)
     (cm/replace-key-value :JVB_AUTH_PASSWORD (b64/encode jvb-auth-password))
     (cm/replace-key-value :JICOFO_AUTH_PASSWORD (b64/encode jicofo-auth-password))
     (cm/replace-key-value :JICOFO_COMPONENT_SECRET (b64/encode jicofo-component-secret)))))

(defn-spec generate-jvb-service cp/map-or-seq? 
  [config config?]
  (let [{:keys [namespace]} config]
    (->
     (yaml/from-string (yaml/load-resource "jitsi/jvb-service.yaml"))
     (cm/replace-all-matching "NAMESPACE" namespace))))

(defn-spec generate-web-service cp/map-or-seq?
  [config config?]
  (let [{:keys [namespace]} config]
    (->
     (yaml/load-as-edn "jitsi/web-service.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace))))

(defn-spec generate-etherpad-service cp/map-or-seq?
  [config config?]
  (let [{:keys [namespace]} config]
    (->
     (yaml/load-as-edn "jitsi/etherpad-service.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace))))

(defn-spec generate-excalidraw-backend-service cp/map-or-seq?
  [config config?]
  (let [{:keys [namespace]} config]
    (->
     (yaml/load-as-edn "jitsi/excalidraw-backend-service.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace))))

(defn-spec generate-modelector-service cp/map-or-seq?
  [config config?]
  (let [{:keys [namespace]} config]
    (->
     (yaml/load-as-edn "jitsi/modelector-service.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace))))

(defn-spec generate-deployment cp/map-or-seq?
  [config config?]
  (let [{:keys [fqdn namespace]} config]
    (->
     (yaml/load-as-edn "jitsi/deployment.yaml")
     (cm/replace-all-matching "REPLACE_JITSI_FQDN" fqdn)
     (cm/replace-all-matching "NAMESPACE" namespace)
     (cm/replace-all-matching "REPLACE_ETHERPAD_URL"
                              (str "https://etherpad." fqdn "/p/"))
     
     (cm/replace-all-matching "REPLACE_EXCALIDRAW_BACKEND_URL"
                              (str "https://excalidraw-backend." fqdn)))))

(defn-spec generate-excalidraw-deployment cp/map-or-seq?
  [config config?]
  (let [{:keys [fqdn namespace]} config]
    (->
     (yaml/load-as-edn "jitsi/excalidraw-deployment.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace))))

(defn-spec generate-modelector-deployment cp/map-or-seq?
  [config config?]
  (let [{:keys [fqdn namespace]} config]
    (->
     (yaml/load-as-edn "jitsi/modelector-deployment.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace))))
