
# How to set up HTTP Proxy for Geoweaver

When deploying Geoweaver to a public server, the default port 8070 is normally blocked. To access Geoweaver, you need to set up a proxy in the HTTP server. Here is a how-to guide for Apache server.

## Apache Proxy for Geoweaver

This guide uses Apache 2.4.39. It should work for any newer version. For older version, there might be changes. Please [report](https://github.com/ESIPFed/Geoweaver/issues) if running into issues.

* Open your default site HTTP configuration file `/etc/apache2/sites-available/000-default.conf`. 

* Add the following lines into the code block of `<VirtualHost *:80>`:

```
    ProxyPreserveHost On

    ProxyPass /Geoweaver/jupyter-socket ws://localhost:8070/Geoweaver/jupyter-socket
    ProxypassReverse /Geoweaver/jupyter-socket ws://localhost:8070/Geoweaver/jupyter-socket

    ProxyPass /Geoweaver/workflow-socket ws://localhost:8070/Geoweaver/workflow-socket
    ProxypassReverse /Geoweaver/workflow-socket ws://localhost:8070/Geoweaver/workflow-socket

    ProxyPass /Geoweaver/command-socket ws://localhost:8070/Geoweaver/command-socket
    ProxypassReverse /Geoweaver/command-socket ws://localhost:8070/Geoweaver/command-socket

    ProxyPass /Geoweaver/terminal-socket ws://localhost:8070/Geoweaver/terminal-socket
    ProxypassReverse /Geoweaver/terminal-socket ws://localhost:8070/Geoweaver/terminal-socket

    ProxyPass "/Geoweaver" "http://localhost:8070/Geoweaver"
    ProxyPassReverse "/Geoweaver" "http://localhost:8070/Geoweaver"
```

* Restart Apache using: `service apache restart`. Geoweaver should now be accessible at `http://<your_server_domain>/Geoweaver`.

## Nginx Proxy for Geoweaver

TBD

