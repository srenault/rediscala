package redis.commands

import akka.util.{ByteString, Timeout}
import redis.{RedisValueConverter, Request}
import scala.concurrent.{ExecutionContext, Future}
import redis.protocol.{Bulk, Status}

trait Connection extends Request {
  def auth[A](value: A)(implicit convert: RedisValueConverter[A], timeout: Timeout, ec: ExecutionContext): Future[Status] =
    send("AUTH", Seq(convert.from(value))).mapTo[Status]

  def echo[A](value: A)(implicit convert: RedisValueConverter[A], timeout: Timeout, ec: ExecutionContext): Future[Option[ByteString]] =
    send("ECHO", Seq(convert.from(value))).mapTo[Bulk].map(_.response)

  def ping()(implicit timeout: Timeout, ec: ExecutionContext): Future[String] =
    send("PING").mapTo[Status].map(_.toString)

  // commands sent after will fail with [[redis.protocol.NoConnectionException]]
  def quit()(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("QUIT").mapTo[Status].map(_.toBoolean)

  def select(index: Int)(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("SELECT", Seq(ByteString(index.toString))).mapTo[Status].map(_.toBoolean)
}
