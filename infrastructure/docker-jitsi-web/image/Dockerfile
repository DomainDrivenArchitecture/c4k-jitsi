FROM jitsi/web:stable-6826

# Prepare Entrypoint Script
ADD resources /tmp
RUN /tmp/install.sh

ENTRYPOINT ["/entrypoint.sh"]
