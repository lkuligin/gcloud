### Prerequisites:
Create a bucket if you didn't do it before:
```
gsutil mb -c regional -l europe-west1 gs://$PROJECT_ID
```
Start the cluster:
```
export PROJECT_ID="test-project-188517"
IP=$(wget -qO - http://ipecho.net/plain; echo)

gsutil cp -r . gs://$PROJECT_ID/dataproc-lab
gcloud dataproc clusters create test-cluster-1 --region="europe-west1" --master-machine-type="n1-standard-2" --master-boot-disk-size=50 \
    --num-workers=2 --worker-machine-type="n1-standard-1" --worker-boot-disk-size=50 --network=default \
    --initialization-actions="gs://dataproc-initialization-actions/datalab/datalab.sh,gs://$PROJECT_ID/dataproc-lab/init.sh" \
    --scopes 'https://www.googleapis.com/auth/cloud-platform'
gcloud compute firewall-rules create dataproc-test-1 --allow tcp:8088,tcp:9870,tcp:8080 --source-ranges="$IP/32"
gcloud compute scp .  test-cluster-1-m:~/dataproc --recurse --zone="europe-west1-d"
```

### Lab1
[Par1](https://codelabs.developers.google.com/codelabs/cpb102-creating-dataproc-clusters) and [part2](https://codelabs.developers.google.com/codelabs/cpb102-running-pig-spark/#0)
```
gcloud compute --project $PROJECT_ID ssh --zone "europe-west1-d" "test-cluster-1-m"

hadoop fs -mkdir /job1
hadoop fs -put job1.txt /job1
pig < job1.pig
hadoop fs -get /GroupedByType/part* result.txt
```

### Lab2
[Here](https://codelabs.developers.google.com/codelabs/cpb102-running-dataproc-jobs). Submit the job with command line arguments:
``` 
gcloud dataproc jobs submit pyspark --cluster test-cluster-1 gs://$PROJECT_ID/dataproc-lab/job2.py \
  --region="europe-west1" -- $PROJECT_ID/dataproc-lab
```

### Lab3
[Par1](https://codelabs.developers.google.com/codelabs/cpb102-dataproc-with-gcp) and [part2](https://codelabs.developers.google.com/codelabs/cpb102-machine-learning-to-big-data-processing). Datalab is already set up, so you can try ipynb easily.

### Clean up
```
gcloud dataproc clusters delete -q test-cluster-1 --region="europe-west1"
```
