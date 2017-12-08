#!/usr/bin/env python
"""
"""
from __future__ import print_function

import os
import sys
import pickle
import itertools
from math import sqrt
from operator import add
from os.path import join, isfile, dirname
from pyspark import SparkContext, SparkConf, SQLContext
from pyspark.mllib.recommendation import ALS, MatrixFactorizationModel, Rating
from pyspark.sql.types import StructType, StructField, StringType, FloatType

CLOUDSQL_INSTANCE_IP = '35.189.215.238'
CLOUDSQL_DB_NAME = 'recommendation_spark'
CLOUDSQL_USER = 'root'
CLOUDSQL_PWD  = '123u56' 

class CloudSQLAgent:
  jdbcDriver = 'com.mysql.jdbc.Driver'
  
  def __init__(self, sqlContext, instance_ip = CLOUDSQL_INSTANCE_IP, db_name = CLOUDSQL_DB_NAME, user = CLOUDSQL_USER, pwd = CLOUDSQL_PWD):
    self.jdbcUrl = 'jdbc:mysql://%s:3306/%s?user=%s&password=%s' % (instance_ip, db_name, user, pwd)
    self.sqlContext = sqlContext

  def get_full_table(self, table_name):
    return self.sqlContext.read.format('jdbc').options(driver = self.jdbcDriver, url = self.jdbcUrl, dbtable = table_name).load()

  def save_full_table(self, df, table_name, schema):
    df.write.jdbc(url = self.jdbcUrl, table = table_name, mode = 'overwrite')


def initiate():
  conf = SparkConf().setAppName("train_model")
  sc = SparkContext(conf=conf)
  sqlContext = SQLContext(sc)

  #  checkpointing helps prevent stack overflow errors
  sc.setCheckpointDir('checkpoint/')
  print("context created")
  return sqlContext


def train(ratings):  
  return ALS.train(ratings.rdd, 20, 20)


def predict(sqlContext, model, ratings, accommodations):
  # use this model to predict what the user would rate accommodations that she has not rated
  allPredictions = None
  
  for user_id in xrange(0, 100):
    user_ratings = ratings.filter(ratings.userId == user_id).rdd.map(lambda r: r.accoId).collect()
    rddPotential  = accommodations.rdd.filter(lambda x: x[0] not in user_ratings)
    pairsPotential = rddPotential.map(lambda x: (user_id, x[0]))
    predictions = model.predictAll(pairsPotential).map(lambda p: (str(p[0]), str(p[1]), float(p[2])))
    predictions = predictions.takeOrdered(5, key=lambda x: -x[2]) # top 5
    print("predicted for user={0}".format(user_id))
    if (allPredictions == None):
      allPredictions = predictions
    else:
      allPredictions.extend(predictions)
  return allPredictions

def save(sqlContext, predictions, sql_agent):
  # write them
  schema = StructType([StructField("userId", StringType(), True), StructField("accoId", StringType(), True), StructField("prediction", FloatType(), True)])
  dfToSave = sqlContext.createDataFrame(predictions, schema)
  sql_agent.save_full_table(dfToSave, "Recommendation", schema)

def get_recommendations():
  sqlContext = initiate()
  sql_agent = CloudSQLAgent(sqlContext)

  ratings = sql_agent.get_full_table("Rating")
  accomodations = sql_agent.get_full_table("Accommodation")
  print("raw data loaded")
  
  model = train(ratings)
  print("model trained")
  predictions = predict(sqlContext, model, ratings, accomodations)
  print("prediction ready")
  save(sqlContext, predictions, sql_agent)
  print("predictions exported to mysql")

def main():
  get_recommendations()

if __name__ == "__main__":
  main()
