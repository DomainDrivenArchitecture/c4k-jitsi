(ns dda.c4k-jitsi.jitsi
  (:require
   [clojure.spec.alpha :as s]
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.base64 :as b64]
   [dda.c4k-common.predicate :as cp]
   #?(:cljs [dda.c4k-common.macros :refer-macros [inline-resources]])))

(s/def ::fqdn cp/fqdn-string?)
(s/def ::issuer cp/letsencrypt-issuer?)
(s/def ::namespace string?)
(s/def ::jvb-auth-password cp/bash-env-string?)
(s/def ::jicofo-auth-password cp/bash-env-string?)
(s/def ::jicofo-component-secret cp/bash-env-string?)

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer
                              ::namespace]))

(def auth? (s/keys :req-un [::jvb-auth-password
                            ::jicofo-auth-password
                            ::jicofo-component-secret]))
#?(:cljs
   (defmethod yaml/load-resource :jitsi [resource-name]
     (get (inline-resources "jitsi") resource-name)))

(defn- load-and-adjust-namespace
  [file namespace]
  (->
   (yaml/load-as-edn file)
   (cm/replace-all-matching "NAMESPACE" namespace)))

(defn-spec prosody-config cp/map-or-seq?
  [config config?]
  (let [{:keys [fqdn namespace]} config]
    [(load-and-adjust-namespace "jitsi/prosody-config-serviceaccount.yaml" namespace)
     (->
      (load-and-adjust-namespace "jitsi/prosody-config-common-cm.yaml" namespace)
      (cm/replace-all-matching "JITSI_FQDN" fqdn))
     (load-and-adjust-namespace "jitsi/prosody-config-default-cm.yaml" namespace)
     (load-and-adjust-namespace "jitsi/prosody-config-envs-cm.yaml" namespace)
     (load-and-adjust-namespace "jitsi/prosody-config-init-cm.yaml" namespace)
     (load-and-adjust-namespace "jitsi/prosody-config-stateful-set.yaml" namespace)
     (load-and-adjust-namespace "jitsi/prosody-config-service.yaml" namespace)
     (load-and-adjust-namespace "jitsi/prosody-config-test-deployment.yaml" namespace)]))

(defn-spec prosody-auth cp/map-or-seq?
  [config config?
   auth auth?]
  (let [{:keys [namespace]} config
        {:keys [jvb-auth-password jicofo-auth-password jicofo-component-secret]} auth]
  [(load-and-adjust-namespace "jitsi/prosody-auth-secret.yaml" namespace)
   (load-and-adjust-namespace "jitsi/prosody-auth-jibri-secret.yaml" namespace)
   (->
    (load-and-adjust-namespace "jitsi/prosody-auth-jicofo-secret.yaml" namespace)
    (cm/replace-key-value :JICOFO_AUTH_PASSWORD (b64/encode jicofo-auth-password))
    (cm/replace-key-value :JICOFO_COMPONENT_SECRET (b64/encode jicofo-component-secret)))
   (load-and-adjust-namespace "jitsi/prosody-auth-jigasi-secret.yaml" namespace)
   (-> 
    (load-and-adjust-namespace "jitsi/prosody-auth-jvb-secret.yaml" namespace)
    (cm/replace-key-value :JVB_AUTH_PASSWORD (b64/encode jvb-auth-password)))]))

(defn-spec jitsi-config cp/map-or-seq?
  [config config?]
  (let [{:keys [fqdn namespace]} config]
    [(load-and-adjust-namespace "jitsi/jitsi-config-serviceaccount.yaml" namespace)]))

(defn-spec jicofo-config cp/map-or-seq?
  [config config?]
  (let [{:keys [namespace]} config]
    [(load-and-adjust-namespace "jitsi/jicofo-config-defaults-cm.yaml" namespace)
     (->
      (load-and-adjust-namespace "jitsi/jicofo-config-envs-cm.yaml" namespace)
      (cm/replace-key-value :XMPP_SERVER (str "prosody." namespace ".svc.cluster.local")))
     (load-and-adjust-namespace "jitsi/jicofo-config-init-cm.yaml" namespace)
     (load-and-adjust-namespace "jitsi/jicofo-config-deployment.yaml" namespace)]))

