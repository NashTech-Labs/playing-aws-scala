# playing-aws-scala
A simple example of Amazon Web Services in the Scala way with Play Framework and [AWScala](https://github.com/seratch/AWScala)

###[AWScala](https://github.com/seratch/AWScala): AWS SDK on the Scala REPL

AWScala enables Scala developers to easily work with Amazon Web Services in the Scala way.

Though AWScala objects basically extend AWS SDK for Java APIs, you can use them with less stress on Scala REPL or ```sbt console```.

-----------------------------------------------------
###AWScala Supported Services
-----------------------------------------------------
- AWS Identity and Access Management (IAM)
- AWS Security Token Service (STS)
- Amazon Elastic Compute Cloud (Amazon EC2)
- Amazon Simple Storage Service (Amazon S3)
- Amazon Simple Queue Service（Amazon SQS）
- Amazon Redshift
- Amazon DynamoDB
- Amazon SimpleDB

-----------------------------------------------------
###Amazon Simple Storage Service (Amazon S3)
-----------------------------------------------------
- Multipart form upload handlers looks like this:
[Application.scala](https://github.com/knoldus/playing-aws-scala/blob/master/app/controllers/Application.scala)

```scala
  val uploadService: UploadService

  def upload = Action(parse.multipartFormData) { implicit request =>
    val result = uploadService.uploadFile(request)
    Redirect(routes.Application.index).flashing("message" -> result)
  }
```


- Service for upload file looks like this:
[UploadService.scala](https://github.com/knoldus/playing-aws-scala/blob/master/app/services/UploadService.scala)
```scala
  /**
   * Get file from the request and move it in your location
   *
   * @param request
   * @return
   */
  def uploadFile(request: Request[MultipartFormData[TemporaryFile]]): String = {
    log.error("Called uploadFile function" + request)
    request.body.file("file").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      log.error(s"File name : $filename, content type : $contentType")
      picture.ref.moveTo(new File(s"/tmp/$filename"))
      "File uploaded"
    }.getOrElse {
      "Missing file"
    }
  }

```
------------------------------------------------------
###Test Code for Controller and Service
------------------------------------------------------
[ApplicationSpec.scala](https://github.com/knoldus/playing-aws-scala/blob/master/test/ApplicationSpec.scala)
```scala
"should be valid" in new WithApplication {
  val request = mock[Request[MultipartFormData[TemporaryFile]]]
  mockedUploadService.uploadFile(request) returns "File Uploaded"
  val result: Future[Result] = TestController.upload().apply(request)
  status(result) must equalTo(SEE_OTHER)
}
```

[UploadServiceSpec.scala](https://github.com/knoldus/playing-aws-scala/blob/master/test/services/UploadServiceSpec.scala)
```scala
"UploadService" should {
    "uploadFile returns (File uploaded)" in new WithApplication {
      val files = Seq[FilePart[TemporaryFile]](FilePart("file", "UploadServiceSpec.scala", None, TemporaryFile("file", "spec")))
      val multipartBody = MultipartFormData(Map[String, Seq[String]](), files, Seq[BadPart](), Seq[MissingFilePart]())
      val fakeRequest = FakeRequest[MultipartFormData[Files.TemporaryFile]]("POST", "/", FakeHeaders(), multipartBody)
      val success = UploadService.uploadFile(fakeRequest)
      success must beEqualTo("File uploaded")
    }
    
    "uploadFile returns (Missing file)" in new WithApplication {
      val files = Seq[FilePart[TemporaryFile]]()
      val multipartBody = MultipartFormData(Map[String, Seq[String]](), files, Seq[BadPart](), Seq[MissingFilePart]())
      val fakeRequest = FakeRequest[MultipartFormData[Files.TemporaryFile]]("POST", "/", FakeHeaders(), multipartBody)
      val success = UploadService.uploadFile(fakeRequest)
      success must beEqualTo("Missing file")
    }
}
```

-----------------------------------------------------------------------
###Build and Run the application
-----------------------------------------------------------------------
* To run the Play Framework, you need JDK 6 or later
* Install Typesafe Activator if you do not have it already. You can get it from [here](http://www.playframework.com/download) 
* Execute `./activator clean compile` to build the product
* Execute `./activator run` to execute the product
* playing-aws-scala should now be accessible at localhost:9000

-----------------------------------------------------------------------
###Test the application with code coverage
-----------------------------------------------------------------------
* Execute `$ ./activator clean coverage test` to test
* Execute `$ ./activator coverageReport` to generate coverage report

-----------------------------------------------------------------------
###References :-
-----------------------------------------------------------------------
* [Play Framework](http://www.playframework.com/)
* [AWScala: AWS SDK in Scala](https://github.com/seratch/AWScala)
* [Bootstrap 3.1.1](http://getbootstrap.com/css/)
* [Bootswatch](http://bootswatch.com/darkly/)
* [WebJars](http://www.webjars.org/)

