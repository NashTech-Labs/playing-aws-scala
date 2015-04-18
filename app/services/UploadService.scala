package services

import java.io.File
import play.api.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData
import play.api.mvc.Request
import java.util.UUID
import play.api.Play
import play.api.Play.current
import utils.S3Utility

object UploadService extends UploadService {
  val s3Utility: S3Utility = S3Utility
}

trait UploadService {

  private val log: Logger = Logger(this.getClass)

  val s3Utility: S3Utility

  /**
   * Get file from the request and move it in your location
   *
   * @param request
   * @return
   */
  def uploadFile(request: Request[MultipartFormData[TemporaryFile]]): String = {
    log.error("Called uploadFile function" + request)
    request.body.file("file").map { file =>
      import java.io.File
      val filename = file.filename
      val contentType = file.contentType
      log.error(s"File name : $filename, content type : $contentType")
      val uniqueFile = new File(s"/tmp/${UUID.randomUUID}_$filename")
      file.ref.moveTo(uniqueFile, true)
      if (Play.isProd) {
        try {
          val bucket = s3Utility.getBucketByName("test").getOrElse(s3Utility.createBucket("test"))
          val result = s3Utility.createObject(bucket, filename, uniqueFile)
          s"File uploaded on S3 with Key : ${result.key}"
        } catch {
          case t: Throwable => log.error(t.getMessage, t); t.getMessage
        }
      } else {
        s"File(${filename}) uploaded"
      }
    }.getOrElse {
      "Missing file"
    }
  }

}
