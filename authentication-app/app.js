/*eslint-env node*/

//------------------------------------------------------------------------------
// node.js starter application for Bluemix
//------------------------------------------------------------------------------

// This application uses express as its web server
// for more info, see: http://expressjs.com
var express = require('express');
var redis = require('redis');
var session = require('express-session');
var RedisStore = require('connect-redis')(session);
var request = require('request');
var Promise = require('promise');
var requestPromise = require('request-promise-json').request;
var querystring = require('querystring');
var uuid = require('uuid');
var util = require('util');
var nodeCache = require('node-cache');
var config = require('config');

// cfenv provides access to your Cloud Foundry environment
// for more info, see: https://www.npmjs.com/package/cfenv
var cfenv = require('cfenv');

// create a new express server
var app = express();

// get the app environment from Cloud Foundry
var appEnv = cfenv.getAppEnv();
var store = null;
if (appEnv.services['compose-for-redis'] != null) {
    var redisCredentials = appEnv.services['compose-for-redis'][0].credentials;   
    var redisClient = redis.createClient(redisCredentials.uri);

    // use redis to store sessions
    store = new RedisStore({
        client: redisClient,
        ttl: 3600
    });
} else {
    // use in-memory nodecache
    var store = new nodeCache({stdTTL: 3600, checkperiod: 600});
}

app.use(session({
  store: store,
  secret: 'ibmApiConnect4Me',
  resave: true,
  saveUninitialized: true,
  cookie: { maxAge: 300000  },
}));


app.use(function(req, res, next) {
    res.locals.session = req.session;
    next();
});

// serve the files out of ./public as our main files
app.use(express.static(__dirname + '/public'));

var oauthConfig = config.get('oauth');

// start server on the specified port and binding host
app.listen(appEnv.port, '0.0.0.0', function() {
    // print a message when the server starts listening
    console.log("server starting on " + appEnv.url);
});

// Test to see if credentials are in the user registry
var lookup = function(name, pass) {
  for (var user of registryObj.users) {
    if ((name == user.name) && (pass == user.pass)) {
      return true;
    }
  }
  return false;
}
var auth = function (req, res, next) {
  // save original url passed to us by APIC
  var originalUrl = req.query['original-url'];
  var appName = req.query['app-name'];
  req.session.originalUrl = originalUrl;
  req.session.appName = appName;

  // Authorized request will already have authContext stored in session
  if (req.session != null && req.session.authContext != null){   
    next();
  } else {
      // Unauthorized requests should be forwarded to the Mobile Client Access authorization endpoint
      // save original url to session before redirect
      req.session.save(function(err) {
          if (err != null) {
              console.log("Error saving session: ", err);
          } else {
              //console.log("auth(): Session saved ", JSON.stringify(req.session));
          }
      });

      // Retrieve Mobile Client Access credentials from VCAP_SERVICES
      var mcaCredentials = appEnv.services.AdvancedMobileAccess[0].credentials;   
      //var authorizationEndpoint = mcaCredentials.authorizationEndpoint;   
      //var clientId = mcaCredentials.clientId;   
      console.log(oauthConfig);
      var authorizationEndpoint = oauthConfig.get('authEndpoint');   
      var clientId = oauthConfig.get('clientId');   
      var redirectUri = oauthConfig.get('redirectUri'); 

      var scope = oauthConfig.get('scope');
      var scopes = null;
      if (scope != null && scope.length > 0) {
          scopes = scope.join("%20");
      }

      // Add the redirect URI of your web applications
      // This must be the same web application redirect URI you've defined in the Mobile Client Access dashboard

      // Create a URI for the authorization endpoint and redirect client
      var authorizationUri = authorizationEndpoint + "?response_type=code";
      authorizationUri += "&client_id=" + clientId;   
      authorizationUri += "&redirect_uri=" + redirectUri;   
      if (scopes != null) {
          authorizationUri += "&scope=" + scopes;
      }

      console.log("auth(): redirecting to: ", authorizationUri);
      res.redirect(authorizationUri);  
  }
};

//function redirectToOriginalURL(req, res) {
function redirectToOriginalURL(functionInput) {
    var req = functionInput.req;
    var res = functionInput.res;

    var username = req.session.username ? req.session.username : uuid.v4();
    var confirmation = req.session.id;

    var originalUrl = req.session.originalUrl;
    var urlQueryStr = {
      'app-name': req.session.appName,
      'username': username,
      'confirmation': req.session.id
    }

    // use session ID as confirmation, and generate a uuid for the username
    //console.log("session id = ", req.session.id);
    req.session.confirmation = req.session.id;
    req.session.username = username;

    // save these in memory store
    //store.set(confirmation, displayName);
    req.session.save(function(err) {
        if (err != null) {
            console.log("Error saving session: ", err);
        } else {
            //console.log("Session saved: ", JSON.stringify(req.session));
        }
    });

    var urlStr = querystring.stringify(urlQueryStr);

    var redirectUrl = originalUrl + "&" + urlStr;
    //console.log("redirectToOriginalURL(): redirecting to: ", redirectUrl);

	res.redirect(redirectUrl);
}

