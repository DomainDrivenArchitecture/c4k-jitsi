FROM coturn/coturn:4.5.2-r11

# Prepare Configuration
ADD resources /tmp
RUN /tmp/install.sh

ENTRYPOINT ["/entrypoint.sh"]