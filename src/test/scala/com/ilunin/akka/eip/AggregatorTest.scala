package com.ilunin.akka.eip

import org.scalatest.{WordSpecLike, Matchers}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import com.ilunin.akka.eip.Aggregator.{AggregationDone, Aggregated, Complete}
import akka.actor.ActorSystem
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

/**
 * @author ddelautre
 * @since 1.0
 */
class AggregatorTest extends TestKit(ActorSystem("testSystem")) with ImplicitSender with WordSpecLike with Matchers with MockitoSugar {

  private val test1 = "test1"
  private val test2 = "test2"
  private val test3 = "test3"
  private val test4 = "test4"

  "An Aggregator" when {
    s"receiving $test1" should {
      "send the Aggregated message to its sender" in new SimpleAggregator {
        aggregator ! test1
        expectMsg(Aggregated)
      }
    }

    "receiving the Complete message" should {
      "send the AggregationDone message containing Nil to its sender" in new SimpleAggregator {
        aggregator ! Complete
        expectMsg(AggregationDone(Nil))
      }
    }

    s"receiving $test1 followed by the Complete message" should {
      s"send Aggregated followed by the AggregationDone message containing List($test1) to its sender" in new SimpleAggregator {
        aggregator ! test1
        expectMsg(Aggregated)
        aggregator ! Complete
        expectMsg(AggregationDone(List(test1)))
      }
    }
  }

  "An Aggregator initialized with a completion size of 2" when {
    s"receiving $test1" should {
      "send the Aggregated message to its sender" in new CompletionSizeAggregator {
        aggregator ! test1
        expectMsg(Aggregated)
      }
    }

    "receiving the Complete message" should {
      "send the AggregationDone message containing Nil to its sender" in new CompletionSizeAggregator {
        aggregator ! Complete
        expectMsg(AggregationDone(Nil))
      }
    }

    s"receiving $test1 followed by $test2" should {
      s"send Aggregated followed by the AggregationDone message containing List($test2, $test1) to its sender" in new CompletionSizeAggregator {
        aggregator ! test1
        expectMsg(Aggregated)
        aggregator ! test2
        expectMsg(AggregationDone(List(test2, test1)))
      }
    }

    s"receiving $test1 followed by $test2, $test3 and $test4" should {
      s"send Aggregated, AggregationDone containing List($test2, $test1), Aggregated and finally AggregationDone containing List($test4, $test3) to its sender" in new CompletionSizeAggregator {
        aggregator ! test1
        expectMsg(Aggregated)
        aggregator ! test2
        expectMsg(AggregationDone(List(test2, test1)))
        aggregator ! test3
        expectMsg(Aggregated)
        aggregator ! test4
        expectMsg(AggregationDone(List(test4, test3)))
      }
    }

    s"receiving $test1 followed by Complete, $test2 and $test3" should {
      s"send Aggregated, AggregationDone containing List($test1), Aggregated and finally AggregationDone containing List($test3, $test2) to its sender" in new CompletionSizeAggregator {
        aggregator ! test1
        expectMsg(Aggregated)
        aggregator ! Complete
        expectMsg(AggregationDone(List(test1)))
        aggregator ! test2
        expectMsg(Aggregated)
        aggregator ! test3
        expectMsg(AggregationDone(List(test3, test2)))
      }
    }
  }

  "An Aggregator initialized with a completion predicate that always return false" when {
    s"receiving $test1" should {
      "send the Aggregated message to its sender" in new CompletionPredicateAggregator {
        when(predicate(test1, List(test1), 1)) thenReturn false
        aggregator ! test1
        verify(predicate).apply(test1, List(test1), 1)
        expectMsg(Aggregated)
      }
    }

    "receiving the Complete message" should {
      "send the AggregationDone message containing Nil to its sender" in new SimpleAggregator {
        aggregator ! Complete
        expectMsg(AggregationDone(Nil))
      }
    }
  }

  "An Aggregator initialized with a completion predicate that always return true" when {
    s"receiving $test1" should {
      "send the AggregationDone message containing List(test1) to its sender" in new CompletionPredicateAggregator {
        when(predicate(test1, List(test1), 1)) thenReturn true
        aggregator ! test1
        verify(predicate).apply(test1, List(test1), 1)
        expectMsg(AggregationDone(List(test1)))
      }
    }
  }

  trait SimpleAggregator {
    val aggregator = TestActorRef(Aggregator.props(Nil, (s: String, l: List[String]) => s :: l))
  }

  trait CompletionSizeAggregator {
    val aggregator = TestActorRef(Aggregator.props(Nil, (s: String, l: List[String]) => s :: l, 2))
  }

  trait CompletionPredicateAggregator {
    val predicate = mock[(String, List[String], Int) => Boolean]
    val aggregator = TestActorRef(Aggregator.props(Nil, (s: String, l: List[String]) => s :: l, predicate))
  }

}


