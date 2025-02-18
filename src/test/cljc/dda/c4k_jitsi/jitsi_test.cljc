(ns dda.c4k-jitsi.jitsi-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.test.alpha :as st]
   [dda.c4k-jitsi.jitsi :as cut]))

(st/instrument `cut/prosody-config)
(st/instrument `cut/prosody-auth)
(st/instrument `cut/jitsi-config)
(st/instrument `cut/jibri-config)
(st/instrument `cut/web-config)
(st/instrument `cut/jvb-config)
(st/instrument `cut/etherpad-config)
(st/instrument `cut/excalidraw-config)

(deftest should-generate-prosody
  (is (= {:apiVersion "v1",
          :kind "ServiceAccount",
          :metadata
          {:name "prosody",
           :namespace "jitsi",
           :labels
           #:app.kubernetes.io{:name "prosody" :component "prosody"}}}
         (first (cut/prosody-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"}))))
  (is (= {:apiVersion "v1",
          :kind "ConfigMap",
          :metadata
          {:name "prosody-common",
           :namespace "jitsi",
           :labels
           #:app.kubernetes.io{:name "jitsi-meet" :component "prosody"}},
          :data
          {:ENABLE_AUTH "0",
           :ENABLE_GUESTS "1",
           :PUBLIC_URL "xy.xy.xy",
           :XMPP_DOMAIN "meet.jitsi",
           :XMPP_MUC_DOMAIN "muc.meet.jitsi",
           :XMPP_AUTH_DOMAIN "auth.meet.jitsi",
           :XMPP_GUEST_DOMAIN "guest.meet.jitsi",
           :XMPP_RECORDER_DOMAIN "recorder.meet.jitsi",
           :XMPP_INTERNAL_MUC_DOMAIN "internal-muc.meet.jitsi",
           :ENABLE_COLIBRI_WEBSOCKET "true",
           :ENABLE_COLIBRI_WEBSOCKET_UNSAFE_REGEX "1",
           :ENABLE_XMPP_WEBSOCKET "true",
           :ENABLE_RECORDING "true",
           :ENABLE_FILE_RECORDING_SERVICE_SHARING "true",
           :TZ "Europe/Berlin"}}
         (second (cut/prosody-config
                  {:fqdn "xy.xy.xy"
                   :namespace "jitsi"}))))
  (is (= 8
         (count (cut/prosody-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"}))))
   (is (= 5
         (count (cut/prosody-auth
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"}
                 {:jvb-auth-password "jvb-auth"
                  :jicofo-auth-password "jicofo-auth"
                  :jicofo-component-secret "jicofo-comp"})))))

(deftest should-generate-jicofo
  (is (= 4
         (count (cut/jicofo-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"})))))

(deftest should-generate-jitsi
  (is (= 1
         (count (cut/jitsi-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"})))))

(deftest should-generate-jibri
  (is (= 6
         (count (cut/jibri-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"})))))

(deftest should-generate-web
  (is (= 6
         (count (cut/web-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"})))))

(deftest should-generate-jvb
  (is (= 3
         (count (cut/jvb-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"})))))

(deftest should-generate-etherpad
  (is (= 2
         (count (cut/etherpad-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"})))))

(deftest should-generate-excalidraw
  (is (= 2
         (count (cut/excalidraw-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"})))))

(deftest should-generate-moderator-elector
  (is (= 2
         (count (cut/moderator-elector-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"})))))

(deftest should-generate-restart
  (is (= {:apiVersion "rbac.authorization.k8s.io/v1",
          :kind "RoleBinding",
          :metadata {:name "deployment-restart", :namespace "jitsi"},
          :roleRef
          {:apiGroup "rbac.authorization.k8s.io",
           :kind "Role",
           :name "deployment-restart"},
          :subjects
          [{:kind "ServiceAccount",
            :name "deployment-restart",
            :namespace "jitsi"}]}
         (second (cut/restart-config
                  {:fqdn "xy.xy.xy"
                   :namespace "jitsi"}))))
  (is (= {:apiVersion "rbac.authorization.k8s.io/v1",
          :kind "Role",
          :metadata {:name "deployment-restart", :namespace "jitsi"},
          :rules
          [{:apiGroups ["apps" "extensions"],
            :resources ["deployments"],
            :resourceNames ["etherpad" "excalidraw"],
            :verbs ["get" "patch" "list" "watch"]}]}
         (nth (cut/restart-config
               {:fqdn "xy.xy.xy"
                :namespace "jitsi"})
              2)))
  (is (= {:apiVersion "batch/v1",
          :kind "CronJob",
          :metadata {:name "restart-etherpad", :namespace "jitsi"},
          :spec
          {:concurrencyPolicy "Forbid",
           :schedule "0 2 * * *",
           :jobTemplate
           {:spec
            {:backoffLimit 2,
             :activeDeadlineSeconds 600,
             :template
             {:spec
              {:serviceAccountName "deployment-restart",
               :restartPolicy "Never",
               :containers [{:name "kubectl", :image "bitnami/kubectl"}],
               :command
               ["bash"
                "-c"
                "kubectl rollout restart deployment/etherpad && kubectl rollout status deployment/etherpad"]}}}}}}
         (nth (cut/restart-config
               {:fqdn "xy.xy.xy"
                :namespace "jitsi"})
              3)))
  (is (= 5
         (count (cut/restart-config
                 {:fqdn "xy.xy.xy"
                  :namespace "jitsi"})))))
