package com.ilunin.akka.eip

import akka.actor.Actor
import scala.reflect.ClassTag
import com.ilunin.akka.eip.Aggregator.{Aggregated, Aggregation, AggregationDone}

/**
 * Created by ddelautre on 2014-06-11.
 */
class Aggregator[M: ClassTag, A](aggregationStrategy: (M, A) => A,
                                 private var aggregation: A) extends Actor {

  def receive = {
    case message: M =>
      aggregation = aggregationStrategy(message, aggregation)
      sender() ! Aggregated
    case AggregationDone => sender() ! Aggregation(aggregation)
  }

}

object Aggregator {

  case object AggregationDone

  sealed trait AggregatorResult

  case object Aggregated extends AggregatorResult

  case class Aggregation[A](aggregation: A) extends AggregatorResult

}
