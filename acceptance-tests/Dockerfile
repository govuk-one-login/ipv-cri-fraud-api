FROM amazonlinux:2

RUN yum install -y awscli

COPY run-tests.sh ./

ENTRYPOINT ["/run-tests.sh"]
