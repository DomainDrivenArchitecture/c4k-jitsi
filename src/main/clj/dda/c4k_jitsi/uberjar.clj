(ns dda.c4k-jitsi.uberjar
  (:gen-class)
  (:require
   [dda.c4k-common.uberjar :as uberjar]
   [dda.c4k-jitsi.core :as core]))

(defn -main [& cmd-args]
  (uberjar/main-cm
   "c4k-jitsi"
   ::core/config
   ::core/auth
   core/config-defaults
   core/config-objects
   core/auth-objects
   cmd-args))
