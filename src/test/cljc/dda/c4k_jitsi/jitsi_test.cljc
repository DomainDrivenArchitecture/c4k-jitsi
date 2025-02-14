(ns dda.c4k-jitsi.jitsi-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.test.alpha :as st]
   [dda.c4k-jitsi.jitsi :as cut]))

(st/instrument `cut/generate-deployment)
(st/instrument `cut/generate-secret-jitsi)
(st/instrument `cut/generate-ingress-web)
(st/instrument `cut/generate-jvb-service)

(deftest should-generate-deployment
  (is (= {:apiVersion "apps/v1",
          :kind "Deployment",
          :metadata
          {:labels {:app "jitsi"},
           :name "jitsi"
           :namespace "jitsi"},
          :spec
          {:strategy {:type "Recreate"},
           :selector {:matchLabels {:app "jitsi"}},
           :template
           {:metadata {:labels {:app "jitsi"}},
            :spec
            {:containers
             [{:name "jicofo",
               :image "jitsi/jicofo:stable-9646",
               :imagePullPolicy "IfNotPresent",
               :env
               [{:name "XMPP_SERVER", :value "localhost"}
                {:name "JICOFO_COMPONENT_SECRET",
                 :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_COMPONENT_SECRET"}}}
                {:name "JICOFO_AUTH_USER", :value "focus"}
                {:name "JICOFO_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_AUTH_PASSWORD"}}}
                {:name "TZ", :value "Europe/Berlin"}]}
              {:name "prosody",
               :image "jitsi/prosody:stable-9646",
               :imagePullPolicy "IfNotPresent",
               :env
               [{:name "PUBLIC_URL", :value "xy.xy.xy"}
                {:name "XMPP_SERVER", :value "localhost"}
                {:name "JICOFO_COMPONENT_SECRET",
                 :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_COMPONENT_SECRET"}}}
                {:name "JVB_AUTH_USER", :value "jvb"}
                {:name "JVB_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JVB_AUTH_PASSWORD"}}}
                {:name "JICOFO_AUTH_USER", :value "focus"}
                {:name "JICOFO_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_AUTH_PASSWORD"}}}
                {:name "TZ", :value "Europe/Berlin"}
                {:name "JVB_TCP_HARVESTER_DISABLED", :value "true"}]}
              {:name "web",
               :image "domaindrivenarchitecture/c4k-jitsi-web",
               :imagePullPolicy "IfNotPresent",
               :env
               [{:name "PUBLIC_URL", :value "xy.xy.xy"}
                {:name "XMPP_SERVER", :value "localhost"}
                {:name "XMPP_BOSH_URL_BASE", :value "http://127.0.0.1:5280"}
                {:name "JICOFO_AUTH_USER", :value "focus"}
                {:name "TZ", :value "Europe/Berlin"}
                {:name "JVB_TCP_HARVESTER_DISABLED", :value "true"}
                {:name "DEFAULT_LANGUAGE", :value "de"}
                {:name "RESOLUTION", :value "480"}
                {:name "RESOLUTION_MIN", :value "240"}
                {:name "RESOLUTION_WIDTH", :value "853"}
                {:name "RESOLUTION_WIDTH_MIN", :value "427"}
                {:name "DISABLE_AUDIO_LEVELS", :value "true"}
                {:name "ETHERPAD_PUBLIC_URL", :value "https://etherpad.xy.xy.xy/p/"}
                {:name "WHITEBOARD_ENABLED", :value "true"}
                {:name "WHITEBOARD_COLLAB_SERVER_PUBLIC_URL", :value "https://excalidraw-backend.xy.xy.xy"}
                {:name "COLIBRI_WEBSOCKET_REGEX", :value "127.0.0.1"}]}
              {:name "jvb",
               :image "jitsi/jvb:stable-9646",
               :imagePullPolicy "IfNotPresent",
               :env
               [{:name "PUBLIC_URL", :value "xy.xy.xy"}
                {:name "XMPP_SERVER", :value "localhost"}
                {:name "DOCKER_HOST_ADDRESS", :value "xy.xy.xy"}
                {:name "JICOFO_AUTH_USER", :value "focus"}
                {:name "JVB_TCP_HARVESTER_DISABLED", :value "true"}
                {:name "JVB_AUTH_USER", :value "jvb"}
                {:name "JVB_PORT", :value "30300"}
                {:name "JVB_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JVB_AUTH_PASSWORD"}}}
                {:name "JICOFO_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_AUTH_PASSWORD"}}}
                {:name "TZ", :value "Europe/Berlin"}]}
              {:name "etherpad",
               :image "etherpad/etherpad:2",
               :env
               [{:name "XMPP_SERVER", :value "localhost"}
                {:name "JICOFO_COMPONENT_SECRET",
                 :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_COMPONENT_SECRET"}}}
                {:name "JICOFO_AUTH_USER", :value "focus"}
                {:name "JICOFO_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_AUTH_PASSWORD"}}}
                {:name "TZ", :value "Europe/Berlin"}]}]}}}}
         (cut/generate-deployment {:fqdn "xy.xy.xy"
                                   :namespace "jitsi"}))))

