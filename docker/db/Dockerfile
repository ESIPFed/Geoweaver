FROM mysql
MAINTAINER  (Ziheng Sun) <zsun@gmu.edu>

ENV MYSQL_ROOT_PASSWORD aA123456789

# Copy SQL to mysql initialization folder
ADD gw.sql /docker-entrypoint-initdb.d/

#PORT
#EXPOSE 3306

