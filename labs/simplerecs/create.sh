IP=$(wget -qO - http://ipecho.net/plain; echo)

gcloud sql instances create rentals --tier="db-n1-standard-1" --region="europe-west1"
gcloud sql users set-password root % --instance rentals --password ***
gcloud sql instances patch rentals --authorized-networks=$IP
