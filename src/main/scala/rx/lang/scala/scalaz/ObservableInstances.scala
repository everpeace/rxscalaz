package rx.lang.scala.scalaz

import rx.lang.scala.Observable

import scalaz.Tags.{ Zip => TZip }
import scalaz._

private[scalaz] trait ObservableInstances {
  // Monoid
  implicit def observableMonoid[A] = new Monoid[Observable[A]] {
    override def zero: Observable[A] = Observable.empty
    override def append(f1: Observable[A], f2: => Observable[A]): Observable[A] = f1 ++ f2
  }

  implicit val observableInstances = new Traverse[Observable] with IsEmpty[Observable] with Zip[Observable] with MonadPlus[Observable] {

    // Monad
    override def point[A](a: => A) = Observable.just(a)
    override def bind[A, B](oa: Observable[A])(f: (A) => Observable[B]) = oa.flatMap(f)

    // MonadPlus
    override def empty[A]: Observable[A] = observableMonoid[A].zero
    override def plus[A](a: Observable[A], b: => Observable[A]): Observable[A] = observableMonoid[A].append(a, b)

    // Zip
    override def zip[A, B](a: => Observable[A], b: => Observable[B]): Observable[(A, B)] = a zip b

    // IsEmpty (NOTE: This method is blocking call)
    override def isEmpty[A](fa: Observable[A]): Boolean = fa.isEmpty.toBlocking.first

    // Traverse (NOTE: This method is blocking call)
    override def traverseImpl[G[_], A, B](fa: Observable[A])(f: (A) => G[B])(implicit G: Applicative[G]): G[Observable[B]] = {
      val seed: G[Observable[B]] = G.point(Observable.empty)
      fa.foldLeft(seed) {
        (ys, x) => G.apply2(ys, f(x))((bs, b) => bs :+ b)
      }.toBlocking.first
    }
  }

  val observableZipApplicative: Applicative[({ type λ[α] = Observable[α] @@ TZip })#λ] = new Applicative[({ type λ[α] = Observable[α]@@ TZip })#λ] {
    def point[A](a: => A) = TZip(Observable.just(a).repeat)
    def ap[A, B](oa: => Observable[A] @@ TZip)(of: => Observable[A => B] @@ TZip) = TZip(Tag.unwrap(of).zip(Tag.unwrap(oa)) map { fa => fa._1(fa._2) })
  }
}
