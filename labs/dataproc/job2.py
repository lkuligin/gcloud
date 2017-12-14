#!/usr/bin/env python

import argparse
from pyspark import SparkContext

def runJob(bucket_name, file_name):
	sc = SparkContext("local")

	file = sc.textFile("gs://{0}/{1}".format(bucket_name, file_name))
	dataLines = file.map(lambda s: s.split(",")).map(lambda x : (x[0], [x[1]]))
	print dataLines.take(100)

	databyKey = dataLines.reduceByKey(lambda a, b: a + b)
	print databyKey.take(100)

	countByKey = databyKey.map(lambda (k,v): (k, len(v)))
	print countByKey.take(100)

	sc.stop()

def parse_args():
	parser = argparse.ArgumentParser(description = 'Reading input file from the bucket and processing it to the stdin.')
	parser.add_argument("bucket_name", type = str, help = "name of the input bucket")
	parser.add_argument("input_file", type = str, nargs = "?", help = "name of the input file", default  = "job2.txt")
	args = parser.parse_args()
	return vars(args)

def main():
	args =  parse_args()
	print args
	runJob(args.get("bucket_name"), args.get("input_file"))

if __name__ == "__main__":
	main()
