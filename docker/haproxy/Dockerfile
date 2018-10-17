FROM haproxy:alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ADD conf/haproxy.cfg /usr/local/etc/haproxy/haproxy.cfg

CMD ["haproxy", "-f", "/usr/local/etc/haproxy/haproxy.cfg"]
