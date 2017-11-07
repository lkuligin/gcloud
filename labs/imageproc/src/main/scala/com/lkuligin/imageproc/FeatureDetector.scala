package com.lkuligin.imageproc

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.{SparkConf, SparkContext}

object FeatureDetector extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val sc = new SparkContext(conf)

    val timestampInit = System.currentTimeMillis()

    if (args.length < 2 || args.length > 3) {
      logger.debug("usage: <classifier-path> <input-directory> [output-directory]")
      System.exit(-1)
    }

    val classifierPath = args(0)
    val inputDirName = args(1)
    val outputDirName = args.lift(2).getOrElse(inputDirName)

    val classifier = FileUploader().downloadToCluster(classifierPath, sc)

    val inputDir = new Path(inputDirName)
    val fs = inputDir.getFileSystem(new Configuration())
    val files = fs.listStatus(inputDir)
    val filePaths = files.map(_.getPath().toString())
    logger.debug("Number of files found: " + filePaths.length)

    sc.parallelize(filePaths).foreach({
      ImageProcessor().processImage(_, classifier.getName(), outputDirName)
    })
  }
}
