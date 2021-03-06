package redis

import akka.util.ByteString
import redis.protocol._
import scala.util.Try
import scala.annotation.tailrec
import scala.collection.mutable

trait RedisValueConverter[A] {
  def from(a: A): ByteString
}

trait MultiBulkConverter[A] {
  def to(redisReply: MultiBulk): Try[A]
}

object Converter {

  implicit object StringConverter extends RedisValueConverter[String] {
    def from(s: String): ByteString = ByteString(s)
  }

  implicit object ByteStringConverter extends RedisValueConverter[ByteString] {
    def from(bs: ByteString): ByteString = bs
  }

  implicit object SeqStringMultiBulkConverter extends MultiBulkConverter[Seq[String]] {
    def to(reply: MultiBulk): Try[Seq[String]] = Try(reply.responses.map(r => {
      r.map(_.toString)
    }).get)
  }

  implicit object SeqByteStringMultiBulkConverter extends MultiBulkConverter[Seq[ByteString]] {
    def to(reply: MultiBulk): Try[Seq[ByteString]] = Try(reply.responses.map(r => {
      r.map(_.toByteString)
    }).get)
  }

  implicit object SeqOptionByteStringMultiBulkConverter extends MultiBulkConverter[Seq[Option[ByteString]]] {
    def to(reply: MultiBulk): Try[Seq[Option[ByteString]]] = Try(reply.responses.map(r => {
      r.map(_.asOptByteString)
    }).get)
  }

  implicit object MapStringByteStringMultiBulkConverter extends MultiBulkConverter[Map[String, ByteString]] {
    def to(reply: MultiBulk): Try[Map[String, ByteString]] = Try(reply.responses.map(r => {
      val seq = r.map(_.toByteString)
      seqToMap(seq)
    }).get)

    def seqToMap(s: Seq[ByteString]): Map[String, ByteString] = {
      require(s.length % 2 == 0, "odd number of elements")
      @tailrec
      def recur(s: Seq[ByteString], acc: Map[String, ByteString]): Map[String, ByteString] = {
        if (s.isEmpty) {
          acc
        } else {
          recur(s.tail.tail, acc + (s.head.utf8String -> s.tail.head))
        }
      }
      recur(s, Map.empty[String, ByteString])
    }
  }

  implicit object SeqByteStringDoubleMultiBulkConverter extends MultiBulkConverter[Seq[(ByteString, Double)]] {
    def to(reply: MultiBulk): Try[Seq[(ByteString, Double)]] = Try(reply.responses.map(r => {
      val seq = r.map(_.toByteString)
      seqToSeqTuple2(seq)
    }).get)

    def seqToSeqTuple2(s: Seq[ByteString]): Seq[(ByteString, Double)] = {
      require(s.length % 2 == 0, "odd number of elements")
      val queue = mutable.Queue[(ByteString, Double)]()
      @tailrec
      def recur(s: Seq[ByteString]) {
        if (s.nonEmpty) {
          val score = java.lang.Double.valueOf(s.tail.head.utf8String)
          queue.enqueue((s.head, score))
          recur(s.tail.tail)
        }
      }
      recur(s)
      queue.toSeq
    }
  }

}
