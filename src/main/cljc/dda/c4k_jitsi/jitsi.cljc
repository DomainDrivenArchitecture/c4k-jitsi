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

#?(:cljs
   (defmethod yaml/load-resource :jitsi [resource-name]
     (case resource-name
       "jitsi/ingress.yaml"              (rc/inline "jitsi/ingress.yaml")
       "jitsi/jvb-service.yaml"          (rc/inline "jitsi/jvb-service.yaml")
       "jitsi/secret.yaml"               (rc/inline "jitsi/secret.yaml")
       "jitsi/web-service.yaml"          (rc/inline "jitsi/web-service.yaml")
       "jitsi/pod-security-policy.yaml"  (rc/inline "jitsi/pod-security-policy.yaml")
       "jitsi/role-binding.yaml"         (rc/inline "jitsi/role-binding.yaml")
       "jitsi/role.yaml"                 (rc/inline "jitsi/role.yaml")
       "jitsi/service-account.yaml"      (rc/inline "jitsi/service-account.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(defn generate-ingress [config]
  (let [{:keys [fqdn issuer ingress-type]
         :or {issuer :staging ingress-type :default}} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")
        ingress-kind (if (= :default ingress-type) "" (name ingress-type))]
    (->
     (yaml/from-string (yaml/load-resource "jitsi/ingress.yaml"))
     (assoc-in [:metadata :annotations :cert-manager.io/cluster-issuer] letsencrypt-issuer)
     (assoc-in [:metadata :annotations :kubernetes.io/ingress.class] ingress-kind)
     (cm/replace-all-matching-values-by-new-value "fqdn" fqdn))))

(defn generate-secret [config]
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

(defn generate-deployment []
  (yaml/from-string (yaml/load-resource "jitsi/deployment.yaml")))

(defn generate-pod-security-policy []
  (yaml/from-string (yaml/load-resource "jitsi/pod-security-policy.yaml")))

(defn generate-role-binding []
  (yaml/from-string (yaml/load-resource "jitsi/role-binding.yaml")))

(defn generate-role []
  (yaml/from-string (yaml/load-resource "jitsi/role.yaml")))

(defn generate-service-account []
  (yaml/from-string (yaml/load-resource "jitsi/service-account.yaml")))