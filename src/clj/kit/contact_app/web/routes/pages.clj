(ns kit.contact-app.web.routes.pages
  (:require
    [kit.contact-app.web.middleware.exception :as exception]
    [kit.contact-app.web.pages.layout :as layout]
    [integrant.core :as ig]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]

    [kit.contact-app.web.controllers.contact-app :as contact-app]))

(defn wrap-page-defaults []
  (let [error-page (layout/error-page
                     {:status 403
                      :title  "Invalid anti-forgery token"})]
    #(wrap-anti-forgery % {:error-response error-page})))

(defn index [request]
  (layout/render request "index.html"))

;; Routes
(defn page-routes [_opts]
  [["/" {:get index}]
   ["/contacts" {:get (partial contact-app/contacts _opts)}]
   ["/contacts/new"
    {:get  (partial contact-app/contacts-new-get _opts)
     :post (partial contact-app/contacts-new-post _opts)}]
   ["/contacts/validate-email" {:get (partial contact-app/contacts-validate-email _opts)}]
   ["/contacts/:id"
    ["/edit" {:get  (partial contact-app/contacts-edit-get _opts)
              :post (partial contact-app/contacts-edit-post _opts)}]
    ["/delete" {:delete (partial contact-app/contacts-delete _opts)}]
    ["/view" {:get (partial contact-app/contacts-view _opts)}]]])

(def route-data
  {:middleware
   [;; Default middleware for pages
    (wrap-page-defaults)
    ;; query-params & form-params
    parameters/parameters-middleware
    ;; encoding response body
    muuntaja/format-response-middleware
    ;; exception handling
    exception/wrap-exception]})

(derive :reitit.routes/pages :reitit/routes)

(defmethod ig/init-key :reitit.routes/pages
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (layout/init-selmer! opts)
  (fn [] [base-path route-data (page-routes opts)]))

