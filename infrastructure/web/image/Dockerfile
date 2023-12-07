FROM jitsi/web:stable-9111

# Prepare Configuration
ADD resources /tmp
RUN DEBIAN_FRONTEND=noninteractive DEBCONF_NOWARNINGS=yes /tmp/install.sh
