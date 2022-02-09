(ns dda.c4k-shynet.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.postgres :as postgres]
  [dda.c4k-shynet.shynet :as shynet]))

(def config-defaults {:issuer :staging})

(def config? (s/keys :req-un [::shynet/fqdn]
                     :opt-un [::shynet/issuer ::shynet/ingress-type]))

(def auth? (s/keys :req-un [::shynet/django-secret-key
                            ::postgres/postgres-db-user ::postgres/postgres-db-password]))

(defn k8s-objects [config]
  (into
   []
   (concat
    [(yaml/to-string (postgres/generate-config {:postgres-size :2gb :db-name "shynet"}))
     (yaml/to-string (postgres/generate-secret config))
     (yaml/to-string (postgres/generate-persistent-volume {:postgres-data-volume-path "/var/postgres"}))
     (yaml/to-string (postgres/generate-pvc))
     (yaml/to-string (postgres/generate-deployment :postgres-image "postgres:14"))
     (yaml/to-string (postgres/generate-service))]
    [(yaml/to-string (shynet/generate-secret config))
     (yaml/to-string (shynet/generate-webserver-deployment))
     (yaml/to-string (shynet/generate-celeryworker-deployment))
     (yaml/to-string (shynet/generate-ingress config))
     (yaml/to-string (shynet/generate-certificate config))
     (yaml/to-string (shynet/generate-service-redis))
     (yaml/to-string (shynet/generate-service-webserver))
     (yaml/to-string (shynet/generate-statefulset))])))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (let [resulting-config (merge config-defaults my-config my-auth)]
    (cs/join
     "\n---\n"
     (k8s-objects resulting-config))))
