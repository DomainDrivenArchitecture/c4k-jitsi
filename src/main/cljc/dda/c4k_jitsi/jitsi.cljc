(ns dda.c4k-jitsi.jitsi
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [shadow.resource :as rc])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.base64 :as b64]
  [dda.c4k-common.predicate :as pred]))

(s/def ::fqdn pred/fqdn-string?)
(s/def ::issuer pred/letsencrypt-issuer?)
(s/def ::jvb-auth-password pred/bash-env-string?)
(s/def ::jicofo-auth-password pred/bash-env-string?)
(s/def ::jicofo-component-secret pred/bash-env-string?)

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer ::ingress-type]))

(def auth? (s/keys :req-un [::jvb-auth-password 
                            ::jicofo-auth-password 
                            ::jicofo-component-secret]))

#?(:cljs
   (defmethod yaml/load-resource :jitsi [resource-name]
     (case resource-name
       "jitsi/deployment.yaml"           (rc/inline "jitsi/deployment.yaml")
       "jitsi/etherpad-service.yaml"     (rc/inline "jitsi/etherpad-service.yaml")
       "jitsi/ingress-jitsi.yaml"        (rc/inline "jitsi/ingress-jitsi.yaml")
       "jitsi/ingress-etherpad.yaml"     (rc/inline "jitsi/ingress-etherpad.yaml")
       "jitsi/jvb-service.yaml"          (rc/inline "jitsi/jvb-service.yaml")
       "jitsi/secret.yaml"               (rc/inline "jitsi/secret.yaml")
       "jitsi/web-service.yaml"          (rc/inline "jitsi/web-service.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(defn generate-ingress-jitsi [config]
  (let [{:keys [fqdn issuer ingress-type]
         :or {issuer :staging ingress-type :default}} config
        letsencrypt-issuer (name issuer)
        ingress-kind (if (= :default ingress-type) "" (name ingress-type))]
    (->
     (yaml/from-string (yaml/load-resource "jitsi/ingress-jitsi.yaml"))
     (assoc-in [:metadata :annotations :cert-manager.io/cluster-issuer] letsencrypt-issuer)
     (assoc-in [:metadata :annotations :kubernetes.io/ingress.class] ingress-kind)
     (cm/replace-all-matching-values-by-new-value "REPLACE_JITSI_FQDN" fqdn))))

(defn generate-ingress-etherpad [config]
  (let [{:keys [fqdn issuer ingress-type]
         :or {issuer :staging ingress-type :default}} config
        letsencrypt-issuer (name issuer)
        ingress-kind (if (= :default ingress-type) "" (name ingress-type))]
    (->
     (yaml/from-string (yaml/load-resource "jitsi/ingress-etherpad.yaml"))
     (assoc-in [:metadata :annotations :cert-manager.io/cluster-issuer] letsencrypt-issuer)
     (assoc-in [:metadata :annotations :kubernetes.io/ingress.class] ingress-kind)
     (cm/replace-all-matching-values-by-new-value "REPLACE_ETHERPAD_FQDN" 
                                                  (str "etherpad." fqdn)))))

(defn generate-secret-jitsi [config]
  (let [{:keys [jvb-auth-password jicofo-auth-password jicofo-component-secret]} config]
    (->
     (yaml/from-string (yaml/load-resource "jitsi/secret.yaml"))
     (cm/replace-key-value :JVB_AUTH_PASSWORD (b64/encode jvb-auth-password))
     (cm/replace-key-value :JICOFO_AUTH_PASSWORD (b64/encode jicofo-auth-password))
     (cm/replace-key-value :JICOFO_COMPONENT_SECRET (b64/encode jicofo-component-secret)))))

(defn generate-jvb-service []
  (yaml/from-string (yaml/load-resource "jitsi/jvb-service.yaml")))

(defn generate-web-service []
  (yaml/from-string (yaml/load-resource "jitsi/web-service.yaml")))

(defn generate-etherpad-service []
  (yaml/from-string (yaml/load-resource "jitsi/etherpad-service.yaml")))

(defn generate-deployment [config]
  (let [{:keys [fqdn]} config]
    (->
     (yaml/from-string (yaml/load-resource "jitsi/deployment.yaml"))
     (cm/replace-all-matching-values-by-new-value "REPLACE_JITSI_FQDN" fqdn)
     (cm/replace-all-matching-values-by-new-value "REPLACE_ETHERPAD_URL"
                                                  (str "https://etherpad." fqdn "/p/")))))

(defn generate-certificate-jitsi
  [config]
  (let [{:keys [fqdn issuer ingress-type]
         :or {issuer :staging ingress-type :default}} config
        letsencrypt-issuer (name issuer)
        ingress-kind (if (= :default ingress-type) "" (name ingress-type))]
    (->
     (yaml/load-as-edn "jitsi/certificate-jitsi.yaml")
     (assoc-in [:spec :issuerRef :name] letsencrypt-issuer)
     (cm/replace-all-matching-values-by-new-value "REPLACE_JITSI_FQDN" fqdn))))

(defn generate-certificate-etherpad
  [config]
  (let [{:keys [fqdn issuer ingress-type]
         :or {issuer :staging ingress-type :default}} config
        letsencrypt-issuer (name issuer)
        ingress-kind (if (= :default ingress-type) "" (name ingress-type))]
    (->
     (yaml/load-as-edn "jitsi/certificate-etherpad.yaml")
     (assoc-in [:spec :issuerRef :name] letsencrypt-issuer)
     (cm/replace-all-matching-values-by-new-value "REPLACE_ETHERPAD_FQDN" (str "etherpad." fqdn)))))