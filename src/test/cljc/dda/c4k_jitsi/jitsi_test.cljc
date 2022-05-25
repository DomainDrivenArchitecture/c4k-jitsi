(ns dda.c4k-jitsi.jitsi-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.c4k-jitsi.jitsi :as cut]))


(deftest should-generate-deployment
  (is (= {:apiVersion "apps/v1"
          :kind "Deployment"
          :metadata
          {:name "jitsi-webserver"
           :namespace "default"
           :labels {:app "jitsi-webserver"}}
          :spec
          {:selector {:matchLabels {:app "jitsi-webserver"}}
           :strategy {:type "Recreate"}
           :replicas 1
           :template
           {:metadata {:labels {:app "jitsi-webserver"}}
            :spec
            {:containers
             [{:name "jitsi-webserver"
               :image "milesmcc/jitsi:v0.12.0"
               :imagePullPolicy "IfNotPresent"
               :envFrom [{:secretRef {:name "jitsi-settings"}}]}]}}}}
         (cut/generate-deployment))))

(deftest should-generate-celeryworker-deployment
  (is (= {:apiVersion "apps/v1"
          :kind "Deployment"
          :metadata
          {:name "jitsi-celeryworker"
           :namespace "default"
           :labels {:app "jitsi-celeryworker"}}
          :spec
          {:selector {:matchLabels {:app "jitsi-celeryworker"}}
           :strategy {:type "Recreate"}
           :replicas 1
           :template
           {:metadata {:labels {:app "jitsi-celeryworker"}}
            :spec
            {:containers
             [{:name "jitsi-celeryworker"
               :image "milesmcc/jitsi:v0.12.0"
               :imagePullPolicy "IfNotPresent"
               :command ["./celeryworker.sh"]
               :envFrom [{:secretRef {:name "jitsi-settings"}}]}]}}}}
         (cut/generate-celeryworker-deployment))))

(deftest should-generate-certificate
  (is (= {:apiVersion "cert-manager.io/v1"
          :kind "Certificate"
          :metadata {:name "jitsi-cert", :namespace "default"}
          :spec
          {:secretName "jitsi-secret"
           :commonName "test.com"
           :dnsNames ["test.com"]
           :issuerRef {:name "letsencrypt-staging-issuer", :kind "ClusterIssuer"}}}
         (cut/generate-certificate {:fqdn "test.com" :issuer :staging}))))

(deftest should-generate-ingress
  (is (= {:apiVersion "networking.k8s.io/v1"
          :kind "Ingress"
          :metadata
          {:name "jitsi-webserver-ingress"
           :annotations
           {:cert-manager.io/cluster-issuer "staging"
            :nginx.ingress.kubernetes.io/proxy-body-size "256m"
            :nginx.ingress.kubernetes.io/ssl-redirect "true"
            :nginx.ingress.kubernetes.io/rewrite-target "/"
            :nginx.ingress.kubernetes.io/proxy-connect-timeout "300"
            :nginx.ingress.kubernetes.io/proxy-send-timeout "300"
            :nginx.ingress.kubernetes.io/proxy-read-timeout "300"}}
          :spec
          {:tls [{:hosts ["test.com"], :secretName "jitsi-secret"}]
           :rules
           [{:host "test.com"
             :http {:paths [{:backend {:service
                                       {:name "jitsi-webserver-service" :port {:number 8080}}}, :path "/", :pathType "Prefix"}]}}]}}
         (cut/generate-ingress {:fqdn "test.com" :issuer :staging}))))

(deftest should-generate-secret
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "jitsi-settings"}
          :type "Opaque"
          :stringData
          {:DEBUG "False"
           :ALLOWED_HOSTS "*"
           :DJANGO_SECRET_KEY "django-pw"
           :ACCOUNT_SIGNUPS_ENABLED "False"
           :TIME_ZONE "America/New_York"
           :REDIS_CACHE_LOCATION
           "redis://jitsi-redis.default.svc.cluster.local/0"
           :CELERY_BROKER_URL
           "redis://jitsi-redis.default.svc.cluster.local/1"
           :DB_NAME "jitsi"
           :DB_USER "postgres-user"
           :DB_PASSWORD "postgres-pw"
           :DB_HOST "postgresql-service"
           :EMAIL_HOST_USER ""
           :EMAIL_HOST_PASSWORD ""
           :EMAIL_HOST ""
           :SERVER_EMAIL "jitsi <noreply@jitsi.example.com>"}}
         (cut/generate-secret {:fqdn "test.com" :django-secret-key "django-pw"
                               :postgres-db-user "postgres-user" :postgres-db-password "postgres-pw"}))))