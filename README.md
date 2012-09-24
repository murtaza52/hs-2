# hs-2

A parsing app ....

1) Enter a valid url http://github.com to fetch a url. (Plain domain names such as 'github.com' results in an unhandled exception. Also a non existent url such as http://githuuub.com results in an unhandled exception.)

2) Any url with a non http scheme (other than http or https) will result in an error message. llo://github.com will cause an error message to be shown.

3) If the content returned is not html then an error message will be displayed. ex http://feeds.washingtonpost.com/rss/politics

4) If an url with the same domain name is requested more than twice in 5 secs, then the fetching is throttled - the url is fetched after a delay of 5 secs on the third attempt. (There is a known bug which affects the fetching on the fourth attempt)

5) A defdomain function can be defined with a prohibited domain, and all requests to such a domain will be blocked. ex http://prohib2.com

6) lein2 midje results in running of a midje test.


## Usage

clone the repo

lein2 deps

lein2 ring sever

## License

Copyright © 2012 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
