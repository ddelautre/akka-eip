package com.ilunin.akka.eip

import org.scalatest.{WordSpecLike, Matchers}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import com.ilunin.akka.eip.Aggregator.{Aggregation, Aggregated, AggregationDone}
import akka.actor.ActorSystem

/**
 * Created by ddelautre on 2014-06-14.
 */
class AggregatorTest extends TestKit(ActorSystem("testSystem")) with ImplicitSender with WordSpecLike with Matchers {

  "An Aggregator initialized with Nil and an aggregation strategy that adds a String to a List" when {
    """receiving "test"""" should {
      "send the Aggregated message to its sender" in {
        val aggregator = TestActorRef(new Aggregator[String, List[String]](_ :: _, Nil))
        aggregator ! "test"
        expectMsg(Aggregated)
      }
    }

    "receiving the AggregationDone message" should {
      "send the Aggregation message containing Nil to its sender" in {
        val aggregator = TestActorRef(new Aggregator[String, List[String]](_ :: _, Nil))
        aggregator ! AggregationDone
        expectMsg(Aggregation(Nil))
      }
    }

    """receiving "test" followed by the AggregationDone message""" should {
      """send the Aggregation message containing List("test") to its sender""" in {
        val aggregator = TestActorRef(new Aggregator[String, List[String]](_ :: _, Nil))
        aggregator ! "test"
        expectMsg(Aggregated)
        aggregator ! AggregationDone
        expectMsg(Aggregation(List("test")))
      }
    }
  }

}