app.get('/validate', function(req, res) {
    /* APIC calls this to validate the username and confirmation code
     * returned in the auth flow, we've stored this in the session so just look it up */

    //console.log("validate(), query: %j", req.query);
    //console.log("validate(), headers: %j", req.headers);

    // TODO validate yep
    //res.sendStatus(200);
    //res.end();
    //return;
   
    // un-base64 the "authorization" header"
    var authHeaderZ = req.headers.authorization;
    // remove the "Basic"
    var authPart = authHeaderZ.split(' ')[1];

    var authHeader = new Buffer(authPart,"base64").toString();
    var username = authHeader.split(':')[0];
    var confirmation = authHeader.split(':')[1];

    //console.log("validate(): auth confirmation=", confirmation);
    //console.log("validate(): auth username=", username);

    store.get(confirmation, function(err, session) {
        if (err != null) {
            console.log("Error loading session: ", err);
            res.sendStatus(403);
            res.end();
        } else {
            console.log("Session loaded: ", session);
            var storedUsername = session.username;
            var storedConfirmation = session.confirmation;

            // get the values from store
            //console.log("validate(): session confirmation = " + storedConfirmation);
            //console.log("validate(): session username = " + storedUsername);
            if (storedUsername != username) {
                // doesn't match, return 403 Forbidden
                res.sendStatus(403);
                res.end();
                return;
            } 

            // Decode identity token and store it as authContext
            var idTokenComponents = session.idToken.split("."); // [header, payload, signature] 
            var id_token_payload = new Buffer(idTokenComponents[1],"base64").toString();
            var idToken = JSON.parse(id_token_payload);

            var cred = 'google/' + idToken.sub + ':' + session.accessToken;
            var encoded = new Buffer(cred).toString('base64');

            // return the google resource name, and Google access Token as metadata
            var headers = {
                'API-Authenticated-Credential': encoded,
            };

            console.log("returning: ", headers);

            res.writeHead(200, headers);
            res.end();
        }
    });

    // destroy the session associated with /validate, it's called by APIC
    req.session.destroy(function(err) {
    });

});

app.get('/authenticate', auth, function(req, res) {
    //console.log("ALREADY AUTHENTICATED! ");
    //console.log("ALREADY AUTHENTICATED, query: %j", req.query);
    //console.log("ALREADY AUTHENTICATED, session: %j", req.session);

    // if we get here, we've already been authenticated before so just redirect to
    // originalURL (should be in the request)
    
    var originalUrl = req.query['original-url'];
    var appName = req.query['app-name'];
    req.session.originalUrl = originalUrl;
    req.session.appName = appName;

    var options = {
        req: req,
        res: res
    }

    redirectToOriginalURL(options);
});


function setGetAccessTokenOptions(req, res) {
//    console.log("setGetAccessTokenOptions");

    // Retrieve Mobile Client Access credentials from VCAP_SERVICES
    var mcaCredentials = appEnv.services.AdvancedMobileAccess[0].credentials; 
    //var tokenEndpoint = mcaCredentials.tokenEndpoint; 
    //var clientId = mcaCredentials.tenantId;
    //var clientSecret = mcaCredentials.secret;
    var tokenEndpoint = oauthConfig.get('tokenEndpoint');
    var clientId = oauthConfig.get('clientId');
    var clientSecret = oauthConfig.get('clientSecret');
    var redirectUri = oauthConfig.get('redirectUri'); 

    // post to the token endpoint with my code (if it exists)
    var grantCode = req.query.code;
    var formData = { 
        grant_type: "authorization_code", 
        client_id: clientId,
        client_secret: clientSecret,
        redirect_uri: redirectUri,
        code: grantCode
    } 

    // encode base64 clientId:clientSecret
    var authStr = new Buffer(util.format("%s:%s", clientId, clientSecret)).toString('base64'); 

    var getAccessTokenOptions = {
        method: 'POST',
        url: tokenEndpoint,
        strictSSL: false,
        headers: {
            // Supply clientId and clientSecret as Basic Http Auth credentials
            "Authorization": util.format("Basic %s", authStr)
        },
        form: formData,
        JSON: false
    }

    return new Promise(function (fulfill) {
        if (!grantCode) {
            console.log("No code found");
            res.on('data', function (chunk) {
                console.log('BODY: ' + chunk);
            });

            // Store accessToken in session in
            req.session.accessToken = req.query.access_token; 
            req.session.authContext = req.query.access_token;
            res.redirect('/')
        }

        // set options
        fulfill({
            req: req,
            res: res,
            getAccessToken_options: getAccessTokenOptions
        });
    });

}

function getAccessToken(function_input) {
//    console.log("getAccessToken");

    var req = function_input.req;
    var res = function_input.res;
    var options = function_input.getAccessToken_options;

    return new Promise(function (fulfill, reject) {
        requestPromise(options)
          .then(function(parsedBody) {
              //console.log("token endpoint returned, %j", parsedBody);
              // TODO: error handling

              // Stash google access_token and id_token in session in base64 format
              req.session.accessToken = parsedBody['access_token'];
              req.session.idToken = parsedBody['id_token']; 
              req.session.save();

              // Redirect to originalURL after successful authentication
              fulfill({
                  req: req,
                  res: res
              });
          }).catch(function (error) {
              console.error("error! ", error);
              reject(error);
          }).done();
    });
}

app.get('/authenticate/callback', function(req, res) {
    //console.log("callback, query: %j", req.query);
    //console.log("CALLBACK CALLED, SESSION: %j", req.session);

    setGetAccessTokenOptions(req, res)
      .then(getAccessToken)
      .then(redirectToOriginalURL)
      .done();
});
