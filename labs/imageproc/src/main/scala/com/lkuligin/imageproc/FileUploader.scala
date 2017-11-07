package com.lkuligin.imageproc

import java.io.File
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.IOUtils
import org.apache.spark.SparkContext

/**
  * downloads file to every node in the cluster
  */
class FileUploader{

  def copyFile(from: String, to: String) {
    val conf = new Configuration()
    val fromPath = new Path(from)
    val toPath = new Path(to)
    val is = fromPath.getFileSystem(conf).open(fromPath)
    val os = toPath.getFileSystem(conf).create(toPath)
    IOUtils.copyBytes(is, os, conf)
    is.close()
    os.close()
  }

  def downloadToCluster(path: String, sc: SparkContext): File = {
    val file = File.createTempFile("file_", path.substring(path.lastIndexOf(".")), new File("/tmp/"))
    val destPath = s"file://${file.getPath()}"
    copyFile(path, destPath)
    sc.addFile(destPath)
    file
  }

}

object FileUploader {
  def apply() = new FileUploader
}
