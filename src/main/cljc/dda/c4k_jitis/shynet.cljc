(ns dda.c4k-jitsi.jitsi
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [shadow.resource :as rc])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.predicate :as pred]))

(s/def ::fqdn pred/fqdn-string?)
(s/def ::issuer pred/letsencrypt-issuer?)
(s/def ::django-secret-key pred/bash-env-string?)

(defn ingress-type?
  [input]
  (contains? #{:traefik :default} input))

(s/def ::ingress-type ingress-type?)

#?(:cljs
   (defmethod yaml/load-resource :jitsi [resource-name]
     (case resource-name
       "jitsi/secret.yaml" (rc/inline "jitsi/secret.yaml")
       "jitsi/certificate.yaml" (rc/inline "jitsi/certificate.yaml")
       "jitsi/deployments.yaml" (rc/inline "jitsi/deployments.yaml")
       "jitsi/ingress.yaml" (rc/inline "jitsi/ingress.yaml")  
       "jitsi/service-redis.yaml" (rc/inline "jitsi/service-redis.yaml")
       "jitsi/service-webserver.yaml" (rc/inline "jitsi/service-webserver.yaml")
       "jitsi/statefulset.yaml" (rc/inline "jitsi/statefulset.yaml")
       (throw (js/Error. "Undefined Resource!")))))
 
(defn generate-secret [config]
  (let [{:keys [fqdn django-secret-key postgres-db-user postgres-db-password]} config]
    (->
     (yaml/from-string (yaml/load-resource "jitsi/secret.yaml"))
     ; See comment in secret.yaml
     ;(assoc-in [:stringData :ALLOWED_HOSTS] fqdn)
     (assoc-in [:stringData :DJANGO_SECRET_KEY] django-secret-key)
     (assoc-in [:stringData :DB_USER] postgres-db-user)
     (assoc-in [:stringData :DB_PASSWORD] postgres-db-password))))

(defn generate-certificate [config]
  (let [{:keys [fqdn issuer]} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "jitsi/certificate.yaml"))
     (assoc-in [:spec :commonName] fqdn)
     (assoc-in [:spec :dnsNames] [fqdn])
     (assoc-in [:spec :issuerRef :name] letsencrypt-issuer))))

(defn generate-webserver-deployment []
  (let [jitsi-application "jitsi-webserver"]
    (-> (yaml/from-string (yaml/load-resource "jitsi/deployments.yaml"))
        (cm/replace-all-matching-values-by-new-value "jitsi-application" jitsi-application)
        (update-in [:spec :template :spec :containers 0] dissoc :command))))

(defn generate-celeryworker-deployment []
  (let [jitsi-application "jitsi-celeryworker"]
    (-> (yaml/from-string (yaml/load-resource "jitsi/deployments.yaml"))
        (cm/replace-all-matching-values-by-new-value "jitsi-application" jitsi-application))))

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

(defn generate-statefulset []
  (yaml/from-string (yaml/load-resource "jitsi/statefulset.yaml")))

(defn generate-service-redis []
  (yaml/from-string (yaml/load-resource "jitsi/service-redis.yaml")))

(defn generate-service-webserver []
  (yaml/from-string (yaml/load-resource "jitsi/service-webserver.yaml")))
