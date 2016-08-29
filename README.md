# Secure the API with OAuth 2.0 and authentication Service

*This project is part of the 'IBM Cloud Native Reference Architecture' suite, available at
https://github.com/ibm-cloud-architecture/refarch-cloudnative*

This project provides the artifact to authenticate the API user as well as enable OAuth 2.0 authorization for the Social Review API. It uses IBM API Connect as OAuth provider and uses a mock authentication services. The project contains the following components:

 - Mock Authentication Service implemented as Cloud Foundry Node.js application
 - Custom OAuth Login form and authorization grant form (under the `authentication-app/public` folder)

The authentication application is managed under the `authentication-app` folder.  It uses Node.js basic-auth module to implement the security authentication function. Several mock username/password identities are provided inside the application.


The OAuth provider API definition is actually defined in the `refarch-cloudnative-bff-socialreview` project under the `socialreview\definitions` folder. In the sample scenario, only the SocialReview API will be protected by the OAuth authorization thus we grouped the provider definition into the same socialreview project so that it can be packaged and deployed as a single unit.

The application uses API Connect OAuth 2.0 provider Implicit grant type. For detail of how API Connect supports OAuth 2.0, please reference the IBM Redbook [Getting Started with IBM API Connect: Scenarios Guide](https://www.redbooks.ibm.com/redbooks.nsf/RedpieceAbstracts/redp5350.html?Open)

## Deploy the Mock Authentication Service:

(Optional) In the sample application, the API Connect OAuth provider relies on a dummy authenticating application to validate user credentials. We have deployed the authentication application and configured the OAuth provider already (http://case-authenticate-app.mybluemix.net/). If you would like to deploy your own authentication services follow this section.

You need to have Bluemix command line (bx or cf) installed, as well as Node.js runtime in your development environment.

- Configure the application

  This need to change the Cloud Foundry application route for your own authentication service. Edit the `authentication-app/manefest.yml` file to update the name and host fields:

  ```yml
  applications:
   - path: .
     memory: 512M
     instances: 1
     domain: mybluemix.net
     name: case-authenticate-app
     host: case-authenticate-app
     disk_quota: 1024M
  ```

  Replace the `case-authenticate-app` with your own application host name. For example, `john-test-authentication-app`.

- Deploy the application:

  `$ cd authentication-app`  
  `$ cf push`   


## Validate the Mock Authentication service and

You can validate the mock authentication service at:

 [http://case-authenticate-app.mybluemix.net/authenticate](http://case-authenticate-app.mybluemix.net/authenticate)

   Enter the username password as `foo` and `bar`, you should see response returned as `OK` (HTTP status code 200).

You can validate the OAuth login and grant forms at:

  [http://case-authenticate-app.mybluemix.net/login.html](http://case-authenticate-app.mybluemix.net/login.html)  
  [http://case-authenticate-app.mybluemix.net/grant.html](http://case-authenticate-app.mybluemix.net/grant.html)  
