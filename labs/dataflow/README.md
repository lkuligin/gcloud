### Preparation
Create BigQuery table:
```
curl -o ~/Downloads/schema_flight_performance.json https://storage.googleapis.com/cloud-training/CPB200/BQ/lab4/schema_flight_performance.json 
bq mk $PROJECT_ID:example_flight_data
bq load --nosync --source_format=NEWLINE_DELIMITED_JSON $PROJECT_ID:example_flight_data.flights_2014 gs://cloud-training/CPB200/BQ/lab4/domestic_2014_flights_*.json ~/Downloads/schema_flight_performance.json
bq ls $PROJECT_ID:example_flight_data
```
### Lab1
The description could be found [here](https://codelabs.developers.google.com/codelabs/cpb101-simple-dataflow/). Run the grep example locally:
```
mvn compile -e exec:java -Dexec.mainClass=com.lkuligin.training.dataflow.Grep \
 -Dexec.args="--searchTerm=sdk"
```
Enjoy the results:
```
cat /tmp/output.txt
```
In order to run it on the cloud, you might need to perform ```gcloud auth application-default login``` and then:
```
mvn test
mvn compile -e exec:java -Dexec.mainClass=com.lkuligin.training.dataflow.Grep \
  -Dexec.args="--project=$PROJECT_ID --stagingLocation=gs://$PROJECT_ID/staging \
  --tempLocation=gs://$PROJECT_ID/staging/ --runner=DataflowRunner --searchTerm=import \
  --outputPrefix=gs://$PROJECT_ID/test/output.txt --inputDir=gs://$PROJECT_ID/dataproc-lab/*.py"
```
### Lab2
[Description](https://codelabs.developers.google.com/codelabs/cpb101-mapreduce-dataflow/). 
Run locally
```
mvn test
mvn compile -e exec:java \
 -Dexec.mainClass=com.lkuligin.training.dataflow.IsPopular
cat  /tmp/output.csv 
```
### Lab3
[Description](https://codelabs.developers.google.com/codelabs/cpb101-bigquery-dataflow-sideinputs). Run this:
```
mvn test
mvn compile -e exec:java -Dexec.mainClass=com.lkuligin.training.dataflow.JavaProjectsThatNeedHelp \
 -Dexec.args="--project=$PROJECT_ID --stagingLocation=gs://$PROJECT_ID/staging --tempLocation=gs://$PROJECT_ID/staging/ \
 --outputPrefix=gs://$PROJECT_ID/test/output2.csv --runner=DataflowRunner"
```
### Lab4
[Description](https://codelabs.developers.google.com/codelabs/cpb101-bigquery-dataflow-streaming). Preparation:
```
bq mk $PROJECT_ID:demos
gcloud pubsub topics create streamdemo --project=$PROJECT_ID
```
Run the streaming job, publish manually some messages to the PubSub topic (with certain delay between the messages!) and inspect the result:
```
mvn test
mvn compile -e exec:java -Dexec.mainClass=com.lkuligin.training.dataflow.StreamDemoConsumer \
	-Dexec.args="--project=$PROJECT_ID --stagingLocation=gs://$PROJECT_ID/staging/ \
    --tempLocation=gs://$PROJECT_ID/staging/ --output=$PROJECT_ID:demos.streamdemo \
    --input=projects/$PROJECT_ID/topics/streamdemo --runner=DataflowRunner"

gcloud pubsub topics publish streamdemo --message="line1 word1 test1" --project=$PROJECT_ID
gcloud pubsub topics publish streamdemo --message="line2 word2 word3 test2" --project=$PROJECT_ID
gcloud pubsub topics publish streamdemo --message="line1 word4 w5 w6 test1" --project=$PROJECT_ID
gcloud pubsub topics publish streamdemo --message="line1 w7 w8 w9 w1 w12" --project=$PROJECT_ID

bq query "SELECT timestamp, num_words from [$PROJECT_ID:demos.streamdemo] ORDER BY timestamp"
```
Cleanup:
```
gcloud pubsub topics delete streamdemo -project=$PROJECT_ID
gcloud dataflow jobs list --project=$PROJECT_ID --status=active
gcloud dataflow jobs cancel *** --region=us-central1 --project=$PROJECT_ID
gcloud pubsub topics delete streamdemo -project=$PROJECT_ID
bq rm -f -t $PROJECT_ID:demos.streamdemo
```
