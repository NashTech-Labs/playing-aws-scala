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