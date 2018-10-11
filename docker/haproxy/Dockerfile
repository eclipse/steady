FROM haproxy:alpine

LABEL maintainer="Antonino Sabetta antonino.sabetta@sap.com"

ADD conf/haproxy.cfg /usr/local/etc/haproxy/haproxy.cfg

CMD ["haproxy", "-f", "/usr/local/etc/haproxy/haproxy.cfg"]
