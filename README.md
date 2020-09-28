# Simple Authentication API
This project provides a simple REST API which does two things:

`POST /user`
Create a user with a `username`, `password`, and an optional `role` and store the user in memory.
For demo purposes, anyone can create a new account with any role, i.e. the API does not have any security.

`GET /user`
Retrieve a user by using their `username` and `password` and return their details

The database already has one existing user, and more can be added using the API
```
username: defaultuser 
password: password@123
role:     user
```

The API throws various exceptions when any one more of the following conditions are met. When an exception is thrown, requests are returned with an HTTP Code 400 or 401 respectively, along with the exception code and details about the violation.

* User already exists when attempting to create a new one
* Password is not at least 8 characters long
* Password does not contain a lower case letter
* Password does not contain an upper case letter
* Password does not contain a special character
* Invalid username and password combination

### Getting Started
Start the server on `localhost:3000`
```bash
$ clj -A:server
2020-09-28 09:27:18.207:INFO:oejs.Server:main: Started @3729ms
Server running on port 3000
```
### Example usage
Authenticate as the existing default user:
```bash 
curl --location --request GET 'localhost:3000/user?username=admin&password=password@123'

{"id":"defaultuser","role":"user"}
```
Create a user
```bash
$ curl --location --request POST 'localhost:3000/user' \
--header 'Content-Type: application/edn' \
--data-raw '{:username "demouser1" :password "abcdEFGH@123"}'

{"id":"demouser1","role":"user"}
```
Authenticate a user
```bash
$ curl --location --request GET 'localhost:3000/user?username=demouser1&password=abcdEFGH@123'

{"id":"demouser1","role":"user"}
```
Attempt to authenticate a user with the wrong password
```bash
$ curl --location --request GET 'localhost:3000/user?username=demouser1&password=forgottenpassword'

{"reason":"login.error/invalid-credentials"}
```
Attempt to create a user with an invalid password
```bash
$ curl --location --request POST 'localhost:3000/user' \
--header 'Content-Type: application/edn' \
--data-raw '{:username "demouser2" :password "hello"}'

{
   "reason":"create-user.error/password-violations",
   "violations":[
      "password.error/missing-uppercase",
      "password.error/too-short",
      "password.error/missing-special-character"
   ]
}
```
Attempt to create a user that already exists
```bash
curl --location --request POST 'localhost:3000/user' \
--header 'Content-Type: application/edn' \
--data-raw '{:username "demouser1" :password "abcdEFGH@123"}'

{"reason":"create-user.error/already-exists"}
```