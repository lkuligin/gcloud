bq mk demos
bq query --destination_table=demos.nyc_weather --use_legacy_sql=false "$(cat ./demos.sql)"
bq query --use_legacy_sql=false "$(cat ./corr_complaints_temp.sql)"
bq query --use_legacy_sql=false "$(cat ./corr_complaints_wind.sql)"