(defn-spec web-config cp/map-or-seq?
  [config config?]
  (let [{:keys [fqdn namespace]} config]
    [(load-and-adjust-namespace "jitsi/web-config-conffiles-cm.yaml" namespace)
     (load-and-adjust-namespace "jitsi/web-config-init-cm.yaml" namespace)
     (-> 
      (load-and-adjust-namespace "jitsi/web-config-envs-cm.yaml" namespace)
      (cm/replace-key-value :XMPP_BOSH_URL_BASE (str "http://prosody." namespace ".svc.cluster.local:5280"))
      (cm/replace-key-value :ETHERPAD_PUBLIC_URL (str "https://etherpad." fqdn "/p/"))
      (cm/replace-key-value :WHITEBOARD_COLLAB_SERVER_PUBLIC_URL (str "https://excalidraw." fqdn))
      )
     (load-and-adjust-namespace "jitsi/web-config-service.yaml" namespace)
     (load-and-adjust-namespace "jitsi/web-config-deployment.yaml" namespace)
     (load-and-adjust-namespace "jitsi/web-config-test-deployment.yaml" namespace)]))

(defn-spec jvb-config cp/map-or-seq?
  [config config?]
  (let [{:keys [fqdn namespace]} config]
    [(-> 
      (load-and-adjust-namespace "jitsi/jvb-config-envs-cm.yaml" namespace)
      (cm/replace-key-value :XMPP_SERVER (str "prosody." namespace ".svc.cluster.local")))
     (load-and-adjust-namespace "jitsi/jvb-config-service.yaml" namespace)
     (->
      (load-and-adjust-namespace "jitsi/jvb-config-deployment.yaml" namespace)
      (cm/replace-all-matching "REPLACE_JITSI_FQDN" fqdn)
      )]))

(defn-spec jibri-config cp/map-or-seq?
  [config config?]
  (let [{:keys [fqdn namespace]} config]
    [(load-and-adjust-namespace "jitsi/jitsi-config-serviceaccount.yaml" namespace)
     (load-and-adjust-namespace "jitsi/jibri-config-default-cm.yaml" namespace)
     (->
      (load-and-adjust-namespace "jitsi/jibri-config-envs.yaml" namespace)
      (cm/replace-key-value :XMPP_SERVER (str "prosody." namespace ".svc.cluster.local")))
     (load-and-adjust-namespace "jitsi/jibri-config-init-cm.yaml" namespace)
     (load-and-adjust-namespace "jitsi/jibri-config-service.yaml" namespace)
     (load-and-adjust-namespace "jitsi/jibri-config-deployment.yaml" namespace)]))

(defn-spec etherpad-config cp/map-or-seq?
  [config config?]
  (let [{:keys [namespace]} config]
    [(load-and-adjust-namespace "jitsi/etherpad-config-service.yaml" namespace)
     (load-and-adjust-namespace "jitsi/etherpad-config-deployment.yaml" namespace)]))

(defn-spec excalidraw-config cp/map-or-seq?
  [config config?]
  (let [{:keys [namespace]} config]
    [(load-and-adjust-namespace "jitsi/excalidraw-config-service.yaml" namespace)
     (load-and-adjust-namespace "jitsi/excalidraw-config-deployment.yaml" namespace)]))

(defn-spec moderator-elector-config cp/map-or-seq?
  [config config?]
  (let [{:keys [namespace]} config]
    [(load-and-adjust-namespace "jitsi/modelector-config-service.yaml" namespace)
     (load-and-adjust-namespace "jitsi/modelector-config-deployment.yaml" namespace)]))
