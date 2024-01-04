# The default deploy instructions (https://biffweb.com/docs/reference/production/) don't
# use Docker, but this file is provided in case you'd like to deploy with containers.
from clojure:temurin-17-tools-deps-bullseye

ENV BB_COMMIT=965c177bca31ae9882c975ef7db448e12f59984e
ENV TAILWIND_VERSION=v3.2.4

RUN apt-get update && apt-get install -y \
  curl default-jre \
  && rm -rf /var/lib/apt/lists/*
RUN curl -s https://raw.githubusercontent.com/babashka/babashka/$BB_COMMIT/install | bash
RUN curl -L -o /usr/local/bin/tailwindcss \
  https://github.com/tailwindlabs/tailwindcss/releases/download/$TAILWIND_VERSION/tailwindcss-linux-x64 \
  && chmod +x /usr/local/bin/tailwindcss

WORKDIR /app
COPY resources ./resources
COPY src ./src
COPY build ./build
COPY bb ./bb
COPY bb.edn config.edn deps.edn .

RUN bb uberjar && cp target/jar/app.jar . && rm -r target
RUN rm /usr/local/bin/tailwindcss && rm /usr/local/bin/bb

EXPOSE 8080

CMD ["/usr/bin/java", "-XX:-OmitStackTraceInFastThrow", "-XX:+CrashOnOutOfMemoryError", "-Duser.timezone=UTC", "-jar", "app.jar", "--port", "7888", "--middleware", "[cider.nrepl/cider-middleware,refactor-nrepl.middleware/wrap-refactor]"]
