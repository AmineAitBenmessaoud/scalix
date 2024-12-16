package model

case class Movie(id: Int, title: String) {
  override def toString: String = s"Film(id=$id, title=$title)"
}
