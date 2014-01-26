import org.junit.Test;
import org.junit.Before;

import rx.Observer;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public class RxJavaCacheTest {

  Observable<Integer> observable;

  @Before
  public void setUp() {

    // Create a new observable that emits one integer on each subscribe.
    // The number indicates how many times the subscribe has been called.
    observable = Observable.create(new Observable.OnSubscribeFunc<Integer>() {
      private int counter = 0;

      @Override
      public Subscription onSubscribe(final Observer<? super Integer> observer) {
        // Give the next counter value synchronously.
        observer.onNext(counter++);

        // Normally it would be nice to complete the observable, but for
        // illustrative purposes we'll leave the subscriptions as non-terminating.
        // observer.onCompleted();

        // Return en empty subscription as this observer has already completed.
        // Normally in case of an async operation the caller would get a
        // possibility to unsubscribe.
        return Subscriptions.empty();
      } 
    });
  }

  @Test
  public void testSubscribeTwice() {
    Observer testObserver1 = mock(Observer.class);
    Observer testObserver2 = mock(Observer.class);

    observable.subscribe(testObserver1);
    observable.subscribe(testObserver2);

    verify(testObserver1).onNext(0);
    verify(testObserver2).onNext(1);
  }

  @Test
  public void testCache() {
    Observer testObserver1 = mock(Observer.class);
    Observer testObserver2 = mock(Observer.class);

    // Create a cached observable that saves all values it receives from
    // the original source and gives the forward to all of its subscribers.
    Observable<Integer> cachedObservable = observable.cache();

    cachedObservable.subscribe(testObserver1);
    cachedObservable.subscribe(testObserver2);

    verify(testObserver1).onNext(0);
    verify(testObserver2).onNext(0);


    // The original observable is still of course there:
    Observer testObserver3 = mock(Observer.class);

    observable.subscribe(testObserver3);

    verify(testObserver3).onNext(1);


    // The other two observers are connected to the cache, and
    // do not receive any values.
    verify(testObserver1, never()).onNext(1);
    verify(testObserver2, never()).onNext(1);
  }
}
