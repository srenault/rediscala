package redis.commands

import akka.util.{ByteString, Timeout}
import redis.{MultiBulkConverter, Request}
import scala.concurrent.{ExecutionContext, Future}
import redis.protocol._
import redis.protocol.Integer
import redis.protocol.Status
import redis.protocol.Bulk
import scala.util.Try

trait Server extends Request {

  def bgrewriteaof[A]()(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("BGREWRITEAOF").mapTo[Status].map(_.toBoolean)

  def bgsave()(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("BGSAVE").mapTo[Status].map(_.toBoolean)

  def clientKill(ip: String, port: Int)(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("CLIENT KILL", Seq(ByteString(ip), ByteString(port.toString))).mapTo[Status].map(_.toBoolean)

  def clientList()(implicit timeout: Timeout, ec: ExecutionContext): Future[String] =
    send("CLIENT LIST").mapTo[Bulk].map(_.toString)

  def clientGetname()(implicit timeout: Timeout, ec: ExecutionContext): Future[Option[String]] =
    send("CLIENT GETNAME").mapTo[Bulk].map(_.toOptString)

  def clientSetname(connectionName: String)(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("CLIENT SETNAME", Seq(ByteString(connectionName))).mapTo[Status].map(_.toBoolean)

  def configGet(parameter: String)(implicit timeout: Timeout, ec: ExecutionContext): Future[Option[String]] =
    send("CONFIG GET", Seq(ByteString(parameter))).mapTo[Bulk].map(_.toOptString)

  def configSet(parameter: String, value: String)(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("CONFIG SET", Seq(ByteString(parameter), ByteString(value))).mapTo[Status].map(_.toBoolean)

  def configResetstat(parameter: String, value: String)(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("CONFIG RESETSTAT").mapTo[Status].map(_.toBoolean)

  def dbsize()(implicit timeout: Timeout, ec: ExecutionContext): Future[Long] =
    send("DBSIZE").mapTo[Integer].map(_.toLong)

  def debugObject(key: String)(implicit timeout: Timeout, ec: ExecutionContext): Future[ByteString] =
    send("DEBUG OBJECT", Seq(ByteString(key))).mapTo[Status].map(_.toByteString)

  def debugSegfault()(implicit timeout: Timeout, ec: ExecutionContext): Future[ByteString] =
    send("DEBUG SEGFAULT").mapTo[Status].map(_.toByteString)

  def flushall()(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("FLUSHALL").mapTo[Status].map(_.toBoolean)

  def flushdb()(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("FLUSHDB").mapTo[Status].map(_.toBoolean)

  def info()(implicit timeout: Timeout, ec: ExecutionContext): Future[String] =
    send("INFO").mapTo[Bulk].map(_.toString)

  def info(section: String)(implicit timeout: Timeout, ec: ExecutionContext): Future[String] =
    send("INFO", Seq(ByteString(section))).mapTo[Bulk].map(_.toString)

  def lastsave()(implicit timeout: Timeout, ec: ExecutionContext): Future[Long] =
    send("LASTSAVE").mapTo[Integer].map(_.toLong)

  def monitor()(implicit timeout: Timeout, ec: ExecutionContext): Future[Long] = ??? // TODO blocking!

  def save()(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("SAVE").mapTo[Status].map(_.toBoolean)

  def shutdown()(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("SHUTDOWN").mapTo[Status].map(_.toBoolean)

  // timeout on success LOL
  def shutdown(modifier: ShutdownModifier)(implicit timeout: Timeout, ec: ExecutionContext): Future[Boolean] =
    send("SHUTDOWN", Seq(ByteString(modifier.toString))).mapTo[Status].map(_.toBoolean)

  def slaveof(host: String, port: Int)(implicit timeout: Timeout, ec: ExecutionContext): Future[String] =
    send("SLAVEOF", Seq(ByteString(host), ByteString(port.toString))).mapTo[Status].map(_.toString)

  def slowlog(subcommand: String, argument: String)(implicit timeout: Timeout, ec: ExecutionContext): Future[String] =
    send("SLOWLOG", Seq(ByteString(subcommand), ByteString(argument))).mapTo[Status].map(_.toString)

  def sync()(implicit timeout: Timeout, ec: ExecutionContext): Future[RedisReply] =
    send("SYNC").mapTo[RedisReply]

  def time()(implicit convert: MultiBulkConverter[Seq[ByteString]], timeout: Timeout, ec: ExecutionContext): Future[Try[Seq[ByteString]]] =
    send("TIME").mapTo[MultiBulk].map(_.asTry[Seq[ByteString]])

}

sealed trait ShutdownModifier

case object NOSAVE extends ShutdownModifier

case object SAVE extends ShutdownModifier