#!/bin/bash

build_number=${BUILD_NUMBER}
cf_email=$(cat /var/run/secrets/bx-auth-secret/CF_EMAIL)
cf_password=$(cat /var/run/secrets/bx-auth-secret/CF_PASSWORD)
cf_account=$(cat /var/run/secrets/bx-auth-secret/CF_ACCOUNT)
cf_org=$(cat /var/run/secrets/bx-auth-secret/CF_ORG)
cf_space=$(cat /var/run/secrets/bx-auth-secret/CF_SPACE)

build_number=${BUILD_NUMBER}
registry_namespace=chrisking
image_name="registry.ng.bluemix.net/${registry_namespace}/micro-auth:${build_number}"

# Install plugins
bx plugin install container-service -r Bluemix
bx plugin install container-registry -r Bluemix

# Login to Bluemix and init plugins
bx login \
    -a api.ng.bluemix.net \
    -u $cf_email \
    -p $cf_password \
    -c $cf_account \
    -o $cf_org \
    -s $cf_space

bx cs init
bx cr login

docker push ${image_name}

