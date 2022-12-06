FROM gradle:jdk11
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && unzip awscliv2.zip && ./aws/install

RUN apt-get update
RUN apt-get install -y libglib2.0-0
RUN apt-get install -y libnss3
RUN apt-get install -y libglu1
RUN apt-get install -y libxcb1
RUN apt-get install -y libappindicator1 fonts-liberation
RUN apt-get -y install dbus-x11 xfonts-base xfonts-100dpi xfonts-75dpi xfonts-cyrillic xfonts-scalable
RUN apt-get -y install libxss1 lsb-release xdg-utils
RUN apt-get -y install jq

RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
RUN dpkg --configure -a
RUN dpkg -i --force-depends google-chrome-stable_current_amd64.deb
RUN apt-get install -fy

COPY . .
RUN mv run-tests.sh /run-tests.sh
ENTRYPOINT ["/run-tests.sh"]