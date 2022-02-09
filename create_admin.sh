#!/bin/bash

if [ $# != 4 ] 
then
    echo "expected 4 arguments (webserver-pod-name, username, email, password)"
    exit -1
fi

kubectl exec $1 -- python3 manage.py shell -c "from django.contrib.auth import get_user_model; User = get_user_model(); User.objects.create_superuser('$2', '$3', '$4')"
