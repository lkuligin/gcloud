## Initial setup
Google cloud SDK is needed in order to be able to manage the GC from the command line.
```
curl -o ~/Downloads/gcloud.tar.gz "https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-182.0.0-darwin-x86_64.tar.gz"

cd ~/Documents
mkdir tools
tar -xzf ~/Downloads/gcloud.tar.gz -C ./tools
./tools/google-cloud-sdk/install.sh 

echo 'alias gcloud="~/Documents/tools/google-cloud-sdk/bin/gcloud"' >> ~/.bash_profile 
echo 'alias gsutil="~/Documents/tools/google-cloud-sdk/bin/gsutil"' >> ~/.bash_profile
```
Now we can use ```gcloud``` and ```gsutil``` directly from the Terminal.
