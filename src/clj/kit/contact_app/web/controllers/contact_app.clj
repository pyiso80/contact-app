(ns kit.contact-app.web.controllers.contact-app
  (:require [clojure.tools.logging :as log]
            [kit.contact-app.web.pages.layout :as layout]
            [ring.util.http-response :as http-response]))


(defn contacts
  [{:keys [query-fn]} {{:strs [q]} :query-params :as req}]
  (let [result (if (empty? q)
                 (query-fn :get-all-contacts {:limit 10 :offset 0})
                 (query-fn :find-contacts {:text q}))]
    (layout/render req "index.html" {:contacts result})))

(defn contacts-new-get
  [_ {:keys [flash] :as req}]
  (log/debug "getting new contact form...")
  (layout/render req "new.html" {:contact {:first "" :last "" :phone "" :email ""} :errors (:errors flash)}))

(defn contacts-new-post
  [{:keys [query-fn]} {{:strs [first last phone email]} :form-params :as req}]
  (log/debug "saving contact: " [first last phone email])
  (try
    (if (or (empty? first) (empty? last) (empty? phone) (empty? email))
      (let [data (cond-> {:contact {:first first :last last :phone phone :email email}}
                         (empty? first)
                         (assoc-in [:errors :first] "First name is required")
                         (empty? last)
                         (assoc-in [:errors :last] "Last name is required")
                         (empty? phone)
                         (assoc-in [:errors :phone] "Phone number is required")
                         (empty? email)
                         (assoc-in [:errors :email] "Email is required"))
            ]
        (do
          (println data)
          (layout/render req "new.html" data)))
      (do
        (query-fn :save-contact! {:first first :last last :phone phone :email email})
        (-> (http-response/found "/contacts")
            (assoc-in [:flash :message] "Created New Contact!"))))
    (catch Exception e
      (log/error e "failed to save message!")
      (-> (http-response/found "/")
          (assoc :flash {:errors {:unknown (.getMessage e)}})))))

(defn contacts-new-post3333
  [{:keys [query-fn]} {{:strs [first last phone email]} :form-params :as req}]
  (log/debug "saving contact: " [first last phone email])
  (try
    (if (or (empty? first) (empty? last) (empty? phone) (empty? email))
      (cond-> (http-response/found "/contacts/new")
              (empty? first)
              (assoc-in [:flash :errors :first] "First name is required")
              (empty? last)
              (assoc-in [:flash :errors :last] "Last name is required")
              (empty? phone)
              (assoc-in [:flash :errors :phone] "Phone number is required")
              (empty? email)
              (assoc-in [:flash :errors :email] "Email is required"))
      (do
        (query-fn :save-contact! {:first first :last last :phone phone :email email})
        (-> (http-response/found "/contacts")
            (assoc-in [:flash :message] "Created New Contact!"))))
    (catch Exception e
      (log/error e "failed to save message!")
      (-> (http-response/found "/")
          (assoc :flash {:errors {:unknown (.getMessage e)}})))))

(defn contacts-view [{:keys [query-fn]} {{:keys [id]} :path-params :as req}]
  (log/debug "view contact" id)
  (try
    (let [contact (query-fn :get-contact-by-id {:id (some-> id Long/parseLong)})]
      (log/debug contact)
      (layout/render req "show.html" {:contact contact}))
    (catch Exception e
      (log/error e "failed to retrieve message!"))))

(defn contacts-edit-get [{:keys [query-fn]} {{:keys [id]} :path-params :as req}]
  (log/debug "edit contact get" id)
  (try
    (let [contact (query-fn :get-contact-by-id {:id (some-> id Long/parseLong)})]
      (log/debug contact)
      (layout/render req "edit.html" {:contact contact}))
    (catch Exception e
      (log/error e "failed to retrieve message!"))))

(defn contacts-edit-post
  [{:keys [query-fn]}
   {{:strs [first last phone email]} :form-params
    {:keys [id]}                     :path-params :as req}]
  (try
    (if (or (empty? first) (empty? last) (empty? phone) (empty? email))
      (let [data (cond-> {:contact {:first first :last last :phone phone :email email}}
                         (empty? first)
                         (assoc-in [:errors :first] "First name is required")
                         (empty? last)
                         (assoc-in [:errors :last] "Last name is required")
                         (empty? phone)
                         (assoc-in [:errors :phone] "Phone number is required")
                         (empty? email)
                         (assoc-in [:errors :email] "Email is required"))]
        (do
          (println data)
          (layout/render req "edit.html" data)))
      (do
        (query-fn :update-contact!
                  {:id (some-> id Long/parseLong) :first first :last last :phone phone :email email})
        (-> (http-response/found "/contacts")
            (assoc-in [:flash :message] "Updated Contact!"))))
    (catch Exception e
      (log/error e "failed to retrieve message!"))))

(defn contacts-delete [{:keys [query-fn]} {{:keys [id]} :path-params :as req}]
  (query-fn :delete-contact! {:id (some-> id Long/parseLong)})
  (-> (http-response/found "/contacts")
      (assoc-in [:flash :message] "Deleted Contact!")))