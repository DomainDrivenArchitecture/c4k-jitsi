(ns dda.c4k-jitsi.jitsi
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [shadow.resource :as rc])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.predicate :as pred]))

(s/def ::fqdn pred/fqdn-string?)
(s/def ::issuer pred/letsencrypt-issuer?)

#?(:cljs
   (defmethod yaml/load-resource :jitsi [resource-name]
     (case resource-name
       "jitsi/jicofo-deployment.yaml"    (rc/inline "jitsi/jicofo-deployment.yaml")
       "jitsi/jicofo-pvc.yaml"           (rc/inline "jitsi/jicofo-pvc.yaml")
       "jitsi/jvb-deployment.yaml"       (rc/inline "jitsi/jvb-deployment.yaml")
       "jitsi/jvb-pvc.yaml"              (rc/inline "jitsi/jvb-pvc.yaml")
       "jitsi/jvb-service.yaml"          (rc/inline "jitsi/jvb-service.yaml")
       "jitsi/networkpolicy.yaml"        (rc/inline "jitsi/networkpolicy.yaml")
       "jitsi/prosody-deployment.yaml"   (rc/inline "jitsi/prosody-deployment.yaml")
       "jitsi/prosody-pvc-config.yaml"   (rc/inline "jitsi/prosody-pvc-config.yaml")
       "jitsi/prosody-pvc-plugins.yaml"  (rc/inline "jitsi/prosody-pvc-plugins.yaml")
       "jitsi/prosody-service.yaml"      (rc/inline "jitsi/prosody-service.yaml")
       "jitsi/web-deployment.yaml"       (rc/inline "jitsi/web-deployment.yaml")
       "jitsi/web-pvc-config.yaml"       (rc/inline "jitsi/web-pvc-config.yaml")
       "jitsi/web-pvc-crontabs.yaml"     (rc/inline "jitsi/web-pvc-crontabs.yaml")
       "jitsi/web-pvc-transcripts.yaml"  (rc/inline "jitsi/web-pvc-transcripts.yaml")
       "jitsi/web-service.yaml"          (rc/inline "jitsi/web-service.yaml")
       (throw (js/Error. "Undefined Resource!")))))
 
(defn generate-jicofo-deployment [config]
  (->
   (yaml/from-string (yaml/load-resource "jitsi/jicofo-deployment.yaml"))))

(defn generate-jicofo-pvc []
  (yaml/from-string (yaml/load-resource "jitsi/jicofo-pvc.yaml")))

(defn generate-jvb-deployment [config]
  (->
   (yaml/from-string (yaml/load-resource "jitsi/jvb-deployment.yaml"))))

(defn generate-jvb-pvc []
  (yaml/from-string (yaml/load-resource "jitsi/jvb-pvc.yaml")))

(defn generate-jvb-service []
  (yaml/from-string (yaml/load-resource "jitsi/jvb-service.yaml")))

(defn generate-networkpolicy []
  (yaml/from-string (yaml/load-resource "jitsi/networkpolicy.yaml")))

(defn generate-prosody-deployment [config]
  (->
   (yaml/from-string (yaml/load-resource "jitsi/prosody-deployment.yaml"))))

(defn generate-prosody-pvc-config []
  (yaml/from-string (yaml/load-resource "jitsi/prosody-pvc-config.yaml")))

(defn generate-prosody-pvc-plugins []
  (yaml/from-string (yaml/load-resource "jitsi/prosody-pvc-plugins.yaml")))

(defn generate-prosody-service []
  (yaml/from-string (yaml/load-resource "jitsi/prosody-service.yaml")))

(defn generate-web-deployment [config]
  (->
   (yaml/from-string (yaml/load-resource "jitsi/web-deployment.yaml"))))

(defn generate-web-pvc-config []
  (yaml/from-string (yaml/load-resource "jitsi/web-pvc-config.yaml")))

(defn generate-web-pvc-crontabs []
  (yaml/from-string (yaml/load-resource "jitsi/web-pvc-crontabs.yaml")))

(defn generate-web-pvc-transcripts []
  (yaml/from-string (yaml/load-resource "jitsi/web-pvc-transcripts.yaml")))

(defn generate-web-service []
  (yaml/from-string (yaml/load-resource "jitsi/web-service.yaml")))
