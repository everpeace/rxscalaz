# rxscalaz [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/everpeace/rxscalaz?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This provides some useful type class instances for `Observable`.  Therefore you can apply scalaz's fancy operators to `Observable`.

Provided type class instances are `Monoid`, `Monad`, `MonadPlus`, `Traverse`, `Foldable`, etc.

This also provides `ObservableT` monad transformer for `Observable`.  Therefore you can compose arbitrary monad with `Observable` monad.

For QuickStart, please refer to [RxScalazDemo](./src/test/scala/rx/lang/scala/scalaz/examples/RxScalazDemo.scala).

## How to use

```scala
import scalaz._, Scalaz._
import rx.lang.scala.Observable
import rx.lang.scala.scalaz._

Observable.just(1, 2) |+| Observable.just(3, 4)             // == Observable.just(1 2 3 4)
Observable.just(1, 2) ∘ {_ + 1}                              // == Observable.just(2, 3)
(Observable.just(1, 2) |@| Observable.just(3, 4)) {_ + _}   // == Observable.just(4, 5, 5, 6)
1.η[Observable]                                              // == Observable.just(1)
(Observable.just(3) >>= {(i: Int) => Observable.just(i + 1)}) // Observable.just(4)
```

Some other useful operators are available.  Please see below for details.

## Provided Typeclass Instances for Observable
### Monoid
`Observable` obviously forms a monoid interms of  [`concat`](https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#concat).

```scala
(Observable.just(1, 2) |+| Observable.just(3, 4)) === Observable.just(1, 2, 3, 4)
(Observable.just(1, 2) ⊹ Observable.just(3, 4)) === Observable.just(1, 2, 3, 4)
mzero[Observable[Int]] === Observable.empty
```

### Monad, MonadPlus
Essentially, `Observable` is similar to `Stream`. So, `Observable` can be a Stream-like `Monad` and can be a `MonadPlus` as well as `Monoid`.  Of course, `Observable` can be also `Functor` and `Applicative`.

```scala
// Functor operators
(Observable.just(1, 2) ∘ {_ + 1}) === Observable.just(2, 3)
(Observable.just(1, 2) >| 5) === Observable.just(5, 5)
Observable.just(1, 2).fpair === Observable.just((1, 1), (2, 2))
Observable.just(1, 2).fproduct {_ + 1} === Observable.just((1, 2), (2, 3))
Observable.just(1, 2).strengthL("x") === Observable.just(("x", 1), ("x", 2))
Observable.just(1, 2).strengthR("x") === Observable.just((1, "x"), (2, "x"))
Functor[Observable].lift {(_: Int) + 1}(Observable.just(1, 2)) === Observable.just(2, 3)

// Applicative operators
1.point[Observable] === Observable.just(1)
1.η[Observable] === Observable.just(1)
(Observable.just(1, 2) |@| Observable.just(3, 4)) {_ + _} === Observable.just(4, 5, 5, 6)
(Observable.just(1) <*> {(_: Int) + 1}.η[Observable]) === Observable.just(2)
Observable.just(1) <* Observable.just(2) === Observable.just(1)
Observable.just(1) *> Observable.just(2) === Observable.just(2)

// Monad and MonadPlus operators
(Observable.just(3) >>= {(i: Int) => Observable.just(i + 1)}) === Observable.just(4)
Observable.just(3) >> Observable.just(2) === Observable.just(2)
Observable.just(Observable.just(1, 2), Observable.just(3, 4)).μ === Observable.just(1, 2, 3, 4)
Observable.just(1, 2) <+> Observable.just(3, 4) === Observable.just(1, 2, 3, 4)
```

### Traverse and Foldable
`Observable` can be `Traverse` and `Foldable` as well as `Stream`.  This means you can fold `Observable` instance to single value.

** CAUTION: ** Most of traverse operators are **blocking** operators.

```scala
Observable.just(1, 2, 3).foldMap {_.toString} === "123"
Observable.just(1, 2, 3).foldLeftM(0)((acc, v) => (acc * v).some) === 6.some
Observable.just(1, 2, 3).suml === 6
Observable.just(1, 2, 3).∀(_ > 3) === true
Observable.just(1, 2, 3).∃(_ > 3) === false
Observable.just(1, 2, 3).traverse(x => (x + 1).some) === Observable.just(2, 3, 4).some
Observable.just(1.some, 2.some).sequence === Observable.just(1, 2).some
```

## Monad Transformer for Observable.
This module provides `ObservableT` monad transformer.  You can compose Observable monad with arbitrary monad.

** CAUTION: ** for any monad `M`, `bind (>>=, flatMap)` of `Observable[M,_]`(essentially `M[Observable[_]]`) are **blocking** operators.

### Example: `ObservableT[List,_]`(essentially `List[Observable[_]])`) can be used as a composit monad.

```scala
import scalaz._, Scalaz._
import rx.lang.scala.Observable._
import rx.lang.scala.scalaz._

val ob1 = just(1,2,3,4)
val map = Map(1 -> 10, 2 -> 20, 3 -> 30, 4 -> 40)
(for {
  i <- ObservableT(Option(ob1))
  j <- map.get(i).liftM[ObservableT]
} yield (i,j+1)) === ObservableT(Option(just((1,11),(2,21),(3,31),(4,41))))

```
