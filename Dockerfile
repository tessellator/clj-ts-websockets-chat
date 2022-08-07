FROM --platform=linux/amd64 node:lts-alpine as app-builder

WORKDIR /home/app

COPY package.json package-lock.json .
RUN npm install ci

COPY tsconfig.json tsconfig.node.json vite.config.ts index.html .
COPY src/app src/app
RUN npm run build

## -----------------------------------------------------------------------------

FROM --platform=linux/amd64 clojure:openjdk-17-tools-deps-bullseye as builder

WORKDIR /home/app

COPY deps.edn .
RUN clojure -P -Sdeps '{:deps {uberdeps/uberdeps {:mvn/version "0.1.7"}}}'

COPY src/server src/server
COPY --from=app-builder /home/app/resources ./resources
RUN clojure -Sdeps '{:deps {uberdeps/uberdeps {:mvn/version "0.1.7"}}}' \
      -M -m uberdeps.uberjar --target "target/app.jar"

## -----------------------------------------------------------------------------

FROM --platform=linux/amd64 openjdk:17-alpine

EXPOSE 8080
CMD ["java", "-cp", "app.jar", "clojure.main", "-m", "server.core"]
COPY --from=builder /home/app/target/app.jar .