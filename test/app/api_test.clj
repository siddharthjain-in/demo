(ns app.api-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [app.server :as server]))


(deftest create-user-handler-test
  (testing "Creating a new user is allowed"
    (is (= ((server/app (-> (mock/request :post "/user")
                            (mock/json-body
                              {:username "newusername1"
                               :password "Password123#"}))) :status)
           200)))

  (testing "Creating a duplicate user is not allowed"
    (is (= ((server/app (-> (mock/request :post "/user")
                            (mock/json-body
                              {:username "newusername1"
                               :password "Password@123#"}))) :status)
           400))))



(deftest authenticate-user-handler-test
  (testing "Authenticating a user with correct password is allowed"
    (is (= ((server/app (-> (mock/request :get "/user")
                            (mock/query-string
                              {:username "defaultuser"
                               :password "password@123"}))) :status)
           200)))

  (testing "Authenticating a user with an incorrect password is not allowed"
    (is (= ((server/app (-> (mock/request :get "/user")
                            (mock/query-string
                              {:username "defaultuser"
                               :password "password"}))) :status)
           401)))

  (testing "Authenticating a non registered user is not allowed"
    (is (= ((server/app (-> (mock/request :get "/user")
                            (mock/query-string
                              {:username "nonuser123"
                               :password "password@123"}))) :status)
           401))))

