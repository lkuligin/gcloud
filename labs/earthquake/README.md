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

## Lab itself
### Creating the instance and resources
We need to create the instance (first, we check whether the instance exists).
```
./launch.sh
```
Copying resources:
```
gcloud compute scp ./earthquake  earthquake-1:~ --recurse --zone="europe-west1-b"
```
Now we need to care about the bucket:
```
gsutil ls -b gs://test-1113
```
and create it if needed
```
gsutil mb gs://test-1113
```
### ssh to the VM
```
gcloud compute ssh earthquake-1 --zone="europe-west1-b"
```
### run the lab and get results
Run on the VM
```
./run.sh
```
create a public link for the picture
```
gsutil acl ch -u AllUsers:R gs://test-1113/earthquakes.png
```
and clean it up 
```
acl ch -d AllUsers gs://test-1113/earthquakes.png
```
copy results to the local machine (execute locally)
```
gcloud compute scp earthquake-1:~/earthquake/earthquakes.png ~/Downloads/earthquakes.png --zone="europe-west1-b"
```
