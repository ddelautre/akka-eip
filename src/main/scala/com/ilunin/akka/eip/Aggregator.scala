package com.ilunin.akka.eip

import akka.actor.{Props, Actor}
import scala.reflect.ClassTag
import com.ilunin.akka.eip.Aggregator.{Aggregated, AggregationDone, Complete}

/**
 * @author ddelautre
 * @since 1.0
 */
class Aggregator[M: ClassTag, A](aggregationStrategy: (M, A) => A,
                                 initialAgrgegationValue: A,
                                 completionSize: Option[Int] = None) extends Actor {

  def this(aggregationStrategy: (M, A) => A, aggregation: A, completionSize: Int) = this(aggregationStrategy, aggregation, Some(completionSize))

  private var count = 0

  private var aggregation = initialAgrgegationValue

  def receive = {
    case message: M => aggregate(message)
    case Complete => complete()
  }

  private def aggregate(message: M) {
    aggregation = aggregationStrategy(message, aggregation)
    count += 1
    val completed = completionSize.exists(count == _)
    if (completed) {
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
    aggregation = initialAgrgegationValue
    count = 0
  }
}

object Aggregator {

  case object Complete

  sealed trait AggregatorResult

  case object Aggregated extends AggregatorResult

  case class AggregationDone[A](aggregation: A) extends AggregatorResult

  def props[M: ClassTag, A](aggregation: A, aggregationStrategy: (M, A) => A) = Props(new Aggregator(aggregationStrategy, aggregation))

  def props[M: ClassTag, A](aggregation: A, aggregationStrategy: (M, A) => A, completionSize: Int) = Props(new Aggregator(aggregationStrategy, aggregation, completionSize))

}
