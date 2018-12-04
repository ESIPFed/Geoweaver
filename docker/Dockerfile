FROM tomcat:8.5.35-jre8
MAINTAINER Ziheng Sun <zsun@gmu.edu>


# Prepare the installation of mysql-server and tomcat 7

# Install packages
RUN apt-get update && \
  apt-get -y install mysql-server && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Add image configuration and scripts
# ADD start-tomcat.sh /start-tomcat.sh
# ADD start-mysqld.sh /start-mysqld.sh
ADD run.sh /run.sh
RUN chmod 755 /*.sh
# ADD my.cnf /etc/mysql/conf.d/my.cnf
# RUN chmod 644 /etc/mysql/conf.d/my.cnf
Add gw.sql /gw.sql
# ADD supervisord-tomcat.conf /etc/supervisor/conf.d/supervisord-tomcat.conf
# ADD supervisord-mysqld.conf /etc/supervisor/conf.d/supervisord-mysqld.conf

# Remove pre-installed database
# RUN rm -rf /var/lib/mysql/*

# Add MySQL utils
ADD mysql-user.sh /mysql-user.sh
ADD mysql-setup.sh /mysql-setup.sh
RUN chmod 755 /*.sh

WORKDIR /usr/local/tomcat

# Add volumes for MySQL 
# VOLUME  ["/etc/mysql", "/var/lib/mysql"]

EXPOSE 8080

ENTRYPOINT "/run.sh && /usr/local/tomcat/bin/catalina.sh run"
