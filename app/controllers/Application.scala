package controllers

import play.api._
import play.api.mvc._
import services.UploadService

object Application extends Controller with Application {
  val uploadService: UploadService = UploadService
}

trait Application {
  this: Controller =>

  val uploadService: UploadService

  def index = Action { implicit request =>
    Ok(views.html.index("Playing AWS Scala"))
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    val result = uploadService.uploadFile(request)
    Redirect(routes.Application.index).flashing("message" -> result)
  }

}