(ns hs-2.core-test
  (:use midje.sweet)
  (:use hs-2.core))

(fact ((process-request){:url "llp://usee.com"}) =>
      {:error-msg "The URL is not of http scheme. The URL should start either with http or https.", :url-scheme nil, :url "llp://usee.com"})