package rx.lang.scala.scalaz.examples

import org.junit.runner.RunWith
import org.specs2.matcher.AnyMatchers
import org.specs2.runner.JUnitRunner
import org.specs2.scalaz.{ ScalazMatchers, Spec }
import rx.lang.scala.Observable
import rx.lang.scala.Observable.just

import scala.language.higherKinds

/**
 * This demonstrates how you apply Scalaz's operators to Observables.
 */
@RunWith(classOf[JUnitRunner])
class RxScalazDemo extends Spec with AnyMatchers with ScalazMatchers {

  import rx.lang.scala.scalaz.ImplicitsForTest._
  import rx.lang.scala.scalaz._

  import scalaz.Scalaz._
  import scalaz._

  "Observable" should {
    "be applied to Monoid operators" in {
      (just(1, 2) |+| just(3, 4)) must equal(just(1, 2, 3, 4))

      (just(1, 2) ⊹ just(3, 4)) must equal(just(1, 2, 3, 4))

      mzero[Observable[Int]] must equal(Observable.empty)
    }

    "be applied to Functor operators" in {
      (just(1, 2) ∘ { _ + 1 }) must equal(just(2, 3))

      (just(1, 2) >| 5) must equal(just(5, 5))

      (just(1, 2) as 4) must equal(just(4, 4))

      just(1, 2).fpair must equal(just((1, 1), (2, 2)))

      just(1, 2).fproduct { _ + 1 } must equal(just((1, 2), (2, 3)))

      just(1, 2).strengthL("x") must equal(just(("x", 1), ("x", 2)))

      just(1, 2).strengthR("x") must equal(just((1, "x"), (2, "x")))

      Functor[Observable].lift { (_: Int) + 1 }(just(1, 2)) must equal(just(2, 3))
    }

    "be applied to Applicative operators" in {
      1.point[Observable] must equal(just(1))

      1.η[Observable] must equal(just(1))

      (just(1, 2) |@| just(3, 4)) { _ + _ } must equal(just(4, 5, 5, 6))

      (just(1) <*> { (_: Int) + 1 }.η[Observable]) must equal(just(2))

      just(1) <*> { just(2) <*> { (_: Int) + (_: Int) }.curried.η[Observable] } must equal(just(3))

      just(1) <* just(2) must equal(just(1))

      just(1) *> just(2) must equal(just(2))

      Apply[Observable].ap(just(2)) { { (_: Int) + 3 }.η[Observable] } must equal(just(5))

      Apply[Observable].lift2 { (_: Int) * (_: Int) }(just(1, 2), just(3, 4)) must equal(just(3, 4, 6, 8))
    }

    "be applied to Monad and MonadPlus operators" in {
      (just(3) >>= { (i: Int) => just(i + 1) }) must equal(just(4))

      (just(3) >> just(2)) must equal(just(2))

      just(just(1, 2), just(3, 4)).μ must equal(just(1, 2, 3, 4))

      (just(1, 2) <+> just(3, 4)) must equal(just(1, 2, 3, 4))

      PlusEmpty[Observable].empty[Int] must equal(Observable.empty)
    }
    "be applied to Traverse operators" in {
      just(1, 2, 3).foldMap { _.toString } must equal("123")

      just(1, 2, 3).foldLeftM(0)((acc, v) => (acc + v).some) must equal(6.some)

      just(1, 2, 3).suml must equal(6)

      just(1, 2, 3).∀(_ > 0) must equal(true)

      just(1, 2, 3).∃(_ > 2) must equal(true)

      just(1, 2, 3).traverse(x => (x + 1).some) must equal(just(2, 3, 4).some)

      just(1.some, 2.some).sequence must equal(just(1, 2).some)
    }
  }

