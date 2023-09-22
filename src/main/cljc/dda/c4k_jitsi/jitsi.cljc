(ns dda.c4k-jitsi.jitsi
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [shadow.resource :as rc])
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.ingress :as ing]
  [dda.c4k-common.base64 :as b64]
  [dda.c4k-common.predicate :as cp]))

(s/def ::fqdn cp/fqdn-string?)
(s/def ::issuer cp/letsencrypt-issuer?)
(s/def ::jvb-auth-password cp/bash-env-string?)
(s/def ::jicofo-auth-password cp/bash-env-string?)
(s/def ::jicofo-component-secret cp/bash-env-string?)

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer]))

(def auth? (s/keys :req-un [::jvb-auth-password 
                            ::jicofo-auth-password 
                            ::jicofo-component-secret]))

#?(:cljs
   (defmethod yaml/load-resource :jitsi [resource-name]
     (case resource-name
       "jitsi/deployment.yaml"           (rc/inline "jitsi/deployment.yaml")
       "jitsi/etherpad-service.yaml"     (rc/inline "jitsi/etherpad-service.yaml")
       "jitsi/jvb-service.yaml"          (rc/inline "jitsi/jvb-service.yaml")
       "jitsi/secret.yaml"               (rc/inline "jitsi/secret.yaml")
       "jitsi/web-service.yaml"          (rc/inline "jitsi/web-service.yaml")
       (throw (js/Error. "Undefined Resource!")))))

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

(defn-spec generate-secret-jitsi cp/map-or-seq?
  [auth auth?]
  (let [{:keys [jvb-auth-password jicofo-auth-password jicofo-component-secret]} auth]
    (->
     (yaml/from-string (yaml/load-resource "jitsi/secret.yaml"))
     (cm/replace-key-value :JVB_AUTH_PASSWORD (b64/encode jvb-auth-password))
     (cm/replace-key-value :JICOFO_AUTH_PASSWORD (b64/encode jicofo-auth-password))
     (cm/replace-key-value :JICOFO_COMPONENT_SECRET (b64/encode jicofo-component-secret)))))

(defn-spec generate-jvb-service cp/map-or-seq? []
  (yaml/from-string (yaml/load-resource "jitsi/jvb-service.yaml")))

(defn-spec generate-web-service cp/map-or-seq? []
  (yaml/load-as-edn "jitsi/web-service.yaml"))

(defn-spec generate-etherpad-service cp/map-or-seq? []
  (yaml/load-as-edn "jitsi/etherpad-service.yaml"))

(defn-spec generate-excalidraw-backend-service cp/map-or-seq? []
  (yaml/load-as-edn "jitsi/excalidraw-backend-service.yaml"))

(defn-spec generate-deployment cp/map-or-seq?
  [config config?]
  (let [{:keys [fqdn]} config]
    (->
     (yaml/load-as-edn "jitsi/deployment.yaml")
     (cm/replace-all-matching-values-by-new-value "REPLACE_JITSI_FQDN" fqdn)
     (cm/replace-all-matching-values-by-new-value "REPLACE_ETHERPAD_URL"
                                                  (str "https://etherpad." fqdn "/p/"))
     (cm/replace-all-matching-values-by-new-value "REPLACE_EXCALIDRAW_BACKEND_URL"
                                                  (str "https://excalidraw-backend." fqdn)))))

(defn-spec generate-excalidraw-deployment cp/map-or-seq? []
  (yaml/load-as-edn "jitsi/excalidraw-deployment.yaml"))
