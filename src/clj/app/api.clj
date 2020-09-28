(ns app.api
  (:require [reitit.coercion.spec]
            [spec-tools.data-spec :as ds]
            [app.auth :as auth]))


(defn create-user-handler
  "A web handler to create a new user"
  [{{{:keys [username password role]} :body} :parameters}]
  (try
    {:status 200
     :body   (if role
               (auth/create-user auth/user-database username password role)
               (auth/create-user auth/user-database username password))}
    (catch Exception e
      {:status 400
       :body   (ex-data e)})))

(defn authenticate-user-handler
  "A web handler to create a new user"
  [{{{:keys [username password]} :query} :parameters}]
  (try
    {:status 200
     :body   (auth/authenticate-user @auth/user-database username password)}
    (catch Exception e
      {:status 401
       :body   (ex-data e)})))

(def routes
  ["" {:coercion reitit.coercion.spec/coercion}
   ["/user" {:get  {:summary    "Authenticates a user in the database, returning their details"
                    :parameters {:query {:username string?
                                         :password string?}}
                    :handler    authenticate-user-handler}
             :post {:summary    "Creates a new user in the database, returning their details"
                    :parameters {:body {:username      string?
                                        :password      string?
                                        (ds/opt :role) keyword?}}
                    :handler    create-user-handler}}]])

(comment
  (fn [{{:keys [username password]} :parameters :as req}]
    (clojure.pprint/pprint req)
    (try
      {:status 200
       :body   (auth/create-user auth/user-database username password)}
      (catch Exception e {:status 400
                          :body   (ex-data e)}))))