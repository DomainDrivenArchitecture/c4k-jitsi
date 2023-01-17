(ns dda.c4k-jitsi.jitsi-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.test.alpha :as st]
   [dda.c4k-jitsi.jitsi :as cut]))

(st/instrument)

(deftest should-generate-deployment
  (is (= {:apiVersion "apps/v1",
          :kind "Deployment",
          :metadata {:labels {:app "jitsi"}, :name "jitsi"},
          :spec
          {:strategy {:type "Recreate"},
           :selector {:matchLabels {:app "jitsi"}},
           :template
           {:metadata {:labels {:app "jitsi"}},
            :spec
            {:containers
             [{:name "jicofo",
               :image "jitsi/jicofo:stable-7287",
               :imagePullPolicy "IfNotPresent",
               :env
               [{:name "XMPP_SERVER", :value "localhost"}
                {:name "XMPP_DOMAIN", :value "meet.meissa-gmbh"}
                {:name "XMPP_AUTH_DOMAIN", :value "auth.meet.meissa-gmbh"}
                {:name "XMPP_MUC_DOMAIN", :value "muc.meet.meissa-gmbh"}
                {:name "XMPP_INTERNAL_MUC_DOMAIN", :value "internal-muc.meet.meissa-gmbh"}
                {:name "JICOFO_COMPONENT_SECRET",
                 :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_COMPONENT_SECRET"}}}
                {:name "JICOFO_AUTH_USER", :value "focus"}
                {:name "JICOFO_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_AUTH_PASSWORD"}}}
                {:name "TZ", :value "Europe/Berlin"}
                {:name "JVB_BREWERY_MUC", :value "jvbbrewery"}]}
              {:name "prosody",
               :image "jitsi/prosody:stable-7287",
               :imagePullPolicy "IfNotPresent",
               :env
               [{:name "PUBLIC_URL", :value "xy.xy.xy"}
                {:name "XMPP_DOMAIN", :value "meet.meissa-gmbh"}
                {:name "XMPP_AUTH_DOMAIN", :value "auth.meet.meissa-gmbh"}
                {:name "XMPP_MUC_DOMAIN", :value "muc.meet.meissa-gmbh"}
                {:name "XMPP_INTERNAL_MUC_DOMAIN", :value "internal-muc.meet.meissa-gmbh"}
                {:name "JICOFO_COMPONENT_SECRET",
                 :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_COMPONENT_SECRET"}}}
                {:name "JVB_AUTH_USER", :value "jvb"}
                {:name "JVB_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JVB_AUTH_PASSWORD"}}}
                {:name "JICOFO_AUTH_USER", :value "focus"}
                {:name "JICOFO_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_AUTH_PASSWORD"}}}
                {:name "TZ", :value "Europe/Berlin"}
                {:name "JVB_TCP_HARVESTER_DISABLED", :value "true"}]}
              {:name "web",
               :image "domaindrivenarchitecture/c4k-jitsi",
               :imagePullPolicy "IfNotPresent",
               :env
               [{:name "PUBLIC_URL", :value "xy.xy.xy"}
                {:name "XMPP_SERVER", :value "localhost"}
                {:name "JICOFO_AUTH_USER", :value "focus"}
                {:name "XMPP_DOMAIN", :value "meet.meissa-gmbh"}
                {:name "XMPP_AUTH_DOMAIN", :value "auth.meet.meissa-gmbh"}
                {:name "XMPP_INTERNAL_MUC_DOMAIN", :value "internal-muc.meet.meissa-gmbh"}
                {:name "XMPP_BOSH_URL_BASE", :value "http://127.0.0.1:5280"}
                {:name "XMPP_MUC_DOMAIN", :value "muc.meet.meissa-gmbh"}
                {:name "TZ", :value "Europe/Berlin"}
                {:name "JVB_TCP_HARVESTER_DISABLED", :value "true"}
                {:name "DEFAULT_LANGUAGE", :value "de"}
                {:name "RESOLUTION", :value "480"}
                {:name "RESOLUTION_MIN", :value "240"}
                {:name "RESOLUTION_WIDTH", :value "853"}
                {:name "RESOLUTION_WIDTH_MIN", :value "427"}
                {:name "DISABLE_AUDIO_LEVELS", :value "true"}
                {:name "ETHERPAD_PUBLIC_URL", :value "https://etherpad.xy.xy.xy/p/"}]}
              {:name "jvb",
               :image "jitsi/jvb:stable-7287",
               :imagePullPolicy "IfNotPresent",
               :env
               [{:name "PUBLIC_URL", :value "xy.xy.xy"}
                {:name "XMPP_SERVER", :value "localhost"}
                {:name "DOCKER_HOST_ADDRESS", :value "xy.xy.xy"}
                {:name "XMPP_DOMAIN", :value "meet.meissa-gmbh"}
                {:name "XMPP_AUTH_DOMAIN", :value "auth.meet.meissa-gmbh"}
                {:name "XMPP_INTERNAL_MUC_DOMAIN", :value "internal-muc.meet.meissa-gmbh"}
                {:name "JICOFO_AUTH_USER", :value "focus"}
                {:name "JVB_TCP_HARVESTER_DISABLED", :value "true"}
                {:name "JVB_AUTH_USER", :value "jvb"}
                {:name "JVB_PORT", :value "30300"}
                {:name "JVB_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JVB_AUTH_PASSWORD"}}}
                {:name "JICOFO_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_AUTH_PASSWORD"}}}
                {:name "JVB_BREWERY_MUC", :value "jvbbrewery"}
                {:name "TZ", :value "Europe/Berlin"}]}
              {:name "etherpad",
               :image "jitsi/etherpad",
               :env
               [{:name "XMPP_SERVER", :value "localhost"}
                {:name "XMPP_DOMAIN", :value "meet.meissa-gmbh"}
                {:name "XMPP_AUTH_DOMAIN", :value "auth.meet.meissa-gmbh"}
                {:name "JICOFO_COMPONENT_SECRET",
                 :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_COMPONENT_SECRET"}}}
                {:name "JICOFO_AUTH_USER", :value "focus"}
                {:name "JVB_BREWERY_MUC", :value "jvbbrewery"}
                {:name "XMPP_INTERNAL_MUC_DOMAIN", :value "internal-muc.meet.meissa-gmbh"}
                {:name "JICOFO_AUTH_PASSWORD", :valueFrom {:secretKeyRef {:name "jitsi-config", :key "JICOFO_AUTH_PASSWORD"}}}
                {:name "TZ", :value "Europe/Berlin"}]}]}}}}
         (cut/generate-deployment {:fqdn "xy.xy.xy"}))))

(deftest should-generate-secret
  (is (= {:apiVersion "v1",
          :kind "Secret",
          :metadata {:name "jitsi-config"},
          :type "Opaque",
          :data
          {:JVB_AUTH_PASSWORD "anZiLWF1dGg=",
           :JICOFO_AUTH_PASSWORD "amljb2ZvLWF1dGg=",
           :JICOFO_COMPONENT_SECRET "amljb2ZvLWNvbXA="}}
         (cut/generate-secret-jitsi {:jvb-auth-password "jvb-auth"
                                     :jicofo-auth-password "jicofo-auth"
                                     :jicofo-component-secret "jicofo-comp"}))))
