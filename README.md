# spring-auth
Spring boot template with facebook OAuth login (manual flow)

This project template implements a facebook login flow using based on 

https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow

using OAuth and not the Facebook javascript API (login button).

There are 3 application config parameters necessary to run the app:

* REDIRECT_URL: The url for the login page
* APP_ID: Your fb app id
* APP_SECRET: Your fb app secret


Stack: 
* Spring Boot
* jQuery (just proof-of-concept)


Login flow simplified:
1. Login using dialog
2. Response from dialog contains code and state (csrf)
3. GET access_token from client_secret,client_id and redirect_url
4. GET app access_token from client_id and client_secret
5. GET inspect access_token from access_token and app_id
6. Verify inspect access_token against client_id and validity

CSRF verification is not added.