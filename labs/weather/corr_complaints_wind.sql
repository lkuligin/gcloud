SELECT
  descriptor,
  sum(complaint_count) as total_complaint_count,
  count(wind_speed) as data_count,
  ROUND(corr(wind_speed, avg_count),3) AS corr_count,
  ROUND(corr(wind_speed, avg_pct_count),3) AS corr_pct
From (
SELECT
  avg(pct_count) as avg_pct_count,
  avg(day_count) as avg_count,
  sum(day_count) as complaint_count,
  descriptor,
  wind_speed
FROM (
  SELECT
    DATE(timestamp) AS date,
    wind_speed
  FROM
    demos.nyc_weather)a
  JOIN (
  SELECT x.date, descriptor, day_count, day_count / all_calls_count as pct_count
  FROM
    (SELECT
      DATE(created_date) AS date,
      concat(complaint_type, ": ", descriptor) as descriptor,
      COUNT(*) AS day_count
    FROM
      `bigquery-public-data.new_york.311_service_requests` 
    GROUP BY
      date,
      descriptor)x 
    JOIN (
      SELECT
        DATE(created_date) AS date,
        COUNT(*) AS all_calls_count
      FROM `bigquery-public-data.new_york.311_service_requests` 
      GROUP BY date
    )y
  ON x.date=y.date
)b
ON
  a.date = b.date
GROUP BY
  descriptor,
  wind_speed
)
GROUP BY descriptor
HAVING 
  total_complaint_count > 5000 AND 
  ABS(corr_pct) > 0.5 AND
  data_count > 10
ORDER BY
  ABS(corr_pct) DESC
  