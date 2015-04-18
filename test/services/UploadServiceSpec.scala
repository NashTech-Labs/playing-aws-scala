package services

import scala.concurrent.Future
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import controllers.Application
import play.api.mvc.Controller
import play.api.mvc.Results
import play.api.test._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import play.api.mvc.Result
import play.api.mvc.Request
import play.api.mvc.MultipartFormData
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.BadPart
import play.api.mvc.MultipartFormData.MissingFilePart
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files

class UploadServiceSpec extends Specification with Mockito {

  "UploadService" should {
    "uploadFile returns (File uploaded)" in new WithApplication {
      val files = Seq[FilePart[TemporaryFile]](FilePart("file", "UploadServiceSpec.scala", None, TemporaryFile("file", "spec")))
      val multipartBody = MultipartFormData(Map[String, Seq[String]](), files, Seq[BadPart](), Seq[MissingFilePart]())
      val fakeRequest = FakeRequest[MultipartFormData[Files.TemporaryFile]]("POST", "/", FakeHeaders(), multipartBody)
      val success = UploadService.uploadFile(fakeRequest)
      success must beEqualTo("File(UploadServiceSpec.scala) uploaded")
    }
    
    "uploadFile returns (Missing file)" in new WithApplication {
      val files = Seq[FilePart[TemporaryFile]]()
      val multipartBody = MultipartFormData(Map[String, Seq[String]](), files, Seq[BadPart](), Seq[MissingFilePart]())
      val fakeRequest = FakeRequest[MultipartFormData[Files.TemporaryFile]]("POST", "/", FakeHeaders(), multipartBody)
      val success = UploadService.uploadFile(fakeRequest)
      success must beEqualTo("Missing file")
    }
  }

}