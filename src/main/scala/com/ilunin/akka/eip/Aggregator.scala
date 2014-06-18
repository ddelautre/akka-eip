package com.ilunin.akka.eip

import akka.actor.{Props, Actor}
import scala.reflect.ClassTag
import com.ilunin.akka.eip.Aggregator.{Aggregated, AggregationDone, Complete}

/**
 * @author ddelautre
 * @since 1.0
 */
class Aggregator[M: ClassTag, A](aggregationStrategy: (M, A) => A,
                                 initialAggregationValue: A,
                                 completionPredicate: (M, A, Int) => Boolean) extends Actor {

  private var count = 0

  private var aggregation = initialAggregationValue

  def receive = {
    case message: M => aggregate(message)
    case Complete => complete()
  }

  private def aggregate(message: M) {
    aggregation = aggregationStrategy(message, aggregation)
    count += 1
    if (completionPredicate(message, aggregation, count)) {
      complete()
    } else {
      sender ! Aggregated
    }
  }

  private def complete(): Unit = {
    sender ! AggregationDone(aggregation)
    reset()
  }

  private def reset(): Unit = {
    aggregation = initialAggregationValue
    count = 0
  }
}

object Aggregator {

  case object Complete

  sealed trait AggregatorResult

  case object Aggregated extends AggregatorResult

  case class AggregationDone[A](aggregation: A) extends AggregatorResult

  def props[M: ClassTag, A](aggregation: A, aggregationStrategy: (M, A) => A, completionSize: Int) = Props(new Aggregator(aggregationStrategy, aggregation, (message: M, aggregation: A, count: Int) => completionSize == count))

  def props[M: ClassTag, A](aggregation: A, aggregationStrategy: (M, A) => A, completionPredicate: (M, A, Int) => Boolean = (_: M, _: A, _: Int) => false) = Props(new Aggregator(aggregationStrategy, aggregation, completionPredicate))

}
