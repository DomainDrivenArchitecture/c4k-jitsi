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

(deftest should-generate-etherpad-service
  (is (= {:apiVersion "v1",
          :kind "Service",
          :metadata
          {:labels {:service "etherpad"}, :name "etherpad", :namespace "jitsi"},
          :spec
          {:ports [{:name "etherpad", :port 9001, :targetPort 9001}],
           :selector {:app "jitsi"}}}
         (cut/generate-etherpad-service
          {:fqdn "xy.xy.xy"
           :namespace "jitsi"}))))

(deftest should-generate-excalidraw-backend-service
  (is (= {:apiVersion "v1",
          :kind "Service",
          :metadata
          {:labels {:service "excalidraw-backend"},
           :name "excalidraw-backend",
           :namespace "jitsi"},
          :spec
          {:ports [{:name "excalidraw-backend", :port 3002, :targetPort 80}],
           :selector {:app "excalidraw-backend"}}}
         (cut/generate-excalidraw-backend-service
          {:fqdn "xy.xy.xy"
           :namespace "jitsi"}))))

(deftest should-generate-modelector-service
  (is (= {:apiVersion "v1",
          :kind "Service",
          :metadata
          {:labels {:service "modelector"},
           :name "modelector",
           :namespace "jitsi"},
          :spec
          {:ports [{:name "http", :port 80, :targetPort 8080}],
           :selector {:app "modelector"}}}
         (cut/generate-modelector-service
          {:fqdn "xy.xy.xy"
           :namespace "jitsi"}))))

(deftest should-generate-modelector-deployment
  (is (= {:apiVersion "apps/v1",
          :kind "Deployment",
          :metadata
          {:labels {:app "modelector"},
           :name "modelector",
           :namespace "jitsi"},
          :spec
          {:selector {:matchLabels {:app "modelector"}},
           :replicas 1,
           :strategy {:type "Recreate"},
           :template
           {:metadata {:labels {:app "modelector"}},
            :spec
            {:containers
             [{:name "modelector",
               :image "domaindrivenarchitecture/moderator-election-vaadin_fullstack",
               :imagePullPolicy "IfNotPresent",
               :env
               [{:name "MEMBERNAMES",
                 :value "Micha,Ansgar,Erik,Mirco"}]}]}}}}
          (cut/generate-modelector-deployment
           {:fqdn "xy.xy.xy"
            :namespace "jitsi"}))))

(deftest should-generate-excalidraw-deployment
  (is (= {:apiVersion "v1",
          :kind "Service",
          :metadata
          {:labels {:service "excalidraw-backend"},
           :name "excalidraw-backend",
           :namespace "jitsi"},
          :spec
          {:ports [{:name "excalidraw-backend", :port 3002, :targetPort 80}],
           :selector {:app "excalidraw-backend"}}}
         (cut/generate-excalidraw-backend-service
          {:fqdn "xy.xy.xy"
           :namespace "jitsi"}))))

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

