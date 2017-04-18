package controllers.helpers

import play.api.mvc.PathBindable
import play.api.routing.sird.PathBindableExtractor

object EnumPathExtractor {
  def binders[E <: Enumeration](enum: E): PathBindableExtractor[E#Value] = {
    val pb = new PathBindable.Parsing[E#Value](
      enum.withName(_),
      (e: E#Value) => e.toString,
      (k: String, e: Exception) => "Cannot parse %s as %s: %s".format(k, enum.getClass.getName, e.getMessage))
    new PathBindableExtractor[E#Value]()(pb)
  }

}