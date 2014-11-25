package com.paulasmuth.sqltap.stats


import com.hootsuite.statsd.StatsdClient
import com.hootsuite.statsd.handlers.{NoopStatsdClient, UdpStatsdClient}
import com.typesafe.config.Config


object StatsdFactory {

  def apply(config: Config): StatsdClient = {
    val statsDconfig = config.hasPath("statsd")
    if(statsDconfig) {
      new UdpStatsdClient(config)
    }
    else {
      new NoopStatsdClient
    }
  }

}
