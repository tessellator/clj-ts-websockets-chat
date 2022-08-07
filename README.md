# CLJ+TS WebSocket Chat

This is a sample project that implements a websocket chat application with a
Clojure/ring/Jetty backend and a TS/React frontend.

## Development

You will need both node and the clojure CLI tools installed for development on
this project. When both are installed, clone this repo and run
`npm install && npm run dev`. This command will run both the backend and the
frontend development server. Open your browser to http://localhost:5173 to see
the app.

## Production

This project provides a Dockerfile for building and running a production build
of the software. The docker exposes the service and frontend over port 8080.

Note that this software is not intended to be used in production. The Dockerfile
exists as a sample for other projects.