  "ObservableT[List,_]" should {

    type ObservableTList[A] = ObservableT[List, A]

    // simple contructor
    // ObservableT(just(a1,a2,...)::Nil)
    def justT[A](a: A*) = ObservableT.just[List, A](a: _*)

    // We can lift List monad to Observable by using ObservableT.
    // liftT(1,2) => ObservableT(just(1)::just(2)::Nil)
    def liftT[A](as: A*) = List(as: _*).liftM[ObservableT]

    "be used as a composit Monoid of List and Observable" in {
      val ans = ObservableT(just(1, 4) :: just(1, 5) :: just(2, 4) :: just(2, 5) :: Nil)

      (liftT(1, 2) |+| liftT(4, 5)) must equal(ans)

      (liftT(1, 2) ⊹ liftT(4, 5)) must equal(ans)

      mzero[ObservableT[List, Int]] must equal(ObservableT.empty[List, Int])
    }

    "be used as a composit Functor of List and Observable" in {
      liftT(1, 2) ∘ {
        _ + 1
      } must equal(liftT(2, 3))

      (liftT(1, 2) >| 5) must equal(liftT(5, 5))

      (liftT(1, 2) as 4) must equal(liftT(4, 4))

      liftT(1, 2).fpair must equal(liftT((1, 1), (2, 2)))

      liftT(1, 2).fproduct {
        _ + 1
      } must equal(liftT((1, 2), (2, 3)))

      liftT(1, 2).strengthL("x") must equal(liftT(("x", 1), ("x", 2)))

      liftT(1, 2).strengthR("x") must equal(liftT((1, "x"), (2, "x")))

      Functor[ObservableTList].lift {
        (_: Int) + 1
      }(liftT(1, 2)) must equal(liftT(2, 3))
    }

    "be used as a composit Applicative functor of List and Observable" in {
      1.point[ObservableTList] must equal(liftT(1))

      1.η[ObservableTList] must equal(liftT(1))

      (liftT(1, 2) |@| liftT(3, 4)) {
        _ + _
      } must equal(liftT(4, 5, 5, 6))

      (liftT(1) <*> {
        (_: Int) + 1
      }.η[ObservableTList]) must equal(liftT(2))

      liftT(1) <*> {
        liftT(2) <*> {
          (_: Int) + (_: Int)
        }.curried.η[ObservableTList]
      } must equal(liftT(3))

      liftT(1, 2) <* liftT(3, 4) must equal(liftT(1, 1, 2, 2))

      liftT(1, 2) *> liftT(3, 4) must equal(liftT(3, 4, 3, 4))

      Apply[ObservableTList].ap(liftT(2)) {
        {
          (_: Int) + 3
        }.η[ObservableTList]
      } must equal(liftT(5))

      Apply[ObservableTList].lift2 {
        (_: Int) * (_: Int)
      }(liftT(1, 2), liftT(3, 4)) must equal(liftT(3, 4, 6, 8))
    }

    "be used as a composit Monad and MonadPlus functor of List and Observable" in {
      (liftT(1, 2) >>= { i =>
        justT(i + 1, i + 2)
      }) must equal(ObservableT(just(2, 3) :: just(3, 4) :: Nil))

      // List[Observable[_]] can be treated as composit Monad.
      (for {
        i <- liftT(1, 2)
        k <- List(i + 1, i + 2).liftM[ObservableT] // we can lift List to List[Observable[_]]
      } yield k) must equal(ObservableT(just(2) :: just(3) :: just(3) :: just(4) :: Nil))

      (liftT(4, 3) >> liftT(2, 1)) must equal(liftT(2, 1, 2, 1))

      liftT(liftT(1, 2), liftT(3, 4)).μ must equal(liftT(1, 2, 3, 4))

      (liftT(1, 2) <+> liftT(3, 4)) must equal(ObservableT(just(1, 3) :: just(1, 4) :: just(2, 3) :: just(2, 4) :: Nil))

      PlusEmpty[ObservableTList].empty[Int] must equal(ObservableT.empty[List, Int])
    }
  }

  "More practical examples" should {
    "be showed with ObservableT[Option,_]" in {
      val ob1 = just(1, 2, 3, 4)
      val map = Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40)
      (for {
        i <- ObservableT(Option(ob1)) //Option[Observable[Int]]
        j <- map.get(i).liftM[ObservableT] //Option can be lift to Option[Observable[Int]]
      } yield (i, j + 1)) must equal(ObservableT(Option(just((1, 11), (2, 21), (3, 31), (4, 41)))))
    }
  }
}
