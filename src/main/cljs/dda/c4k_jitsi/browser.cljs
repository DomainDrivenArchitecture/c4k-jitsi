(ns dda.c4k-jitsi.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.browser :as br]
   [dda.c4k-jitsi.core :as core]
   [dda.c4k-jitsi.jitsi :as jitsi]))

(defn generate-content []
  (cm/concat-vec
    [(assoc
      (br/generate-needs-validation) :content
      (cm/concat-vec
       (br/generate-group
        "domain"
        (cm/concat-vec
         (br/generate-input-field "fqdn" "Your fqdn:" "jitsi.prod.meissa-gmbh.de")
         (br/generate-input-field "issuer" "(Optional) Your issuer prod/staging:" "")
         (br/generate-input-field "mon-cluster-name" "(Optional) monitoring cluster name:" "jitsi")
         (br/generate-input-field "mon-cluster-stage" "(Optional) monitoring cluster stage:" "test")
         (br/generate-input-field "mon-cloud-url" "(Optional) grafana cloud url:" "https://prometheus-prod-01-eu-west-0.grafana.net/api/prom/push")
         ))
       (br/generate-group
        "credentials"
        (br/generate-text-area "auth" "Your auth.edn:" "{:jvb-auth-password \"jitsi\"
 :jicofo-auth-password \"jicofo-password\"
 :jicofo-component-secret \"jicofo-component-secrect\"
 :mon-auth {:grafana-cloud-user \"your-user-id\"
            :grafana-cloud-password \"your-cloud-password\"}}}"
        "5"))
       [(br/generate-br)]
       (br/generate-button "generate-button" "Generate c4k yaml")))]
    (br/generate-output "c4k-jitsi-output" "Your c4k deployment.yaml:" "25")))

(defn generate-content-div
  []
  {:type :element
   :tag :div
   :content
   (generate-content)})

(defn config-from-document []
  (let [issuer (br/get-content-from-element "issuer" :optional true)
        mon-cluster-name (br/get-content-from-element "mon-cluster-name" :optional true)
        mon-cluster-stage (br/get-content-from-element "mon-cluster-stage" :optional true :deserializer keyword)
        mon-cloud-url (br/get-content-from-element "mon-cloud-url" :optional true)]
    (merge
     {:fqdn (br/get-content-from-element "fqdn")}     
     (when (some? issuer)
       {:issuer issuer})
     (when (some? mon-cluster-name)
       {:mon-cfg {:cluster-name mon-cluster-name
                  :cluster-stage (keyword mon-cluster-stage)
                  :grafana-cloud-url mon-cloud-url}})
     )))

(defn validate-all! []
  (br/validate! "fqdn" ::jitsi/fqdn)  
  (br/validate! "issuer" ::jitsi/issuer :optional true)
  (br/validate! "mon-cluster-name" ::mon/cluster-name :optional true)
  (br/validate! "mon-cluster-stage" ::mon/cluster-stage :optional true :deserializer keyword)
  (br/validate! "mon-cloud-url" ::mon/grafana-cloud-url :optional true)
  (br/validate! "auth" core/auth? :deserializer edn/read-string)
  (br/set-validated!))

(defn add-validate-listener [name]
  (-> (br/get-element-by-id name)
      (.addEventListener "blur" #(do (validate-all!)))))


(defn init []
  (br/append-hickory (generate-content-div))
  (-> js/document
      (.getElementById "generate-button")
      (.addEventListener "click"
                         #(do (validate-all!)
                              (-> (cm/generate-common
                                   (config-from-document)
                                   (br/get-content-from-element "auth" :deserializer edn/read-string)
                                   {}
                                   core/k8s-objects)
                                  (br/set-output!)))))
  (add-validate-listener "fqdn")  
  (add-validate-listener "issuer")
  (add-validate-listener "auth"))