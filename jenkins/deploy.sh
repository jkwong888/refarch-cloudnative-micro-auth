#!/bin/bash

build_number=${BUILD_NUMBER}
registry_namespace=chrisking
image_name="registry.ng.bluemix.net/${registry_namespace}/micro-auth:${build_number}"
token=`cat /var/run/secrets/kubernetes.io/serviceaccount/token`
cluster_name=`cat /var/run/secrets/bx-auth-secret/CLUSTER_NAME`

function get_hs256_key {
	echo $(kubectl --token=${token} get secrets | grep "hs256-key" | awk '{print $1}')
}

function create_hs256_key {
    kubectl --token=${TOKEN} create secret generic hs256-key --from-literal=key=`cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 256 | head -n 1 | xargs echo -n`
}

set -x

# Check if elasticsearch secret exists
hs256_key=`get_hs256_key`

if [ -z "${hs256_key}" ]; then
	echo "hs256 secret key does not exist. Creating"
    kubectl --token=${TOKEN} create secret generic hs256-key --from-literal=key=`cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 256 | head -n 1 | xargs echo -n`

fi

# Do rolling update here
auth_service=$(kubectl get services | grep auth-service | head -1 | awk '{print $1}')

# Check if service does not exist
if [ -z "${auth_service}" ]; then
	# Deploy service
	echo -e "Deploying auth for the first time"

	# Enter secret and image name into yaml
    cat kubernetes/auth.yaml | \
        yaml w - spec.template.spec.containers[0].image ${image_name} | \
        yaml w - spec.template.spec.containers[0].volumeMounts[0].name hs256-key | \
        yaml w - spec.template.spec.volumes[0].secret.secretName hs256-key \
        > auth.yaml
    
    cat auth.yaml

	# Do the deployment
	kubectl --token=${token} create -f auth.yaml
	kubectl --token=${token} create -f kubernetes/auth-service.yaml

else
	# Do rolling update
	echo -e "Doing a rolling update on auth Deployment"
	kubectl set image deployment/auth-microservice auth-service=${image_name}

	# Watch the rollout update
	kubectl rollout status deployment/auth-microservice
fi

