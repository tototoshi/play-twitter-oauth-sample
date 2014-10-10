package libs

import play.api.Play.current
import play.twirl.api.Html

object Jade {

  import org.fusesource.scalate._
  import org.fusesource.scalate.util.FileResourceLoader

  val engine = {
    val e = new TemplateEngine
    e.resourceLoader = new FileResourceLoader(Some(current.getFile("app/views")))
    e
  }

  def render(template: String, params: Map[String, Any] = Map.empty): Html = {
    Html(engine.layout(template, params))
  }

}
