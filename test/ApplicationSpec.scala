import scala.concurrent.Future
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import controllers.Application
import play.api.mvc.Controller
import play.api.mvc.Results
import play.api.test._
import play.api.test.Helpers._
import services.UploadService
import org.junit.runner.RunWith
import play.api.mvc.Result
import play.api.mvc.Request
import play.api.mvc.MultipartFormData
import play.api.libs.Files.TemporaryFile

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification with Mockito with Results {

  val mockedUploadService: UploadService = mock[UploadService]

  object TestController extends Controller with Application {
    val uploadService: UploadService = mockedUploadService
  }

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain("Welcome to Play")
    }

    "should be valid" in new WithApplication {
      val request = mock[Request[MultipartFormData[TemporaryFile]]]
      mockedUploadService.uploadFile(request) returns "File Uploaded"
      val result: Future[Result] = TestController.upload().apply(request)
      status(result) must equalTo(SEE_OTHER)
    }
  }
}
