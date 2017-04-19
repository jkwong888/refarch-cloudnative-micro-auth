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

auth_deployment=$(kubectl get deployments | grep auth-service | head -1 | awk '{print $1}')
# Check if deployment does not exist
if [ -z "${auth_deployment}" ]; then
	# Deploy service
	echo -e "Deploying auth pod for the first time"

	# Enter secret and image name into yaml
    cat kubernetes/auth.yaml | \
        yaml w - spec.template.spec.containers[0].image ${image_name} | \
        yaml w - spec.template.spec.containers[0].volumeMounts[0].name hs256-key | \
        yaml w - spec.template.spec.volumes[0].secret.secretName hs256-key \
        > auth.yaml
    
	# Do the deployment
	kubectl --token=${token} create -f auth.yaml
else
	# Do rolling update
	echo -e "Doing a rolling update on auth Deployment"
	kubectl --token=${token} set image deployment/auth-microservice auth-service=${image_name}

	# Watch the rollout update
	kubectl --token=${token} rollout status deployment/auth-microservice
fi


auth_service=$(kubectl get services | grep auth-service | head -1 | awk '{print $1}')

# Check if service does not exist
if [ -z "${auth_service}" ]; then
	# Deploy service
	echo -e "Deploying auth service for the first time"

	# Do the deployment
	kubectl --token=${token} create -f kubernetes/auth-service.yaml
fi
