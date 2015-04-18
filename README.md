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
- S3Utility to create/upload bucket/file on AWS S3:
[S3Utility.scala](https://github.com/knoldus/playing-aws-scala/blob/master/app/utils/S3Utility.scala)

```scala
package utils

import awscala._, s3._

object S3Utility extends S3Utility

trait S3Utility {

  implicit val s3 = S3()

  /**
   * Get all the available buckets
   *
   * @return
   */
  def getBuckets(): Seq[Bucket] = s3.buckets

  /**
   * Get the bucket by given name
   *
   * @param name The Bucket name
   * @return
   */
  def getBucketByName(name: String): Option[Bucket] = s3.bucket(name)

  /**
   * Create new bucket for given name
   *
   * @param name The Bucket name
   * @return
   */
  def createBucket(name: String): Bucket = s3.createBucket(name)

  /**
   * Create an object into given bucket by name
   *
   * @param bucket The Bucket
   * @param name The Object name
   * @param file The Object
   * @return
   */
  def createObject(bucket: Bucket, name: String, file: File): PutObjectResult = bucket.put(name, file)

  /**
   * Get the Object by given name from given bucket
   *
   * @param bucket The Bucket
   * @param name The Object name
   * @return
   */
  def getObject(bucket: Bucket, name: String): Option[S3Object] = bucket.getObject(name)

}

```

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
###AWS credentials! Make sure about environment or configuration
-----------------------------------------------------------------------
```
export AWS_ACCESS_KEY_ID=<ACCESS_KEY>
export AWS_SECRET_KEY=<SECRET_KEY>
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

