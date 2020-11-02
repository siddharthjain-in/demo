(ns app.auth
  (:require [clojure.string :as str]))

; an in memory database of registered users
(defonce user-database (atom {"defaultuser" {:id       "defaultuser"
                                             :password "password@123"
                                             :role     :user}}))


(defn- add-user-to-database
  "Add user to database if username does not already exist"
  [db-atom username password role]
  (-> db-atom
      ; put the user in the database
      (swap! assoc (str/lower-case username) {:id username :role role :password password})
      ; select the user in the database
      (get username)
      ; strip their password
      (dissoc :password)))


(defn- get-user
  "Get user  from the database"
  [db username]
  (get db (str/lower-case username)))


(defn username-rules
  "Given a username, return a set of keywords for any rules that are not satisfied"
  [username]
  (cond-> #{}
          ; username must be at least 8 characters long
          (< (count username) 8)
          (conj :username.error/too-short)

          ; username must not be more than 20 characters long
          (> (count username) 20)
          (conj :username.error/too-long)

          ; username must begin with a letter
          (empty? (re-matches #"[a-zA-Z](.*)" username))
          (conj :username.error/invalid-initial-character)

          ; username contains only alphanumeric, underscore or dot characters
          (empty? (re-matches #".*[a-zA-Z0-1_.]" username))
          (conj :username.error/invalid-character)))


(defn password-rules
  "Given a password, return a set of keywords for any rules that are not satisfied"
  [password]
  (cond-> #{}
          ; password must be at least 8 characters long
          (< (count password) 8)
          (conj :password.error/too-short)

          ; password must not be more than 20 characters long
          (> (count password) 20)
          (conj :password.error/too-long)

          ; password must contain a special character
          (empty? (re-find #"[!@#$%^&*(),.?\":{}|<>]" password))
          (conj :password.error/missing-special-character)

          ; password contains at least one lower case letter
          (empty? (re-find #".*[a-z]" password))
          (conj :password.error/missing-lowercase)

          ; password contains at least one upper case letter
          (empty? (re-find #".*[A-Z]" password))
          (conj :password.error/missing-uppercase)

          ; password contains at least one numeric digit
          (empty? (re-find #".*[0-9]" password))
          (conj :password.error/missing-digit)))


(defn authenticate-user
  "Returns a user map when a username and password are correct, or nil when incorrect"
  [db username password]
  (let [user (get-user db username) known-password (:password user)]
    (if (and (not= user nil) (= password known-password))
      (dissoc user :password)
      (throw (ex-info "Invalid username or password"
                      {:reason :login.error/invalid-credentials})))))


(defn create-user
  "Creates a user by adding them to the database, and returns the user's details except for their password"
  ([db-atom username password]
   ; recur with a default role of :user
   (create-user db-atom username password :user))
  ([db-atom username password role]
     ; if there are any password violations
     (when-let [password-violations (not-empty (password-rules password))]
       ; then throw an exception with the violation codes
       (throw (ex-info "Password does not meet criteria"
                       {:reason     :create-user.error/password-violations
                        :violations password-violations})))

     ; if there are any username violations
     (when-let [username-violations (not-empty (username-rules username))]
       ; then throw an exception with the violation codes
       (throw (ex-info "Username does not meet criteria"
                       {:reason     :create-user.error/username-violations
                        :violations username-violations})))

     ; otherwise check if the user exists
     (if (get-user @db-atom username)
       ; and if they do then return an error code
       (throw (ex-info "User already exists"
                       {:reason :create-user.error/already-exists}))

       ; otherwise add user to database and return a success message with the user's details
       (add-user-to-database db-atom username password role))))
