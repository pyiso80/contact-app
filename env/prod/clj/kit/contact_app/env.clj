(ns kit.contact-app.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[contact-app starting]=-"))
   :start      (fn []
                 (log/info "\n-=[contact-app started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[contact-app has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
