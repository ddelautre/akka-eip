package com.ilunin.akka.eip

import akka.actor.Actor
import scala.reflect.ClassTag
import com.ilunin.akka.eip.Aggregator.{Aggregated, AggregationDone, Complete}

/**
 * @author ddelautre
 * @since 1.0
 */
class Aggregator[M: ClassTag, A](aggregationStrategy: (M, A) => A,
                                 private var aggregation: A) extends Actor {

  def receive = {
    case message: M =>
      aggregation = aggregationStrategy(message, aggregation)
      sender() ! Aggregated
    case Complete => sender() ! AggregationDone(aggregation)
  }

}

object Aggregator {

  case object Complete

  sealed trait AggregatorResult

  case object Aggregated extends AggregatorResult

  case class AggregationDone[A](aggregation: A) extends AggregatorResult

}
