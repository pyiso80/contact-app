(ns kit.contact-app.env
  (:require
    [clojure.tools.logging :as log]
    [kit.contact-app.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[contact-app starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[contact-app started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[contact-app has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
