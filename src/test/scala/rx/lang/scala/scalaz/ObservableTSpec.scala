package rx.lang.scala.scalaz

import org.junit.runner.RunWith
import org.scalacheck.Prop._
import org.specs2.runner.JUnitRunner
import org.specs2.scalaz.Spec
import rx.lang.scala.Observable

import scalaz.Scalaz._
import scalaz._
import scalaz.scalacheck.ScalazProperties._

@RunWith(classOf[JUnitRunner])
class ObservableTSpec extends Spec {

  import rx.lang.scala.scalaz.ImplicitsForTest._

  type ObservableTId[A] = ObservableT[Id, A]
  //  type ObservableTList[A] = ObservableT[List, A]
  //  type ObservableTOption[A] = ObservableT[Option, A]

  checkAll(equal.laws[ObservableTId[Int]])
  //  checkAll(equal.laws[ObservableTList[Int]])
  //  checkAll(equal.laws[ObservableTOption[Int]])

  checkAll(monoid.laws[ObservableTId[Int]])
  //  checkAll(monoid.laws[ObservableTList[Int]])
  //  checkAll(monoid.laws[ObservableTOption[Int]])

  checkAll(monadPlus.laws[ObservableTId])
  //  checkAll(monadPlus.laws[ObservableTList])
  //  checkAll(monadPlus.laws[ObservableTOption])

}
