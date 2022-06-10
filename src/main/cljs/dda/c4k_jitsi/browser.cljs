(ns dda.c4k-jitsi.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-jitsi.core :as core]
   [dda.c4k-jitsi.jitsi :as jitsi]
   [dda.c4k-common.browser :as br]
   [dda.c4k-common.postgres :as pgc]))

(defn generate-content
  []
  (into [] (concat [(assoc (br/generate-needs-validation) :content
                           (into [] (concat (br/generate-input-field "fqdn" "Your fqdn:" "jitsi.prod.meissa-gmbh.de")
                                            (br/generate-input-field "issuer" "(Optional) Your issuer prod/staging:" "")
                                            [(br/generate-br)]
                                            (br/generate-text-area "auth" "Your auth.edn:" "{:jvb-auth-password \"jitsi\"
         :jicofo-auth-password \"jicofo-password\"
         :jicofo-component-secret \"jicofo-component-secrect\"}"
                                                                   "5")
                                            [(br/generate-br)]
                                            (br/generate-button "generate-button" "Generate c4k yaml"))))]
                   (br/generate-output "c4k-jitsi-output" "Your c4k deployment.yaml:" "25"))))

(defn generate-content-div
  []
  {:type :element
   :tag :div
   :content
   (generate-content)})

(defn config-from-document []
  (let [issuer (br/get-content-from-element "issuer" :optional true :deserializer keyword)]
    (merge
     {:fqdn (br/get-content-from-element "fqdn")}     
     (when (some? issuer)
       {:issuer issuer})
     )))

(defn validate-all! []
  (br/validate! "fqdn" ::jitsi/fqdn)  
  (br/validate! "issuer" ::jitsi/issuer :optional true :deserializer keyword)
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
                              (-> (core/generate
                                   (config-from-document)
                                   (br/get-content-from-element "auth" :deserializer edn/read-string))
                                  (br/set-output!)))))
  (add-validate-listener "fqdn")  
  (add-validate-listener "issuer")
  (add-validate-listener "auth"))