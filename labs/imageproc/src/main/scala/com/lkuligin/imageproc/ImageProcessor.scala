package com.lkuligin.imageproc

import org.apache.spark.SparkFiles
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import org.bytedeco.javacpp.opencv_imgcodecs.imwrite
import java.io.File
import org.bytedeco.javacpp.opencv_imgproc.rectangle
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier
import org.bytedeco.javacpp.opencv_core.Mat
import org.bytedeco.javacpp.opencv_core.RectVector
import org.bytedeco.javacpp.opencv_core.Scalar

class ImageProcessor extends LazyLogging {
  def processImage(inPath: String, classifier: String, outputDir: String): String = {
    val classifierName = SparkFiles.get(classifier)

    logger.debug("Processing image file: " + inPath)

    val suffix = inPath.lastIndexOf(".")
    val localIn = File.createTempFile("pic_",
      inPath.substring(suffix),
      new File("/tmp/"))
    val fl = FileUploader()
    fl.copyFile(inPath, s"file://${localIn.getPath}")


    val inImg = imread(localIn.getPath())
    val detector = new CascadeClassifier(classifierName)
    val outImg = detectFeatures(inImg, detector)


    val localOut = File.createTempFile("out_",
      inPath.substring(suffix),
      new File("/tmp/"))
    imwrite(localOut.getPath(), outImg)


    val filename = inPath.substring(inPath.lastIndexOf("/") + 1)
    val extension = filename.substring(filename.lastIndexOf("."))
    val name = filename.substring(0, filename.lastIndexOf("."))
    val outPath = s"$outputDir${name}_output$extension"
    fl.copyFile(s"file://${localOut.getPath}", outPath)
    outPath
  }

  private def detectFeatures(img: Mat, detector: CascadeClassifier): Mat = {
    val features = new RectVector()
    detector.detectMultiScale(img, features)
    val numFeatures = features.size().toInt
    val outlined = img.clone()

    // Draws the rectangles on the detected features.
    val green = new Scalar(0, 255, 0, 0)
    for (f <- 0 until numFeatures) {
      val currentFeature = features.get(f)
      rectangle(outlined, currentFeature, green)
    }
    outlined
  }

}

object ImageProcessor {
  def apply() = new ImageProcessor
}