(deftest should-generate-secret
  (is (= {:apiVersion "v1",
          :kind "Secret",
          :metadata 
          {:name "jitsi-config"
           :namespace "jitsi"},
          :type "Opaque",
          :data
          {:JVB_AUTH_PASSWORD "anZiLWF1dGg=",
           :JICOFO_AUTH_PASSWORD "amljb2ZvLWF1dGg=",
           :JICOFO_COMPONENT_SECRET "amljb2ZvLWNvbXA="}}
         (cut/generate-secret-jitsi 
          {:fqdn "xy.xy.xy"
           :namespace "jitsi"}
          {:jvb-auth-password "jvb-auth"
           :jicofo-auth-password "jicofo-auth"
           :jicofo-component-secret "jicofo-comp"}))))
                                                        
  (deftest should-generate-ingress-web
    (is (= [{:apiVersion "cert-manager.io/v1",
            :kind "Certificate",
            :metadata
            {:name "web",
             :labels {:app.kubernetes.part-of "web"},
             :namespace "jitsi"},
            :spec
            {:secretName "web",
             :commonName "xy.xy.xy",
             :duration "2160h",
             :renewBefore "720h",
             :dnsNames ["xy.xy.xy"],
             :issuerRef {:name "staging", :kind "ClusterIssuer"}}}
           {:apiVersion "networking.k8s.io/v1",
            :kind "Ingress",
            :metadata
            {:namespace "jitsi",
             :annotations
             {:traefik.ingress.kubernetes.io/router.entrypoints "web, websecure",
              :traefik.ingress.kubernetes.io/router.middlewares
              "default-redirect-https@kubernetescrd",
              :metallb.universe.tf/address-pool "public"},
             :name "web",
             :labels {:app.kubernetes.part-of "web"}},
            :spec
            {:tls [{:hosts ["xy.xy.xy"], :secretName "web"}],
             :rules
             [{:host "xy.xy.xy",
               :http
               {:paths
                [{:pathType "Prefix",
                  :path "/",
                  :backend {:service {:name "web", :port {:number 80}}}}]}}]}}]
           (cut/generate-ingress-web
            {:fqdn "xy.xy.xy"
             :namespace "jitsi"}))))

(deftest should-generate-ingress-modelector
  (is (= [{:apiVersion "cert-manager.io/v1",
           :kind "Certificate",
           :metadata
           {:name "modelector",
            :labels {:app.kubernetes.part-of "modelector"},
            :namespace "jitsi"},
           :spec
           {:secretName "modelector",
            :commonName "modelector.xy.xy",
            :duration "2160h",
            :renewBefore "720h",
            :dnsNames ["modelector.xy.xy"],
            :issuerRef {:name "staging", :kind "ClusterIssuer"}}}
          {:apiVersion "networking.k8s.io/v1",
           :kind "Ingress",
           :metadata
           {:namespace "jitsi",
            :annotations
            {:traefik.ingress.kubernetes.io/router.entrypoints "web, websecure",
             :traefik.ingress.kubernetes.io/router.middlewares
             "default-redirect-https@kubernetescrd",
             :metallb.universe.tf/address-pool "public"},
            :name "modelector",
            :labels {:app.kubernetes.part-of "modelector"}},
           :spec
           {:tls [{:hosts ["modelector.xy.xy"], :secretName "modelector"}],
            :rules
            [{:host "modelector.xy.xy",
              :http
              {:paths
               [{:pathType "Prefix",
                 :path "/",
                 :backend {:service {:name "modelector", :port {:number 80}}}}]}}]}}]
         (cut/generate-ingress-modelector
          {:fqdn "xy.xy"
           :namespace "jitsi"}))))

(deftest should-generate-jvb-service
  (is (= {:apiVersion "v1",
          :kind "Service",
          :metadata
          {:labels {:service "jvb"},
           :annotations
           #:metallb.universe.tf{:allow-shared-ip "shared-ip-service-group",
                                 :address-pool "public"},
           :name "jvb-udp"
           :namespace "jitsi"},
          :spec
          {:type "LoadBalancer",
           :ports
           [{:port 30300, :protocol "UDP", :targetPort 30300, :nodePort 30300}],
           :selector {:app "jitsi"}}}
         (cut/generate-jvb-service
          {:fqdn "xy.xy.xy"
           :namespace "jitsi"}))))

(deftest should-generate-web-service
  (is (= {:apiVersion "v1",
          :kind "Service",
          :metadata {:labels {:service "web"}, :name "web", :namespace "jitsi"},
          :spec
          {:ports
           [{:name "http", :port 80, :targetPort 80}
            {:name "https", :port 443, :targetPort 443}],
           :selector {:app "jitsi"}}}
         (cut/generate-web-service
          {:fqdn "xy.xy.xy"
           :namespace "jitsi"}))))

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
           :TZ "Europe/Amsterdam"}}
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