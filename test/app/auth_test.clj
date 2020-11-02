(ns app.auth-test
  (:require [clojure.test :refer :all]
            [app.auth :as auth]))

(defn create-user-helper [db username password]
  (try
    (auth/create-user db username password)
    (catch Exception _ nil)))


(deftest create-user-with-invalid-password-test
  (testing "Should not allow to create a user with "

    (testing "password less than 8 characters"
      (is (nil? (create-user-helper auth/user-database "user" "Abc123$"))))

    (testing "password containing no numbers"
      (is (nil? (create-user-helper auth/user-database "user" "Abcdefgh$"))))

    (testing "password containing no small case letter"
      (is (nil? (create-user-helper auth/user-database "user" "ABCD1234$"))))

    (testing "password containing no upper case letter"
      (is (nil? (create-user-helper auth/user-database "user" "abcd1234$"))))

    (testing "password containing no special character"
      (is (nil? (create-user-helper auth/user-database "user" "Abcd1234"))))

    (testing "password having more than 20 characters"
      (is (nil? (create-user-helper auth/user-database "user" "Abcdefghij1234567890$"))))))


(deftest create-user-with-invalid-username-test
  (testing "Should not allow to create a user with "

    (testing "username less than 8 characters"
      (is (nil? (create-user-helper auth/user-database "user" "Abcd1234$"))))

    (testing "username containing more than 20 characters"
      (is (nil? (create-user-helper auth/user-database "Username123_1234567890" "Abcd1234$"))))

    (testing "username starting with a non-alphabetic character"
      (is (nil? (create-user-helper auth/user-database "1Username" "abcd1234$"))))

    (testing "username containing special character other than '_' and '.' "
      (is (nil? (create-user-helper auth/user-database "username#" "Abcd1234$"))))))


(deftest create-user-with-duplicate-user-name-test

  (testing "Creating a new user with already existing username is not allowed"
    (is (nil? (create-user-helper auth/user-database "defaultuser" "Newpassword@123"))))

  (testing "Creating two users with same username is not allowed"
    (is (nil? (if (create-user-helper auth/user-database "newusername" "Password123!")
                (create-user-helper auth/user-database "newusername" "Password123!") nil)))))


(deftest authenticate-user-test
  (testing "Authenticating user with correct password should succeed"
    (is (not= nil (try
                    (auth/authenticate-user @auth/user-database "defaultuser" "password@123")
                    (catch Exception _ nil)))))

  (testing "Authenticating a user with wrong password is not allowed"
    (is (nil? (try
                (auth/authenticate-user @auth/user-database "defaultuser" "Password")
                (catch Exception _ nil)))))

  (testing "Authenticating un-registered user is not allowed"
    (is (nil? (try
                (auth/authenticate-user @auth/user-database "nonuser" "Password")
                (catch Exception _ nil))))))


