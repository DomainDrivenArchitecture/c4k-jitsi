(ns dda.c4k-jitsi.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.postgres :as postgres]
  [dda.c4k-jitsi.jitsi :as jitsi]))

(def config-defaults {:issuer :staging})

(def config? (s/keys :req-un [::jitsi/fqdn]
                     :opt-un [::jitsi/issuer ::jitsi/ingress-type]))

(def auth? (s/keys :req-un [::jitsi/django-secret-key
                            ::postgres/postgres-db-user ::postgres/postgres-db-password]))

(defn k8s-objects [config]
  (into
   []
   (concat
    [(yaml/to-string (postgres/generate-config {:postgres-size :2gb :db-name "jitsi"}))
     (yaml/to-string (postgres/generate-secret config))
     (yaml/to-string (postgres/generate-persistent-volume {:postgres-data-volume-path "/var/postgres"}))
     (yaml/to-string (postgres/generate-pvc))
     (yaml/to-string (postgres/generate-deployment :postgres-image "postgres:14"))
     (yaml/to-string (postgres/generate-service))]
    [(yaml/to-string (jitsi/generate-secret config))
     (yaml/to-string (jitsi/generate-webserver-deployment))
     (yaml/to-string (jitsi/generate-celeryworker-deployment))
     (yaml/to-string (jitsi/generate-ingress config))
     (yaml/to-string (jitsi/generate-certificate config))
     (yaml/to-string (jitsi/generate-service-redis))
     (yaml/to-string (jitsi/generate-service-webserver))
     (yaml/to-string (jitsi/generate-statefulset))])))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (let [resulting-config (merge config-defaults my-config my-auth)]
    (cs/join
     "\n---\n"
     (k8s-objects resulting-config))))
