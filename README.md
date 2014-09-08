# elefant-server

A Clojure library designed to act as an elefant server.

## Usage

1. Run `lein test` to run all midje tests. (test is just an alias for `lein midje`)
2. Run `lein utest :autotest` to run all midje unit tests. (utest is just an alias for `lein midje :filters -it`) 
3. Run `lein itest` to run all midje integration tests. (itest is just an alias for `lein midje :filters it`) 
4. Run `lein run` to start the server. (`lein ring server-headless` doesn't work when subscribing to SSE channel since it doesn't start async jetty).

Note that if you have troubles running e.g. `lein uberjar` comment out (`#_`) the `elefant-server.domain.sse-service/stale-subscribers-evictor` 
function since I haven't yet implemented proper shutdown of its thread-pool. 

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
