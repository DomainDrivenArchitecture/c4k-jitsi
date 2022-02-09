(ns dda.c4k-jitsi.jitsi
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [shadow.resource :as rc])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.common :as cm]
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
       "jitsi/jicofo-deployment.yaml"    (rc/inline "jitsi/jicofo-deployment.yaml")
       "jitsi/jvb-deployment.yaml"       (rc/inline "jitsi/jvb-deployment.yaml")
       "jitsi/jvb-service.yaml"          (rc/inline "jitsi/jvb-service.yaml")
       "jitsi/prosody-deployment.yaml"   (rc/inline "jitsi/prosody-deployment.yaml")
       "jitsi/secret.yaml"               (rc/inline "jitsi/secret.yaml")
       "jitsi/web-deployment.yaml"       (rc/inline "jitsi/web-deployment.yaml")
       "jitsi/web-service.yaml"          (rc/inline "jitsi/web-service.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(defn generate-ingress [config]
  (->
   ; TODO: Update fqdn from config
   (yaml/from-string (yaml/load-resource "jitsi/ingress.yaml"))))

(defn generate-secret [config]
  (->
   ; TODO: Update secrets from auth
   (yaml/from-string (yaml/load-resource "jitsi/secret.yaml"))))

(defn generate-jicofo-deployment []
  (->
   (yaml/from-string (yaml/load-resource "jitsi/jicofo-deployment.yaml"))))

(defn generate-jvb-deployment []
  (->
   (yaml/from-string (yaml/load-resource "jitsi/jvb-deployment.yaml"))))

(defn generate-jvb-service []
  (yaml/from-string (yaml/load-resource "jitsi/jvb-service.yaml")))

(defn generate-prosody-deployment []
  (->
   (yaml/from-string (yaml/load-resource "jitsi/prosody-deployment.yaml"))))

(defn generate-web-deployment []
  (->
   (yaml/from-string (yaml/load-resource "jitsi/web-deployment.yaml"))))

(defn generate-web-service []
  (yaml/from-string (yaml/load-resource "jitsi/web-service.yaml")))
