NAME="earthquake-1"

GCLOUD="/Users/kuligin/Documents/tools/google-cloud-sdk/bin/gcloud"
RAWSTATUS="$($GCLOUD compute instances list --filter="name:$NAME")"
STATUS="$(echo $RAWSTATUS | awk '{print $NF}')"

if [ "$STATUS" = "" ]
then
	echo "creating the VM"
	$GCLOUD compute --project "leonid-on-boarding" instances create "$NAME" \
	--zone "europe-west1-b" --machine-type "n1-standard-1" --subnet "default" \
	--no-restart-on-failure --maintenance-policy "TERMINATE" \
	--service-account "157762861736-compute@developer.gserviceaccount.com" \
	--scopes "https://www.googleapis.com/auth/devstorage.read_write","https://www.googleapis.com/auth/logging.write","https://www.googleapis.com/auth/monitoring.write","https://www.googleapis.com/auth/servicecontrol","https://www.googleapis.com/auth/service.management.readonly","https://www.googleapis.com/auth/trace.append" \
	--image "ubuntu-1704-zesty-v20171011" --image-project "ubuntu-os-cloud" --boot-disk-size "10" --boot-disk-type "pd-standard" --boot-disk-device-name "earthquake-1"
elif [ "$STATUS" == "TERMINATED" ]
then
	echo "TERMINATED"
else 
	echo "status is $STATUS"
fi
