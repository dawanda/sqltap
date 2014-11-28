// This file is part of the "SQLTap" project
//   (c) 2011-2013 Paul Asmuth <paul@paulasmuth.com>
//
// Licensed under the MIT License (the "License"); you may not use this
// file except in compliance with the License. You may obtain a copy of
// the License at: http://opensource.org/licenses/MIT

package com.paulasmuth.sqltap.cache

import com.paulasmuth.sqltap.buffers.ElasticBuffer
import com.typesafe.scalalogging.StrictLogging
import scala.collection.mutable.{HashMap}

// STUB!
class StubCache extends CacheBackend with StrictLogging {

  val stubcache = new HashMap[String,ElasticBuffer]()

  def connect() : Unit = ()

  def execute(requests: List[CacheRequest]) = {
    for (req <- requests) {
      req match {
        case get: CacheGetRequest => {
          logger.debug("[CACHE] retrieve: " + req.key)
          stubcache.get(req.key) match {
            case Some(buf:  ElasticBuffer) => {
              get.buffer = buf.clone()
            }
            case None => ()
          }
        }
        case set: CacheStoreRequest => {
          logger.debug("[CACHE] store: " + req.key)
          stubcache.put(req.key, set.buffer)
        }
        case purge: CachePurgeRequest => {
          logger.debug("[CACHE] purge: " + req.key)
          stubcache.remove(req.key)
        }
      }

      req.ready()
    }
  }

}
