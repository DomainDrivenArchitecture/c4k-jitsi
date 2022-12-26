(ns dda.c4k-jitsi.uberjar
  (:gen-class)
  (:require
   [dda.c4k-common.uberjar :as uberjar]
   [dda.c4k-jitsi.core :as core]
   [dda.c4k-jitsi.jitsi :as jitsi]))

(defn -main [& cmd-args]
  (uberjar/main-common
   "c4k-jitsi" 
   jitsi/config? 
   jitsi/auth?
   core/config-defaults 
   core/k8s-objects
   cmd-args))
