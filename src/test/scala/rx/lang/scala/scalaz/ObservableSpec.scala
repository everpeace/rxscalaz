package rx.lang.scala.scalaz

import org.junit.runner.RunWith
import org.scalacheck.Prop._
import org.scalacheck.Properties
import org.specs2.runner.JUnitRunner
import org.specs2.scalaz.Spec
import rx.lang.scala.Observable

import scalaz.Scalaz._
import scalaz._
import scalaz.scalacheck.ScalazProperties._

@RunWith(classOf[JUnitRunner])
class ObservableSpec extends Spec {

  import rx.lang.scala.scalaz.ImplicitsForTest._

  checkAll(equal.laws[Observable[Int]])
  checkAll(monoid.laws[Observable[Int]])
  checkAll(monad.laws[Observable])
  checkAll(monadPlus.strongLaws[Observable])
  checkAll(isEmpty.laws[Observable])
  checkAll(traverse.laws[Observable])

  checkAll(new Properties("zip operator <*|*>") {
    property("should work equivalently with Observable.zip") = forAll { (ob: Observable[Int], f: Int => Int) =>
      (ob <*|*> (_ map f)) === (ob zip (ob map f))
    }
  })
}
