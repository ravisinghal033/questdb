# Use the official Debian base image
FROM debian:bookworm AS builder

# Define build arguments
ARG tag_name

# Set environment variables
ENV GOSU_VERSION=1.14
ENV JDK_VERSION=17.0.11.9-1
ENV JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto

# Update package list and install necessary tools
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
    git curl wget gnupg2 ca-certificates lsb-release software-properties-common unzip

# Add the Amazon Corretto GPG key and repository, then install JDK and Maven
RUN wget -O - https://apt.corretto.aws/corretto.key | gpg --dearmor -o /usr/share/keyrings/corretto-keyring.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/corretto-keyring.gpg] https://apt.corretto.aws stable main" | tee /etc/apt/sources.list.d/corretto.list && \
    apt-get update && \
    apt-get install --no-install-recommends -y java-17-amazon-corretto-jdk=1:${JDK_VERSION} maven

# Set the working directory for the build
WORKDIR /build

# Echo the tag_name for debugging purposes
RUN echo "Building with tag_name: ${tag_name:-master}"

# Clone the QuestDB repository
RUN git clone --depth=1 --branch "${tag_name:-master}" https://github.com/questdb/questdb.git

# Set the working directory to the cloned repository
WORKDIR /build/questdb

# Build QuestDB with Maven
RUN mvn clean package -Djdk.lang.Process.launchMechanism=vfork \
    -Dmaven.resolver.transport=wagon \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=30 \
    -DskipTests \
    -P build-web-console,build-binaries

# Extract the built tarball
WORKDIR /build/questdb/core/target
RUN tar xvfz questdb-*-rt-*.tar.gz && rm questdb-*-rt-*.tar.gz

# Download and verify gosu
RUN dpkgArch="$(dpkg --print-architecture | awk -F- '{ print $NF }')" && \
    wget -O gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch" && \
    wget -O gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch.asc" && \
    export GNUPGHOME="$(mktemp -d)" && \
    gpg --batch --keyserver hkps://keys.openpgp.org --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 && \
    gpg --batch --verify gosu.asc gosu && \
    gpgconf --kill all && \
    rm -rf "$GNUPGHOME" gosu.asc && \
    chmod +x gosu && \
    ./gosu --version && \
    ./gosu nobody true

# Use a slim version of the Debian base image for the final stage
FROM debian:bookworm-slim

# Set the working directory for the final image
WORKDIR /app

# Copy the built QuestDB artifacts and gosu from the builder stage
COPY --from=builder /build/questdb/core/target/questdb-*-rt-* .
COPY --from=builder /build/questdb/core/target/gosu /usr/local/bin/gosu

# Copy the entrypoint script
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Create questdb user and group
RUN groupadd -g 10001 questdb && \
    useradd -u 10001 -g 10001 -d /var/lib/questdb -M -s /sbin/nologin questdb && \
    mkdir -p /var/lib/questdb && \
    chown -R questdb:questdb /var/lib/questdb

# Set the working directory for QuestDB data
WORKDIR /var/lib/questdb

# Expose necessary ports
EXPOSE 9000/tcp
EXPOSE 8812/tcp
EXPOSE 9009/tcp

# Define the entrypoint
ENTRYPOINT ["/docker-entrypoint.sh"]
