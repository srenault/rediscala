package redis.commands

import redis._
import scala.concurrent.Await
import akka.util.ByteString
import scala.util.Success

class HashesSpec extends RedisSpec {

  import Converter._

  "Hashes commands" should {
    "HDEL" in {
      val r = for {
        _ <- redis.hset("hdelKey", "field", "value")
        d <- redis.hdel("hdelKey", "field", "fieldNonexisting")
      } yield {
        d mustEqual 1
      }
      Await.result(r, timeOut)
    }

    "HEXISTS" in {
      val r = for {
        _ <- redis.hset("hexistsKey", "field", "value")
        exist <- redis.hexists("hexistsKey", "field")
        notExist <- redis.hexists("hexistsKey", "fieldNotExisting")
      } yield {
        exist mustEqual true
        notExist mustEqual false
      }
      Await.result(r, timeOut)
    }

    "HGET" in {
      val r = for {
        _ <- redis.hset("hgetKey", "field", "value")
        get <- redis.hget("hgetKey", "field")
        get2 <- redis.hget("hgetKey", "fieldNotExisting")
      } yield {
        get mustEqual Some(ByteString("value"))
        get2 mustEqual None
      }
      Await.result(r, timeOut)
    }

    "HGETALL" in {
      val r = for {
        _ <- redis.hset("hgetallKey", "field", "value")
        get <- redis.hgetall("hgetallKey")
        get2 <- redis.hgetall("hgetallKeyNotExisting")
      } yield {
        get mustEqual Success(Map("field" -> ByteString("value")))
        get2 mustEqual Success(Map.empty)
      }
      Await.result(r, timeOut)
    }

    "HINCRBY" in {
      val r = for {
        _ <- redis.hset("hincrbyKey", "field", "10")
        i <- redis.hincrby("hincrbyKey", "field", 1)
        ii <- redis.hincrby("hincrbyKey", "field", -1)
      } yield {
        i mustEqual 11
        ii mustEqual 10
      }
      Await.result(r, timeOut)
    }

    "HINCRBYFLOAT" in {
      val r = for {
        _ <- redis.hset("hincrbyfloatKey", "field", "10.5")
        i <- redis.hincrbyfloat("hincrbyfloatKey", "field", 0.1)
        ii <- redis.hincrbyfloat("hincrbyfloatKey", "field", -1.1)
      } yield {
        i mustEqual 10.6
        ii mustEqual 9.5
      }
      Await.result(r, timeOut)
    }

    "HKEYS" in {
      val r = for {
        _ <- redis.hset("hkeysKey", "field", "value")
        keys <- redis.hkeys("hkeysKey")
      } yield {
        keys mustEqual Success(Seq("field"))
      }
      Await.result(r, timeOut)
    }

    "HLEN" in {
      val r = for {
        _ <- redis.hset("hkeysKey", "field", "value")
        hLength <- redis.hlen("hkeysKey")
      } yield {
        hLength mustEqual 1
      }
      Await.result(r, timeOut)
    }

    "HMGET" in {
      val r = for {
        _ <- redis.hset("hmgetKey", "field", "value")
        hmget <- redis.hmget("hmgetKey", "field", "nofield")
      } yield {
        hmget mustEqual Success(Seq(Some(ByteString("value")), None))
      }
      Await.result(r, timeOut)
    }

    "HMSET" in {
      val r = for {
        _ <- redis.hmset("hmsetKey", Map("field" -> "value1", "field2" -> "value2"))
        v1 <- redis.hget("hmsetKey", "field")
        v2 <- redis.hget("hmsetKey", "field2")
      } yield {
        v1 mustEqual Some(ByteString("value1"))
        v2 mustEqual Some(ByteString("value2"))
      }
      Await.result(r, timeOut)
    }

    "HMSET" in {
      val r = for {
        _ <- redis.hdel("hsetKey", "field")
        set <- redis.hset("hsetKey", "field", "value")
        update <- redis.hset("hsetKey", "field", "value2")
        v1 <- redis.hget("hsetKey", "field")
      } yield {
        set mustEqual true
        update mustEqual false
        v1 mustEqual Some(ByteString("value2"))
      }
      Await.result(r, timeOut)
    }

    "HMSETNX" in {
      val r = for {
        _ <- redis.hdel("hsetnxKey", "field")
        set <- redis.hsetnx("hsetnxKey", "field", "value")
        doNothing <- redis.hsetnx("hsetnxKey", "field", "value2")
        v1 <- redis.hget("hsetnxKey", "field")
      } yield {
        set mustEqual true
        doNothing mustEqual false
        v1 mustEqual Some(ByteString("value"))
      }
      Await.result(r, timeOut)
    }

    "HVALS" in {
      val r = for {
        _ <- redis.hdel("hvalsKey", "field")
        empty <- redis.hvals("hvalsKey")
        _ <- redis.hset("hsetnxKey", "field", "value")
        some <- redis.hvals("hsetnxKey")
      } yield {
        empty mustEqual Success(Seq())
        some mustEqual Success(Seq(ByteString("value")))
      }
      Await.result(r, timeOut)
    }
  }
}
