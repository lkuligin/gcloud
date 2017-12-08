curl -o ~/Downloads/rating.csv "https://github.com/GoogleCloudPlatform/training-data-analyst/tree/master/CPB100/lab3a/cloudsql/rating.csv"
curl -o ~/Downloads/accomodation.csv "https://github.com/GoogleCloudPlatform/training-data-analyst/tree/master/CPB100/lab3a/cloudsql/accomodation.csv"

gsutil cp ~/Downloads/rating.csv gs://test-1113
gsutil cp ~/Downloads/accomodation.csv gs://test-1113
	