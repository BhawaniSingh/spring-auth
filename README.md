# spring-auth
Spring boot template with facebook OAuth login (manual flow)

This project template implements a facebook login flow using based on https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow
using OAuth and not the Facebook javascript API (login button).

There are 3 application config parameters necessary to run the app:
REDIRECT_URL: The url for the login page
APP_ID: Your fb app id
APP_SECRET: Your fb app secret


Stack: 
* Spring Boot
* jQuery (just proof-of-concept)
