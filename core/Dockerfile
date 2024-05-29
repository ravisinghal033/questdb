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
RUN git clone --depth=1 --branch "${tag_name:-master}" https://github.com/quest
