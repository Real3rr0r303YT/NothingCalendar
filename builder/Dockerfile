# CORE//BREACH builder: JDK 17 + Android SDK 35, ohne Android Studio
FROM eclipse-temurin:17-jdk-jammy

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    CMDLINE_VER=11076708
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip wget git ca-certificates \
 && rm -rf /var/lib/apt/lists/*

RUN mkdir -p $ANDROID_HOME/cmdline-tools \
 && wget -q https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_VER}_latest.zip -O /tmp/cl.zip \
 && unzip -q /tmp/cl.zip -d $ANDROID_HOME/cmdline-tools \
 && mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest \
 && rm /tmp/cl.zip

RUN yes | sdkmanager --licenses > /dev/null \
 && sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

WORKDIR /src
