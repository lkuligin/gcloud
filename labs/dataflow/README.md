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
Run this:
```
mvn test
mvn compile -e exec:java -Dexec.mainClass=com.lkuligin.training.dataflow.JavaProjectsThatNeedHelp \
 -Dexec.args="--project=$PROJECT_ID --stagingLocation=gs://$PROJECT_ID/staging --tempLocation=gs://$PROJECT_ID/staging/ \
 --outputPrefix=gs://$PROJECT_ID/test/output2.csv --runner=DataflowRunner"
```
