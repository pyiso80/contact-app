(ns kit.contact-app.web.controllers.contact-app
  (:require [clojure.tools.logging :as log]
            [kit.contact-app.web.pages.layout :as layout]
            [ring.util.http-response :as http-response]
            [ring.util.response :as response]))

(defn paginate [total-records per-page]
  (let [total-pages (Math/ceil (/ total-records per-page))]
    (vec (range 1 (inc total-pages)))))

(defn contacts
  [{:keys [query-fn]} {{:strs [text sort-col order limit page]
                        :or   {text "" sort-col "id" order "asc" limit "10" page "1"}} :query-params :as req}]
  (let [cols #{"id" "first" "last" "phone" "email" "created_at"}
        the-limit (some-> limit Long/parseLong)
        the-page (some-> page Long/parseLong)
        the-offset (* (- the-page 1) the-limit)
        the-order (if (= order "asc") "asc" "desc")
        the-sort-col (if (contains? cols sort-col) sort-col "id")
        total-count (if (empty? text)
                      (query-fn :count-all-contacts {})
                      (query-fn :count-found-contacts
                                {:text    text
                                 :sort-by the-sort-col
                                 :order   the-order
                                 :limit   the-limit
                                 :offset  the-offset}))
        result (if (empty? text)
                 (query-fn :get-all-contacts
                           {:limit  the-limit
                            :offset the-offset})
                 (query-fn :find-contacts
                           {:text    text
                            :sort-by the-sort-col
                            :order   the-order
                            :limit   the-limit
                            :offset  the-offset}))]
    (layout/render req "index.html"
                   (merge {:contacts result}
                          total-count
                          {:page-nums (paginate (:total_count total-count) the-limit)}
                          {:text text}))))

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
                         (assoc-in [:errors :email] "Email is required"))]
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
  (-> (response/redirect "/contacts" :see-other)
      (assoc-in [:flash :message] "Deleted Contact!")))

(defn contacts-validate-email [{:keys [query-fn]} {{:strs [email]} :query-params :as req}]
  (let [match (query-fn :find-email {:email email})
        msg (if (seq match) "Email must be unique." "")]
    (-> (response/response msg)
        (response/status 200)
        (response/content-type "text/plain"))))
