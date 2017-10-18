ssh-keygen -t rsa -f ~/.ssh/gcloud
chmod 400 ~/.ssh/gcloud

gcloud compute project-info add-metadata --metadata-from-file key=~/.ssh/gcloud.pub

ssh -i ~/.ssh/glouc lkulighin@35.195.172.82
