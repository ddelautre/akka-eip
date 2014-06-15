package com.ilunin.akka.eip

import org.scalatest.{WordSpecLike, Matchers}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import com.ilunin.akka.eip.Aggregator.{AggregationDone, Aggregated, Complete}
import akka.actor.ActorSystem

/**
 * @author ddelautre
 * @since 1.0
 */
class AggregatorTest extends TestKit(ActorSystem("testSystem")) with ImplicitSender with WordSpecLike with Matchers {

  "An Aggregator initialized with Nil and an aggregation strategy that adds a String to a List" when {
    "receiving a String" should {
      "send the Aggregated message to its sender" in {
        val aggregator = TestActorRef(new Aggregator[String, List[String]](_ :: _, Nil))
        aggregator ! "test"
        expectMsg(Aggregated)
      }
    }

    "receiving the Complete message" should {
      "send the AggregationDone message containing Nil to its sender" in {
        val aggregator = TestActorRef(new Aggregator[String, List[String]](_ :: _, Nil))
        aggregator ! Complete
        expectMsg(AggregationDone(Nil))
      }
    }

    """receiving "test" followed by the Complete message""" should {
      """send the AggregationDone message containing List("test") to its sender""" in {
        val aggregator = TestActorRef(new Aggregator[String, List[String]](_ :: _, Nil))
        aggregator ! "test"
        expectMsg(Aggregated)
        aggregator ! Complete
        expectMsg(AggregationDone(List("test")))
      }
    }
  }

}
