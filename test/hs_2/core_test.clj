(ns hs-2.core-test
  (:use midje.sweet)
  (:use hs-2.core))

(fact ((process-request){:url "llp://usee.com"}) =>
      {:error-msg "Boy dont you know how to write an HTTP scheme URL ? It starts with HTTP or HTTPS !", :url-scheme nil, :url "llp://usee.com"})
