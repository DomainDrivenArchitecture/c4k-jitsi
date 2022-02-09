(ns dda.c4k-shynet.shynet
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
   (defmethod yaml/load-resource :shynet [resource-name]
     (case resource-name
       "shynet/secret.yaml" (rc/inline "shynet/secret.yaml")
       "shynet/certificate.yaml" (rc/inline "shynet/certificate.yaml")
       "shynet/deployments.yaml" (rc/inline "shynet/deployments.yaml")
       "shynet/ingress.yaml" (rc/inline "shynet/ingress.yaml")  
       "shynet/service-redis.yaml" (rc/inline "shynet/service-redis.yaml")
       "shynet/service-webserver.yaml" (rc/inline "shynet/service-webserver.yaml")
       "shynet/statefulset.yaml" (rc/inline "shynet/statefulset.yaml")
       (throw (js/Error. "Undefined Resource!")))))
 
(defn generate-secret [config]
  (let [{:keys [fqdn django-secret-key postgres-db-user postgres-db-password]} config]
    (->
     (yaml/from-string (yaml/load-resource "shynet/secret.yaml"))
     ; See comment in secret.yaml
     ;(assoc-in [:stringData :ALLOWED_HOSTS] fqdn)
     (assoc-in [:stringData :DJANGO_SECRET_KEY] django-secret-key)
     (assoc-in [:stringData :DB_USER] postgres-db-user)
     (assoc-in [:stringData :DB_PASSWORD] postgres-db-password))))

(defn generate-certificate [config]
  (let [{:keys [fqdn issuer]} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "shynet/certificate.yaml"))
     (assoc-in [:spec :commonName] fqdn)
     (assoc-in [:spec :dnsNames] [fqdn])
     (assoc-in [:spec :issuerRef :name] letsencrypt-issuer))))

(defn generate-webserver-deployment []
  (let [shynet-application "shynet-webserver"]
    (-> (yaml/from-string (yaml/load-resource "shynet/deployments.yaml"))
        (cm/replace-all-matching-values-by-new-value "shynet-application" shynet-application)
        (update-in [:spec :template :spec :containers 0] dissoc :command))))

(defn generate-celeryworker-deployment []
  (let [shynet-application "shynet-celeryworker"]
    (-> (yaml/from-string (yaml/load-resource "shynet/deployments.yaml"))
        (cm/replace-all-matching-values-by-new-value "shynet-application" shynet-application))))

(defn generate-ingress [config]
  (let [{:keys [fqdn issuer ingress-type]
         :or {issuer :staging ingress-type :default}} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")
        ingress-kind (if (= :default ingress-type) "" (name ingress-type))]
    (->
     (yaml/from-string (yaml/load-resource "shynet/ingress.yaml"))
     (assoc-in [:metadata :annotations :cert-manager.io/cluster-issuer] letsencrypt-issuer)
     (assoc-in [:metadata :annotations :kubernetes.io/ingress.class] ingress-kind)
     (cm/replace-all-matching-values-by-new-value "fqdn" fqdn))))

(defn generate-statefulset []
  (yaml/from-string (yaml/load-resource "shynet/statefulset.yaml")))

(defn generate-service-redis []
  (yaml/from-string (yaml/load-resource "shynet/service-redis.yaml")))

(defn generate-service-webserver []
  (yaml/from-string (yaml/load-resource "shynet/service-webserver.yaml")))
