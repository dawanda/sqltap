// This file is part of the "SQLTap" project
//   (c) 2011-2013 Paul Asmuth <paul@paulasmuth.com>
//
// Licensed under the MIT License (the "License"); you may not use this
// file except in compliance with the License. You may obtain a copy of
// the License at: http://opensource.org/licenses/MIT

package com.paulasmuth.sqltap.http

import java.net.InetSocketAddress
import java.nio.channels.spi.SelectorProvider
import java.nio.channels.{SelectionKey, ServerSocketChannel}
import com.paulasmuth.sqltap.callbackhell.Worker
import com.paulasmuth.sqltap.stats.Statistics
import com.paulasmuth.sqltap.{Watchdog}
import com.typesafe.scalalogging.StrictLogging
import scala.collection.mutable.{ListBuffer}

class Server(num_workers : Int) extends StrictLogging {

  private val TICK = 500
  var workers = new ListBuffer[Worker]()

  private val watchdog = new Watchdog(this)
  private var seq      = 0
  private val loop     = SelectorProvider.provider().openSelector()
  private val ssock    = ServerSocketChannel.open()

  def run(port: Int) : Unit = {
    ssock.configureBlocking(false)
    ssock.socket().bind(new InetSocketAddress("0.0.0.0", port), 8192)
    ssock.register(loop, SelectionKey.OP_ACCEPT)

    while (true) {
      for (n <- (0 until (num_workers - workers.length)))
        start_worker()

      loop.select(TICK)

      try {
        watchdog.run()
      } catch {
        case e: Exception => {
          logger.error("error running watchdog: " + e.toString, e)
        }
      }

      val events = loop.selectedKeys().iterator()

      if (workers.size > 0) {
        while (events.hasNext) {
          next(events.next())
          events.remove()
        }
      } else {
        logger.info("[CRITICAL] no workers available, sleeping for 500ms")
        Thread.sleep(500)
      }
    }
  }

  private def next(event: SelectionKey) : Unit = {
    if (!event.isValid)
      return

    if (event.isAcceptable) {
      val conn = ssock.accept()

      seq = (seq + 1) % workers.size

      workers(seq).queue.add(conn)
      workers(seq).loop.wakeup()
      workers(seq).requests_queued.incrementAndGet()

      Statistics.incr('http_connections_open)
    }
  }

  private def start_worker() : Unit = {
    val worker = new Worker()
    worker.start()
    workers += worker
  }

}
