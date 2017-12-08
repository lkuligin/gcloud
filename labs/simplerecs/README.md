The lab description can be found here: [part a](https://codelabs.developers.google.com/codelabs/cpb100-dataproc/) and [part b](https://codelabs.developers.google.com/codelabs/cpb100-cloud-sql/)!
### Cloud SQL setup
Setup an instance:
```
./create.sh
```
Get the data (*accommodation.csv* and *rating.csv*), copy them to the bucket and import tables (apply *ddl.sql* and import data to each table):
```
gsutil cp ~/Downloads/accomodation.csv gs://test-1113
gsutil cp ~/Downloads/rating.csv gs://test-1113
```
### Data exploration
The best way to explore the data is via cloudshell since mysql is already installed.
There are two ways to connect:
```mysql --host="35.189.215.238" --user=root --password``` or ```gcloud beta sql connect rentals --user=root```
### Predictions
Create a dataproc cluster
```
gcloud dataproc clusters create rentals --region="europe-west1" --master-machine-type="n1-standard-2" --worker-machine-type="n1-standard-2" --num-workers=2
```
Authrorize access to CloudSQl:
```
./authorize_dataproc.sh
```
Submit the job:
```
gcloud dataproc jobs submit pyspark recs.py  --cluster=rentals --region="europe-west1"  
```
Clean up:
```
gcloud -q dataproc clusters delete rentals --region="europe-west1"
```
