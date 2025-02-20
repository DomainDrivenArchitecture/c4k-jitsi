(ns dda.c4k-jitsi.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.browser :as br]
   [dda.c4k-jitsi.core :as core]))

(defn generate-content []
  (cm/concat-vec
    [(assoc
      (br/generate-needs-validation) :content
      (cm/concat-vec
       (br/generate-group
        "config"
        (br/generate-text-area
         "config" "Your config.edn:"
         "{:fqdn \"jitsi.your.domain\"
 :mon-cfg {:cluster-name \"jitsi\"
                 :cluster-stage \"test\"
                 :grafana-cloud-url \"https://prometheus-prod-01-eu-west-0.grafana.net/api/prom/push\"}}"
         "5"))
       (br/generate-group
        "auth"
        (br/generate-text-area "auth" "Your auth.edn:" "{:jvb-auth-password \"jitsi\"
 :jicofo-auth-password \"jicofo-password\"
 :jicofo-component-secret \"jicofo-component-secrect\"
 :mon-auth {:grafana-cloud-user \"your-user-id\"
            :grafana-cloud-password \"your-cloud-password\"}}}"
        "6"))
       [(br/generate-br)]
       (br/generate-button "generate-button" "Generate c4k yaml")))]
    (br/generate-output "c4k-jitsi-output" "Your c4k deployment.yaml:" "25")))

(defn generate-content-div
  []
  {:type :element
   :tag :div
   :content
   (generate-content)})

(defn validate-all! []
  (br/validate! "config" core/config? :deserializer edn/read-string)
  (br/validate! "auth" core/auth? :deserializer edn/read-string)
  (br/set-form-validated!))

(defn add-validate-listener [name]
  (-> (br/get-element-by-id name)
      (.addEventListener "blur" #(do (validate-all!)))))

(defn init []
  (br/append-hickory (generate-content-div))
  (let [config-only false
        auth-only false]
    (-> js/document
        (.getElementById "generate-button")
        (.addEventListener "click"
                           #(do (validate-all!)
                                (-> (cm/generate-cm
                                     (br/get-content-from-element "config" :deserializer edn/read-string)
                                     (br/get-content-from-element "auth" :deserializer edn/read-string)
                                     core/config-defaults
                                     core/config-objects
                                     core/auth-objects
                                     config-only
                                     auth-only)
                                    (br/set-output!)))))
    (add-validate-listener "config")
    (add-validate-listener "auth")))