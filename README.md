# trumpet-server

A Clojure library designed to ... well, that part is up to you.

## Usage

1. Run `lein midje` to run all tests. (Run `lein midje :autotest :filter -it` to automatically run tests excluding unit tests)
2. Run `lein run` to start the server. (`lein ring server-headless` doesn't work when subscribing to SSE channel since it doesn't start async jetty).

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
