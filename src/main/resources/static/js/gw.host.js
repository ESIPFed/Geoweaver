/**
 * All things related to Host section on the left menu: host listing, creation, browsing, and editing.
 */
GW.host = {
  cred_cache: [
    { h: "xxxx", s: "yyyyy", env: { bin: "python3", pyenv: "cdl" } },
  ],

  host_environment_list_cache: null,

  password_frame: null,

  ssh_password_frame: null,

  new_host_frame: null,

  local_hid: null,

  editOn: false,

  clearCache: function () {
    this.cred_cache = [];

    this.host_environment_list_cache = [];
  },

  checkIfHostPanelActive: function () {
    return document.getElementById("main-host-info").style.display == "flex";
  },

  setEnvCache: function (hid, env) {
    var is = false;

    for (var i = 0; i < GW.host.cred_cache.length; i++) {
      if (GW.host.cred_cache[i].h == hid) {
        GW.host.cred_cache[i].env = env;

        is = true;

        break;
      }
    }

    if (!is) {
      GW.host.cred_cache.push({ h: hid, env: env });
    }
  },

  setCache: function (hid, s) {
    var is = false;

    for (var i = 0; i < GW.host.cred_cache.length; i++) {
      if (GW.host.cred_cache[i].h == hid) {
        GW.host.cred_cache[i].s = s;

        is = true;

        break;
      }
    }

    if (!is) {
      GW.host.cred_cache.push({ h: hid, s: s });
    }
  },

  findEnvCache: function (hid) {
    var env = null;

    for (var i = 0; i < GW.host.cred_cache.length; i++) {
      if (GW.host.cred_cache[i].h == hid) {
        env = GW.host.cred_cache[i].env;

        break;
      }
    }

    return env;
  },

  findCache: function (hid) {
    var s = null;

    for (var i = 0; i < GW.host.cred_cache.length; i++) {
      if (GW.host.cred_cache[i].h == hid) {
        s = GW.host.cred_cache[i].s;

        break;
      }
    }

    return s;
  },

  isLocal: function (msg) {
    var is = false;

    if (msg.ip == "127.0.0.1") {
      is = true;
    }

    return is;
  },

  encrypt: function (hid, pstext, req, dialog, button, business_callback) {
    //Two-step encryption is applied here.
    //First, get public key from server.
    //Second, encrypt the password and sent the encypted string to server.
    $.ajax({
      url: "key",

      type: "POST",

      data: "",
    })
      .done(function (msg) {
        //encrypt the password using the received rsa key

        msg = $.parseJSON(msg);

        var encrypt = new JSEncrypt();

        encrypt.setPublicKey(msg.rsa_public);

        var encrypted = encrypt.encrypt(pstext);

        //                msg.pswd = encrypted;

        business_callback(encrypted, req, dialog, button);
      })
      .fail(function (jxr, status) {});
  },

  enter_password: function (hid, req, business_callback) {
    if (this.password_frame != null) {
      try {
        this.password_frame.closeFrame();
      } catch (e) {}

      this.password_frame = null;
    }

    var content =
      '<div class="modal-body">' +
      '   <div class="form-group row required" style="font-size: 12px;">' +
      '     <label for="host password" class="col-sm-4 col-form-label control-label">Input Host User Password: </label>' +
      '     <div class="col-sm-6">' +
      '        <input type="password" class="form-control" id="inputpswd" placeholder="Password" >' +
      "     </div>" +
      '     <div class="col-sm-12 form-check">' +
      '        <input type="checkbox" class="form-check-input" id="remember" />' +
      '        <label class="form-check-label" for="remember">Remember password</label>' +
      "     </div>" +
      "   </div></div>";

    content +=
      '<div class="modal-footer">' +
      '   <button type="button" id="pswd-confirm-btn" class="btn btn-outline-primary">Confirm</button> ' +
      '   <button type="button" id="pswd-cancel-btn" class="btn btn-outline-secondary">Cancel</button>' +
      "</div>";

    this.password_frame = GW.process.createJSFrameDialog(
      520,
      340,
      content,
      "Host Password",
    );
    //automatic focus - input password focus
    setTimeout(() => {
      requestAnimationFrame(() => {
          document.getElementById("inputpswd")?.focus();
      });
  }, 100);  
    
    $("#inputpswd").on("keypress", function (e) {
      if (e.which == 13) {
        $("#pswd-confirm-btn").click();
      }
    });

    $("#pswd-confirm-btn").click(function () {
      $("#pswd-confirm-btn").prop("disabled", true);

      //              dialogItself.enableButtons(false);

      if (document.getElementById("remember").checked) {
        GW.host.setCache(hid, $("#inputpswd").val()); //remember s
      }

      GW.host.encrypt(
        hid,
        $("#inputpswd").val(),
        req,
        GW.host.password_frame,
        $("#pswd-confirm-btn"),
        business_callback,
      );
    });

    $("#pswd-cancel-btn").click(function () {
      GW.host.password_frame.closeFrame();
    });
  },

  start_auth_single: function (hid, req, business_callback) {
    var s = GW.host.findCache(hid);

    // if(hid == GW.host.local_hid){

    //  GW.host.encrypt(hid, "local", req, null, null, business_callback);

    // }else
    if (s == null) {
      GW.host.enter_password(hid, req, business_callback);
    } else {
      GW.host.encrypt(hid, s, req, null, null, business_callback);
    }
  },

  encrypt_m: function (
    hosts,
    pswds,
    req,
    dialogItself,
    button,
    business_callback,
  ) {
    //Two-step encryption is applied here.
    //First, get public key from server.
    //Second, encrypt the password and sent the encypted string to server.
    $.ajax({
      url: "key",

      type: "POST",

      data: "",
    })
      .done(function (msg) {
        //encrypt the password using the received rsa key

        msg = $.parseJSON(msg);

        var encrypt = new JSEncrypt();

        encrypt.setPublicKey(msg.rsa_public);

        var encrypt_passwds = [];

        for (var i = 0; i < hosts.length; i++) {
          var encrypted = encrypt.encrypt(pswds[i]); //$('#inputpswd_' + i).val());

          encrypt_passwds.push(encrypted);
        }

        var ids = GW.host.turnHosts2Ids(hosts);

        var envs = GW.host.turnHosts2EnvIds(hosts);

        req.hosts = ids;

        req.passwords = encrypt_passwds;

        req.envs = envs;

        business_callback(req, dialogItself, button);
      })
      .fail(function (jxr, status) {
        console.error("fail to get encrypted key");
      });
  },

  enter_pswd_m: function (newhosts, hosts, req, business_callback) {
    var content = '<div class="modal-body">';

    for (var i = 0; i < newhosts.length; i++) {
      content +=
        '<div class="form-group row required">' +
        '     <label for="host password" class="col-sm-4 col-form-label control-label">Host ' +
        newhosts[i].name +
        " Password: </label>" +
        '     <div class="col-sm-8">' +
        '        <input type="password" class="form-control" id="inputpswd_' +
        i +
        '" required="true" placeholder="Password">' +
        "     </div>" +
        "   </div>";
    }

    content +=
      '     <div class="form-group form-check">' +
      '        <input type="checkbox" class="form-check-input" id="remember">' +
      '        <label class="form-check-label" for="remember">Remember password</label>' +
      "     </div></div>";

    content +=
      '<div class="modal-footer">' +
      '   <button type="button" id="pswd-confirm" class="btn btn-outline-primary">Confirm</button> ' +
      '   <button type="button" id="pswd-cancel" class="btn btn-outline-secondary">Cancel</button>' +
      "</div>";

    var frame = GW.process.createJSFrameDialog(
      360,
      360,
      content,
      "Host Password",
    );

    frame.on("#pswd-cancel", "click", (_frame, evt) => {
      _frame.closeFrame();
    });

    frame.on("#pswd-confirm", "click", (_frame, evt) => {
      var filled = true;

      $.each($("input[type='password']"), function () {
        if (!$(this).val()) {
          filled = false;

          alert("Please input password. ");

          return;
        }
      });

      if (!filled) return;

      var $button = $(this);
      //
      //              $button.spin();

      var shortpasswds = [];

      for (var i = 0; i < newhosts.length; i++) {
        shortpasswds.push($("#inputpswd_" + i).val());

        if (document.getElementById("remember").checked) {
          GW.host.setCache(newhosts[i].id, $("#inputpswd_" + i).val());
        }
      }

      var passwds = GW.host.extendList(shortpasswds, newhosts, hosts);

      GW.host.encrypt_m(
        hosts,
        passwds,
        req,
        _frame,
        $button,
        business_callback,
      );

      _frame.closeFrame();
    });
  },

  start_auth_multiple: function (hosts, req, business_callback) {
    var newhosts = this.shrinkList(hosts);

    if (newhosts.length > 0) {
      GW.host.enter_pswd_m(newhosts, hosts, req, business_callback);
    } else {
      var passwds = GW.host.extendList([], newhosts, hosts);

      GW.host.encrypt_m(hosts, passwds, req, null, null, business_callback);
    }
  },

  turnHosts2Ids: function (hosts) {
    var ids = [];

    for (var i = 0; i < hosts.length; i++) {
      ids.push(hosts[i].id);
    }

    return ids;
  },

  turnHosts2EnvIds: function (hosts) {
    var ids = [];

    for (var i = 0; i < hosts.length; i++) {
      ids.push(hosts[i].env);
    }

    return ids;
  },

  /**
   * Extend the list to original size
   */
  extendList: function (shortpasswds, newhosts, hosts) {
    var fullpasswdslist = [];

    for (var i = 0; i < hosts.length; i++) {
      var passwd = null;

      for (var j = 0; j < newhosts.length; j++) {
        if (newhosts[j].id == hosts[i].id) {
          passwd = shortpasswds[j];

          break;
        }
      }

      if (passwd != null) fullpasswdslist.push(passwd);
      else fullpasswdslist.push(GW.host.findCache(hosts[i].id));
    }

    return fullpasswdslist;
  },

  shrinkList: function (hosts) {
    var newhosts = [];

    for (var i = 0; i < hosts.length; i++) {
      var exist = false;

      for (var j = 0; j < newhosts.length; j++) {
        if (hosts[i].id == newhosts[j].id) {
          exist = true;

          break;
        }
      }

      if (!exist && GW.host.findCache(hosts[i].id) == null) {
        //the p is not cached

        newhosts.push(hosts[i]);
      }
    }

    return newhosts;
  },

  /**
   * Close the SSH Terminal and Connection
   */
  closeSSH: function (token) {
    $.ajax({
      url: "geoweaver-ssh-logout-inbox",

      method: "POST",

      data: "token=" + token,
    })
      .done(function (msg) {
        if (msg == "done") {
          console.log("SSH session is closed.");

          $("#ssh-terminal-iframe").html("");
        } else {
          console.error("Fail to close SSH.");
        }
      })
      .fail(function () {
        console.error("Fail to close SSH.");
      });
  },

  /**
   * Show the SSH Terminal Section
   */
  showSSHCmd: function (token) {
    //          var frame = GW.process.createJSFrameDialog(600, 540, "<iframe src=\"geoweaver-ssh?token="+
    //                  token+"\" style=\"height:100%;width:100%;\"></iframe>", "SSH Command Line")

    var frame =
      '<h4 class="border-bottom">SSH Terminal Section  <button type="button" class="btn btn-secondary btn-sm" id="closeSSHTerminal" >close</button></h4>' +
      '<iframe src="geoweaver-ssh?token=' +
      token +
      '" style="height:700px; max-height:1000px;width:100%;"></iframe>';

    $("#ssh-terminal-iframe").html(frame);

    $("#closeSSHTerminal").click(function () {
      GW.host.closeSSH(token);

      $("#ssh-terminal-iframe").html(""); //double remove to make sure it clears every time
    });
  },

  /**
   * Open the SSH Connection Dialog if the host is a remote server
   */
  openssh: function (hostid) {
    //get the host information

    $.ajax({
      url: "detail",

      method: "POST",

      data: "type=host&id=" + hostid,
    }).done(function (msg) {
      //open the login page

      hostmsg = $.parseJSON(msg);

      if (GW.host.ssh_password_frame != null) {
        try {
          GW.host.ssh_password_frame.closeFrame();
        } catch (e) {
          console.log("Probably it is closed already.");
        }

        GW.host.ssh_password_frame = null;
      }

      if (GW.host.findCache(hostid) == null) {
        var cont =
          '<div class="modal-body" style="font-size: 12px;">' +
          '<div class="row">';

        cont +=
          '<div class="col col-md-5">IP</div><div class="col col-md-5">' +
          hostmsg.ip +
          "</div>";

        cont +=
          '<div class="col col-md-5">Port</div><div class="col col-md-5">' +
          hostmsg.port +
          "</div>";

        cont +=
          '<div class="col col-md-5">User</div><div class="col col-md-5">' +
          hostmsg.username +
          "</div>";

        cont +=
          '<div class="col col-md-5">Password</div><div class="col col-md-5"><input type="password" id="passwd" class="form-control" id="inputpswd" placeholder="Password"></div>';

        cont +=
          '     <div class="col-sm-12 form-check">' +
          '        <input type="checkbox" class="form-check-input" id="ssh-remember" />' +
          '        <label class="form-check-label" for="ssh-remember">Remember password and don\'t ask again.</label>' +
          "     </div>";

        cont += "</div></div>";

        cont +=
          '<div class="modal-footer">' +
          '   <button type="button" id="ssh-connect-btn" class="btn btn-outline-primary">Connect</button> ' +
          '   <button type="button" id="ssh-cancel-btn" class="btn btn-outline-secondary">Cancel</button>' +
          "</div>";

        GW.host.ssh_password_frame = GW.process.createJSFrameDialog(
          500,
          340,
          cont,
          "Open SSH session",
        );

        $("#ssh-connect-btn").click(function () {
          $("#ssh-connect-btn").prop("disabled", true);

          $.ajax({
            url: "key",

            type: "POST",

            data: "",
          }).done(function (msg) {
            //encrypt the password using the received rsa key
            msg = $.parseJSON(msg);

            var encrypt = new JSEncrypt();

            encrypt.setPublicKey(msg.rsa_public);

            var encrypted = encrypt.encrypt($("#passwd").val());

            var req = {
              host: hostmsg.ip,
              port: hostmsg.port,
              username: hostmsg.username,
              password: encrypted,
            };

            $.ajax({
              url: "geoweaver-ssh-login-inbox",

              method: "POST",

              data: req,
            })
              .done(function (msg) {
                msg = $.parseJSON(msg);

                if (msg.token != null) {
                  //open a dialog to show the SSH command line interface

                  GW.host.showSSHCmd(msg.token);

                  if (document.getElementById("ssh-remember").checked) {
                    GW.host.setCache(hostid, $("#passwd").val()); //only remember password if users check the box the the login is successful.
                  }
                } else {
                  alert(
                    "Username or Password is wrong or the server is not accessible",
                  );
                }
                try {
                  GW.host.ssh_password_frame.closeFrame();
                } catch (e) {
                  console.log(e);
                }
              })
              .fail(function (status) {
                alert(
                  "Username or Password is wrong or the server is not accessible" +
                    status,
                );

                $("#ssh-connect-btn").prop("disabled", false);
              });
          });
        });

        $("#ssh-cancel-btn").click(function () {
          GW.host.ssh_password_frame.closeFrame();
        });
      } else {
        //if the login attempt failed once, the password will be removed and users need input again.
        var pswd = GW.host.findCache(hostid);

        $.ajax({
          url: "key",

          type: "POST",

          data: "",
        }).done(function (msg) {
          //encrypt the password using the received rsa key
          msg = $.parseJSON(msg);

          var encrypt = new JSEncrypt();

          encrypt.setPublicKey(msg.rsa_public);

          var encrypted = encrypt.encrypt(pswd);

          var req = {
            host: hostmsg.ip,
            port: hostmsg.port,
            username: hostmsg.username,
            password: encrypted,
          };

          $.ajax({
            url: "geoweaver-ssh-login-inbox",

            method: "POST",

            data: req,
          })
            .done(function (msg) {
              msg = $.parseJSON(msg);

              if (msg.token != null) {
                //open a dialog to show the SSH command line interface

                GW.host.showSSHCmd(msg.token);
              } else {
                alert(
                  "Username or Password is wrong or the server is not accessible",
                );

                GW.host.setCache(hostid, null);
              }
            })
            .fail(function (status) {
              alert(
                "Username or Password is wrong or the server is not accessible" +
                  status,
              );

              GW.host.setCache(hostid, null);
              //$("#ssh-connect-btn").prop("disabled", false);
            });
        });
      }
    });
  },

  cleanMenu: function () {
    $("#host_folder_ssh_target").html("");

    $("#host_folder_jupyter_target").html("");

    $("#host_folder_jupyterhub_target").html("");

    $("#host_folder_gee_target").html("");
  },

  refreshHostListForExecution: function () {
    $.ajax({
      url: "listhostwithenvironments",

      method: "POST",

      data: "type=host",
    })
      .done(function (msg) {
        msg = $.parseJSON(msg);

        GW.host.host_environment_list_cache = msg;

        console.log("Start to refresh the host list..");

        GW.host.list(msg);

        if ($(".hostselector")) {
          for (var i = 0; i < msg.length; i++) {
            //right now only SSH host can run processes
            if (msg[i].type == "ssh") {
              $(".hostselector").append(
                '<option id="' + msg[i].id + '">' + msg[i].name + "</option>",
              );
            }
          }

          //show the environment of the first host
          if ($(".environmentselector")) {
            $(".environmentselector").append(
              '<option id="default_option">default</option>',
            );

            var envs = msg[0].envs;

            for (var i = 0; i < envs.length; i++) {
              $(".environmentselector").append(
                '<option id="' + envs[i].id + '">' + envs[i].name + "</option>",
              );
            }
          }

          $(".hostselector").change(function () {
            //get the corresponding environmentselector
            // var corenvelelist = $(this).closest('div').next().find('.environmentselector');
            var hostselectid = $(this).attr("id");

            var envselectid =
              "environmentforprocess_" + hostselectid.split("_")[1];

            //change the environment selector options
            var envselect = $("#" + envselectid);

            var selectedhostid = $(this).children("option:selected").attr("id");

            envselect
              .empty()
              .append('<option id="default_option">default</option>');

            //add new options to the environment selector
            var theenv = GW.host.findEnvironmentByHostId(selectedhostid);

            if (theenv != null) {
              for (var i = 0; i < theenv.length; i++) {
                envselect.append(
                  '<option id="' +
                    theenv[i].id +
                    '">' +
                    theenv[i].name +
                    "</option>",
                );
              }
            }
          });
        }
      })
      .fail(function (jxr, status) {
        console.error("fail to list host");
      });
  },

  findEnvironmentByHostId: function (hostid) {
    var theenv = null;

    if (GW.host.host_environment_list_cache != null) {
      for (var i = 0; i < GW.host.host_environment_list_cache.length; i++) {
        var value = GW.host.host_environment_list_cache[i];
        if (hostid == value.id) {
          theenv = value.envs;
          break;
        }
      }
    }

    return theenv;
  },

  refreshSearchList: function () {
    GW.search.filterMenuListUtil("host_folder_ssh_target", "hosts", "host");
  },

  //refresh host list for the menu
  refreshHostList: function () {
    $.ajax({
      url: "list",

      method: "POST",

      data: "type=host",
    })
      .done(function (msg) {
        msg = $.parseJSON(msg);

        console.log("Start to refresh the host list..");

        GW.host.list(msg);
      })
      .fail(function (jxr, status) {
        console.error("fail to list host");
      });
  },

  addMenuItem: function (one) {
    var one_item =
      ` <li class="host" id="host-` +
      one.id +
      `" onclick="GW.menu.details('` +
      one.id +
      `', 'host')">&nbsp;&nbsp;&nbsp;` +
      one.name +
      `</li>`;
    // add to the first, not the last
    $("#host_folder_" + one.type + "_target").prepend(one_item);
  },

  expand: function (one) {
    console.log("EXPAND host type");

    $("#host_folder_" + one.type + "_target").collapse("show");
  },

  list: function (msg) {
    GW.host.cleanMenu();

    for (var i = 0; i < msg.length; i++) {
      this.addMenuItem(msg[i]);
    }

    $("#hosts").collapse("show");
  },

  validateIP: function (ipaddress) {
    var valid = false;

    if (
      /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(
        ipaddress,
      )
    ) {
      valid = true;
    } else {
      alert("You have entered an invalid IP address!");
    }

    return valid;
  },

  preEditcheck: function () {
    console.log("Check if the input valid");

    var valid = false;

    var hosttype = $("#_host_type").text();

    if (hosttype == "ssh" || hosttype == "") {
      if (
        $("#_host_name").val() &&
        $("#_host_ip").val() &&
        $("#_host_port").val() &&
        $("#_host_username").val() &&
        this.validateIP($("#_host_ip").val()) &&
        $.isNumeric($("#_host_port").val())
      ) {
        valid = true;
      }
    } else if (hosttype == "jupyter") {
      if ($("#_host_name").val() && $("#_host_url").val()) {
        valid = true;
      }
    } else if (hosttype == "gee") {
      if ($("#_host_name").val() && $("#_host_client_id").val()) {
        valid = true;
      }
    }

    return valid;
  },

  precheck: function () {
    var valid = false;

    var hosttype = $("#hosttype option:selected").val();

    if (hosttype == "ssh") {
      if (
        $("#hostname").val() &&
        $("#hostip").val() &&
        $("#hostport").val() &&
        $("#username").val() &&
        this.validateIP($("#hostip").val()) &&
        $.isNumeric($("#hostport").val())
      ) {
        valid = true;
      }
    } else if (
      hosttype == "jupyter" ||
      hosttype == "jupyterhub" ||
      hosttype == "jupyterlab"
    ) {
      if ($("#hostname").val() && $("#jupyter_home_url").val()) {
        valid = true;
      }
    } else if (hosttype == "gee") {
      if ($("#hostname").val() && $("#client_id").val()) {
        valid = true;
      }
    }

    return valid;
  },

  add: function (callback) {
    if (this.precheck()) {
      var hostport = "";

      if (typeof $("#hostport").val() != "undefined") {
        hostport = $("#hostport").val();
      }

      var hostip = "";

      if (typeof $("#hostip").val() != "undefined") {
        hostip = $("#hostip").val();
      }

      var hosttype = $("#hosttype option:selected").val();

      var jupyter_url = "";

      if (typeof $("#jupyter_home_url").val() != "undefined") {
        jupyter_url = $("#jupyter_home_url").val();
      }

      var confidential = "FALSE"; //default is public

      var confidential_field_value = $(
        '#host_dynamic_form input[name="confidential"]:checked',
      ).val();

      if (typeof confidential_field_value != "undefined") {
        confidential = confidential_field_value;
      }

      var req = {
        type: "host",

        hostname: $("#hostname").val(),

        hostip: hostip,

        hostport: hostport,

        url: jupyter_url,

        hosttype: hosttype,

        username: $("#username").val(),

        confidential: confidential,

        ownerid: GW.user.current_userid,
      };

      $.ajax({
        url: "add",

        method: "POST",

        data: req,
      })
        .done(function (msg) {
          msg = $.parseJSON(msg);

          GW.host.addMenuItem(msg);

          GW.host.expand(msg);

          callback();
        })
        .fail(function (jqXHR, textStatus) {
          alert("Fail to add the host.");
        });
    } else {
      alert("Invalid input");
    }
  },

  edit: function () {
    if (this.preEditcheck()) {
      var hostid = $("#_host_id").text();

      var hostname = $("#_host_name").val();

      var hostusername = $("#_host_username").val();

      var hostip = "";

      if (typeof $("#_host_ip").val() != "undefined") {
        hostip = $("#_host_ip").val();
      }

      var hostport = "";

      if (typeof $("#_host_port").val() != "undefined") {
        hostport = $("#_host_port").val();
      }

      var hosttype = $("#_host_type").text();

      var jupyter_url = "";

      if (typeof $("#_host_url").val() != "undefined") {
        jupyter_url = $("#_host_url").val();
      }

      var confidential = "FALSE"; //default is public

      if (typeof $('input[name="confidential"]:checked').val() != "undefined") {
        confidential = $('input[name="confidential"]:checked').val();
      }

      var req = {
        type: "host",

        hostname: hostname,

        hostip: hostip,

        hostport: hostport,

        url: jupyter_url,

        hosttype: hosttype,

        username: hostusername,

        hostid: hostid,

        confidential: confidential,
      };

      $.ajax({
        url: "edit",

        method: "POST",

        data: req,
      })
        .done(function (msg) {
          msg = $.parseJSON(msg);

          GW.general.showToasts("Host updated.");

          GW.host.refreshHostList();
        })
        .fail(function (jqXHR, textStatus) {
          alert("Fail to add the host.");
        });
    } else {
      alert("Invalid input");
    }
  },

  editSwitch: function () {
    if (GW.host.checkIfHostPanelActive()) {
        console.log("Turn on/off the fields");

        // Enable input fields for editing
        $(".host-value-field").prop("disabled", false);

        // Remove any existing Save Changes and Cancel buttons to prevent duplicates
        $("#save-changes-btn").remove();
        $("#cancel-changes-btn").remove();

        // Add action buttons dynamically in the details tab
        var actionButtons = '<div class="mt-4 d-flex justify-content-end gap-2">' +
          '<button id="cancel-changes-btn" type="button" class="btn btn-secondary" onclick="GW.host.cancelEdit()">Cancel</button>' +
          '<button id="save-changes-btn" type="button" class="btn btn-success" onclick="GW.host.saveChanges()">Save Changes</button>' +
          '</div>';
        $("#details-tab-pane").append(actionButtons);

        // Set edit mode to true
        GW.process.editOn = true;
    }
  },
  
  cancelEdit: function () {
    // Disable input fields
    $(".host-value-field").prop("disabled", true);
    
    // Remove action buttons
    $("#save-changes-btn").remove();
    $("#cancel-changes-btn").remove();
    
    // Reset edit mode
    GW.process.editOn = false;
    
    console.log("Edit cancelled");
  },

saveChanges: function () {
  console.log("Saving the changes if any");

  // Disable input fields after saving
  $(".host-value-field").prop("disabled", true);

  // Call the edit function to update the host details
  GW.host.edit();  // Saves the changes

  // Remove the action buttons after saving
  $("#save-changes-btn").remove();
  $("#cancel-changes-btn").remove();

  // Reset the edit mode
  GW.process.editOn = false;

  console.log("Changes saved successfully!");
},

  openJupyter: function (hostid) {
    window.open(GW.path.getBasePath() + "jupyter-proxy/" + hostid + "/", "_blank");
  },

  openGoogleEarth: function (hostid) {
    window.open(GW.path.getBasePath() + "GoogleEarth-proxy/" + hostid + "/", "_blank");
  },

  getToolbar: function (hostid, hosttype, detailsContent) {
    // If detailsContent is provided, include it in the details tab pane
    var detailsPaneContent = detailsContent || '';
    
    // Get host name for the header
    var hostName = '';
    try {
      var hostNameElement = document.getElementById('_host_name');
      if (hostNameElement) {
        hostName = hostNameElement.value || hostNameElement.textContent || '';
      }
    } catch (e) {
      // Ignore if element not found
    }
    
    return `
      <!-- Host Header Section -->
      <div class="host-header" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 24px 30px; margin: 0; border-bottom: 1px solid #e0e0e0;">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <h2 style="margin: 0; color: #fff; font-size: 24px; font-weight: 600; display: flex; align-items: center; gap: 12px;">
              <i class="fas fa-server" style="font-size: 28px;"></i>
              <span id="host-header-name">${hostName || 'Host Management'}</span>
            </h2>
            <p style="margin: 8px 0 0 0; color: rgba(255, 255, 255, 0.9); font-size: 14px;">
              <i class="fas fa-network-wired"></i> Manage host configuration and resources
            </p>
          </div>
        </div>
      </div>

      <!-- Tabs Navigation - Professional Design with Spacing -->
      <div style="background-color: #fff; border-bottom: 2px solid #e0e0e0; padding: 0 30px;">
        <ul class="nav nav-tabs host-tabs" role="tablist" style="margin: 0; border-bottom: none; background-color: transparent;">
          <li class="nav-item" role="presentation">
            <button class="nav-link active" id="details-tab-btn" data-bs-toggle="tab" data-bs-target="#details-tab-pane" 
                    type="button" role="tab" onclick="GW.host.switchTab(event, 'details-tab-pane', null)"
                    style="border: none; border-bottom: 3px solid #007bff; color: #007bff; font-weight: 500; padding: 16px 24px; margin-right: 8px; transition: all 0.3s ease;">
              <i class="fas fa-info-circle"></i> Details
            </button>
          </li>
        ${hosttype === "ssh" || hosttype === "null" || hosttype === null ? `
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="python-env-tab-btn" data-bs-toggle="tab" data-bs-target="#python-env-tab-pane" 
                    type="button" role="tab" onclick="GW.host.switchTab(event, 'python-env-tab-pane', 'GW.host.readEnvironment(\\'${hostid}\\')')"
                    style="border: none; color: #6c757d; font-weight: 500; padding: 16px 24px; margin-right: 8px; transition: all 0.3s ease;">
              <i class="fab fa-python"></i> Python Env
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="upload-tab-btn" data-bs-toggle="tab" data-bs-target="#upload-tab-pane" 
                    type="button" role="tab" onclick="GW.host.switchTab(event, 'upload-tab-pane', 'GW.fileupload.uploadfile(\\'${hostid}\\')')"
                    style="border: none; color: #6c757d; font-weight: 500; padding: 16px 24px; margin-right: 8px; transition: all 0.3s ease;">
              <i class="fas fa-upload"></i> Upload
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="browse-tab-btn" data-bs-toggle="tab" data-bs-target="#browse-tab-pane" 
                    type="button" role="tab" onclick="GW.host.switchTab(event, 'browse-tab-pane', 'GW.filebrowser.start(\\'${hostid}\\')')"
                    style="border: none; color: #6c757d; font-weight: 500; padding: 16px 24px; margin-right: 8px; transition: all 0.3s ease;">
              <i class="fas fa-folder-open"></i> Browse
            </button>
          </li>
        ` : ""}
        ${hosttype === "jupyter" || hosttype === "jupyterhub" || hosttype === "jupyterlab" ? `
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="history-tab-btn" data-bs-toggle="tab" data-bs-target="#history-tab-pane" 
                    type="button" role="tab" onclick="GW.host.switchTab(event, 'history-tab-pane', 'GW.host.recent(\\'${hostid}\\')')"
                    style="border: none; color: #6c757d; font-weight: 500; padding: 16px 24px; margin-right: 8px; transition: all 0.3s ease;">
              <i class="fas fa-history"></i> History
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="jupyter-tab-btn" data-bs-toggle="tab" data-bs-target="#jupyter-tab-pane" 
                    type="button" role="tab" onclick="GW.host.switchTab(event, 'jupyter-tab-pane', 'GW.host.openJupyter(\\'${hostid}\\')')"
                    style="border: none; color: #6c757d; font-weight: 500; padding: 16px 24px; margin-right: 8px; transition: all 0.3s ease;">
              <i class="fab fa-python"></i> Jupyter
            </button>
          </li>
        ` : ""}
        ${hosttype === "gee" ? `
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="history-tab-btn" data-bs-toggle="tab" data-bs-target="#history-tab-pane" 
                    type="button" role="tab" onclick="GW.host.switchTab(event, 'history-tab-pane', 'GW.host.recent(\\'${hostid}\\')')"
                    style="border: none; color: #6c757d; font-weight: 500; padding: 16px 24px; margin-right: 8px; transition: all 0.3s ease;">
              <i class="fas fa-history"></i> History
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button class="nav-link" id="google-earth-tab-btn" data-bs-toggle="tab" data-bs-target="#google-earth-tab-pane" 
                    type="button" role="tab" onclick="GW.host.switchTab(event, 'google-earth-tab-pane', 'GW.host.openGoogleEarth(\\'${hostid}\\')')"
                    style="border: none; color: #6c757d; font-weight: 500; padding: 16px 24px; margin-right: 8px; transition: all 0.3s ease;">
              <i class="fas fa-globe"></i> Google Earth
            </button>
          </li>
        ` : ""}
        </ul>
      </div>

      <!-- Tabs Content Container with Proper Spacing -->
      <div class="tab-content host-tab-content" style="width: 100%; min-height: 400px; background: #f8f9fa; padding: 30px; position: relative; overflow: visible;">
        <div id="details-tab-pane" class="tab-pane fade show active" role="tabpanel" style="display: block !important; visibility: visible !important; opacity: 1 !important; position: relative;">${detailsPaneContent}</div>
        <div id="python-env-tab-pane" class="tab-pane fade" role="tabpanel" style="display: none;"></div>
        <div id="upload-tab-pane" class="tab-pane fade" role="tabpanel" style="display: none;"></div>
        <div id="browse-tab-pane" class="tab-pane fade" role="tabpanel" style="display: none;"></div>
        <div id="history-tab-pane" class="tab-pane fade" role="tabpanel" style="display: none;"></div>
        <div id="jupyter-tab-pane" class="tab-pane fade" role="tabpanel" style="display: none;"></div>
        <div id="google-earth-tab-pane" class="tab-pane fade" role="tabpanel" style="display: none;"></div>
      </div>
    `;
  },


  switchTab: function (event, tabId, action) {
  // Get all tab buttons and panes
  let tabButtons = document.querySelectorAll(".host-tabs .nav-link");
  let tabPanes = document.querySelectorAll(".host-tab-content .tab-pane");

  // Remove active state from all tabs
  tabButtons.forEach(btn => {
    btn.classList.remove("active");
    btn.style.borderBottom = "none";
    btn.style.color = "#6c757d";
    btn.style.backgroundColor = "transparent";
  });

  // Hide all tab panes and clear content (except details tab which should keep its content)
  tabPanes.forEach(pane => {
    if (pane.id === tabId) {
      // Show the selected tab pane - use !important to override any conflicting styles
      pane.style.setProperty("display", "block", "important");
      pane.style.setProperty("visibility", "visible", "important");
      pane.style.setProperty("opacity", "1", "important");
      pane.classList.add("show", "active");
    } else {
      // Hide other tab panes
      pane.style.setProperty("display", "none", "important");
      pane.classList.remove("show", "active");
      // Only clear content for non-details tabs to avoid losing host details
      if (pane.id !== "details-tab-pane") {
        // Don't clear details tab content, but clear others when switching away
        if (tabId !== "details-tab-pane") {
          pane.innerHTML = "";
        }
      }
    }
  });

  // Activate the clicked tab button
  if (event && event.currentTarget) {
    event.currentTarget.classList.add("active");
    event.currentTarget.style.borderBottom = "3px solid #007bff";
    event.currentTarget.style.color = "#007bff";
    event.currentTarget.style.backgroundColor = "transparent";
  } else {
    // If called programmatically, find the button by tabId
    let buttonId = tabId.replace("-pane", "-btn");
    let button = document.getElementById(buttonId);
    if (button) {
      button.classList.add("active");
      button.style.borderBottom = "3px solid #007bff";
      button.style.color = "#007bff";
    }
  }

  // The selected tab pane is already shown in the forEach loop above
  // This is just a safety check
  let activePane = document.getElementById(tabId);
  if (activePane && activePane.style.display === "none") {
    activePane.style.display = "block";
    activePane.classList.add("show", "active");
  }

  // If there's an action, execute it
  if (action) {
    setTimeout(() => {
      try {
        eval(action); // Execute the function dynamically
      } catch (e) {
        console.error("Error executing tab action:", e);
      }
    }, 100);
  }

  // Close any existing modals/popups to ensure a clean switch
  if (GW.host.ssh_password_frame) {
    try {
      // Check if the frame still exists and has the closeFrame method
      if (GW.host.ssh_password_frame && typeof GW.host.ssh_password_frame.closeFrame === 'function') {
        GW.host.ssh_password_frame.closeFrame();
      }
      GW.host.ssh_password_frame = null;
    } catch (e) {
      // Silently handle errors - frame may already be closed
      GW.host.ssh_password_frame = null;
    }
  }

  if (GW.host.password_frame) {
    try {
      // Check if the frame still exists and has the closeFrame method
      if (GW.host.password_frame && typeof GW.host.password_frame.closeFrame === 'function') {
        GW.host.password_frame.closeFrame();
      }
      GW.host.password_frame = null;
    } catch (e) {
      // Silently handle errors - frame may already be closed
      GW.host.password_frame = null;
    }
  }

  // Update content placement based on tab
  // Python Environment tab
  if (tabId === "python-env-tab-pane") {
    let envContainer = document.getElementById("python-env-tab-pane");
    if (envContainer && document.getElementById("environment-iframe")) {
      let envContent = document.getElementById("environment-iframe").innerHTML;
      if (envContent) {
        envContainer.innerHTML = envContent;
        document.getElementById("environment-iframe").innerHTML = "";
      }
    }
  }
  
  // Upload tab
  if (tabId === "upload-tab-pane") {
    let uploadContainer = document.getElementById("upload-tab-pane");
    if (uploadContainer && document.getElementById("host-file-uploader")) {
      let uploadContent = document.getElementById("host-file-uploader").innerHTML;
      if (uploadContent) {
        uploadContainer.innerHTML = uploadContent;
        document.getElementById("host-file-uploader").innerHTML = "";
      }
    }
  }
  
  // Browse tab
  if (tabId === "browse-tab-pane") {
    let browseContainer = document.getElementById("browse-tab-pane");
    if (browseContainer && document.getElementById("host-file-browser")) {
      let browseContent = document.getElementById("host-file-browser").innerHTML;
      if (browseContent) {
        browseContainer.innerHTML = browseContent;
        document.getElementById("host-file-browser").innerHTML = "";
      }
    }
  }
  
  // History tab
  if (tabId === "history-tab-pane") {
    let historyContainer = document.getElementById("history-tab-pane");
    if (historyContainer && document.getElementById("host-history-browser")) {
      let historyContent = document.getElementById("host-history-browser").innerHTML;
      if (historyContent) {
        historyContainer.innerHTML = historyContent;
        document.getElementById("host-history-browser").innerHTML = "";
      }
    }
  }
},
  showEnvironmentTable: function (msg) {
    var content =
      '<div class="container-fluid" style="padding: 20px;">' +
      '<div class="d-flex justify-content-between align-items-center mb-3">' +
      '<h4 style="margin: 0;"><i class="fab fa-python"></i> Python Environment List</h4>' +
      '<button type="button" class="btn btn-secondary btn-sm" id="closeEnvironmentPanel">Close</button>' +
      '</div>' +
      '<div style="font-size: 12px;">' +
      '<table class="table table-striped table-hover" id="environment_table"> ' +
      '  <thead class="thead-light"> ' +
      "    <tr> " +
      '      <th scope="col">Name</th> ' +
      '      <th scope="col">Bin Path</th> ' +
      '      <th scope="col">PyEnv</th> ' +
      '      <th scope="col">Base Directory</th> ' +
      '      <th scope="col">Settings</th> ' +
      "    </tr> " +
      "  </thead> " +
      "  <tbody> ";

    if (msg.length == 0) {
      content +=
        "    <tr> " +
        '      <td colspan="5" style="text-align: center;">No environment found</td> ' +
        "    </tr> ";
    } else {
      for (var i = 0; i < msg.length; i++) {
        content +=
          "    <tr> " +
          "      <td>" +
          (msg[i].name || "") +
          "</td> " +
          "      <td>" +
          (msg[i].bin || "") +
          "</td> " +
          "      <td>" +
          (msg[i].pyenv || "") +
          "</td> " +
          "      <td>" +
          (msg[i].basedir || "") +
          "</td> " +
          "      <td>" +
          (msg[i].settings || "") +
          "</td> " +
          "    </tr>";
      }
    }

    content += "</tbody>" + "</table>" + "</div>" + "</div>";

    // Put content in the python-env-tab-pane
    $("#python-env-tab-pane").html(content);
    
    // Also update the old container for backward compatibility
    if ($("#environment-iframe").length) {
      $("#environment-iframe").html(content);
    }

    $("#closeEnvironmentPanel").click(function () {
      $("#python-env-tab-pane").html("");
      if ($("#environment-iframe").length) {
        $("#environment-iframe").html("");
      }
    });
  },

  readEnvironmentCallback: function (encrypt, req, dialogItself, button) {
    req.pswd = encrypt;

    req.token = GW.general.CLIENT_TOKEN;

    $.ajax({
      url: "readEnvironment",

      type: "POST",

      data: req,
    })
      .done(function (msg) {
        if (msg) {
          msg = GW.general.parseResponse(msg);

          if (msg.status == "failed") {
            alert("Fail to read python environment.");

            console.error("fail to execute the process " + msg.reason);
          } else {
            GW.host.showEnvironmentTable(msg);
          }
        } else {
          console.warn("Return Response is Empty");

          GW.host.showEnvironmentTable([]);
        }

        if (dialogItself) {
          try {
            dialogItself.closeFrame();
          } catch (e) {}
        }
      })
      .fail(function (jxr, status) {
        alert(
          "Error: unable to log on. Check if your password or the configuration of host is correct.",
        );

        if ($("#inputpswd").length) $("#inputpswd").val("");

        if ($("#pswd-confirm-btn").prop("disabled")) {
          $("#pswd-confirm-btn").prop("disabled", false);
        }

        console.error("fail to execute the process " + req.processId);
      });
  },

  readEnvironment: function (hid) {
    var req = {
      hostid: hid,
    };

    GW.host.start_auth_single(hid, req, GW.host.readEnvironmentCallback);
  },

  display: function (msg) {
    GW.process.editOn = false;

    var hostid = null,
      hostip = null,
      hosttype = null,
      confidential = null,
      owner = null,
      envs = null,
      hostname = null;

    // First, extract all host information
    jQuery.each(msg, function (i, val) {
      if (val != null && val != "null" && val != "") {
        if (typeof val == "object") {
          val = JSON.stringify(val);
        }

        if (i == "id") {
          hostid = val;
        } else if (i == "ip") {
          hostip = val;
        } else if (i == "type") {
          hosttype = val;
        } else if (i == "name") {
          hostname = val;
        } else if (i == "confidential") {
          confidential = val;
        } else if (i == "owner") {
          owner = val;
        } else if (i == "envs") {
          envs = val;
        }
      }
    });

    // Build the host details content for the Details tab
    var detailsContent = '<div class="container-fluid" style="padding: 0;">';
    detailsContent += '<div class="d-flex justify-content-between align-items-center mb-4" style="background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">';
    detailsContent += '<h3 style="margin: 0; color: #333; font-size: 20px; font-weight: 600;"><i class="fas fa-info-circle" style="color: #007bff;"></i> Host Details</h3>';
    
    // Add action buttons
    var actionButtons = '<div class="btn-group" role="group">';
    actionButtons += '<button class="btn btn-primary btn-sm" onclick="GW.host.editSwitch()" title="Edit host details">' +
                     '<i class="fas fa-edit"></i> Edit</button>';
    if (msg.id != "100001") {
      actionButtons += '<button class="btn btn-danger btn-sm" onclick="GW.menu.del(\'' + hostid + '\',\'host\')" title="Delete this host">' +
                      '<i class="fas fa-trash"></i> Delete</button>';
    }
    actionButtons += '</div>';
    detailsContent += actionButtons;
    detailsContent += '</div>';

    detailsContent += '<form class="form-horizontal" id="info_form" style="background: #f8f9fa; padding: 20px; border-radius: 8px;">';
    
    // Build form fields
    jQuery.each(msg, function (i, val) {
      if (val != null && val != "null" && val != "" && 
          i != "confidential" && i != "owner" && i != "envs") {
        if (typeof val == "object") {
          val = JSON.stringify(val);
        }

        detailsContent += '<div class="row mb-3">';
        
        // Label
        var label = i.charAt(0).toUpperCase() + i.slice(1);
        if (i == "ip") label = "IP Address";
        else if (i == "id") label = "ID";
        else if (i == "url") label = "URL";
        
        detailsContent += '<div class="col-md-3"><label class="form-label fw-bold">' + label + '</label></div>';
        
        // Value field
        if (i == "id" || i == "type") {
          detailsContent += '<div class="col-md-9"><div class="form-control-plaintext" id="_host_' + i + '">' + val + '</div></div>';
        } else {
          var icon = "glyphicon-link";
          if (i == "name") icon = "glyphicon-pencil";
          else if (i == "ip") icon = "glyphicon-globe";
          else if (i == "port") icon = "glyphicon-transfer";
          else if (i == "username") icon = "glyphicon-user";
          
          detailsContent += '<div class="col-md-9">' +
            '<div class="input-group">' +
            '<span class="input-group-addon"><i class="glyphicon ' + icon + '"></i></span>' +
            '<input class="host-value-field form-control" type="text" id="_host_' + i + 
            '" disabled="true" value="' + val + '" />' +
            '</div></div>';
        }
        
        detailsContent += '</div>';
      }
    });

    // Add Confidential field
    detailsContent += '<div class="row mb-3">';
    detailsContent += '<div class="col-md-3"><label class="form-label fw-bold">Confidential</label></div>';
    detailsContent += '<div class="col-md-9">';
    
    if (confidential == "FALSE") {
      detailsContent += '<div class="form-check form-check-inline">' +
        '<input class="form-check-input" type="radio" name="confidential" value="FALSE" checked id="public_radio">' +
        '<label class="form-check-label" for="public_radio">Public</label>' +
        '</div>';
      if (GW.user.current_userid == owner && GW.user.current_userid != "111111") {
        detailsContent += '<div class="form-check form-check-inline">' +
          '<input class="form-check-input" type="radio" name="confidential" value="TRUE" id="private_radio">' +
          '<label class="form-check-label" for="private_radio">Private</label>' +
          '</div>';
      }
    } else {
      detailsContent += '<div class="form-check form-check-inline">' +
        '<input class="form-check-input" type="radio" name="confidential" value="FALSE" id="public_radio">' +
        '<label class="form-check-label" for="public_radio">Public</label>' +
        '</div>';
      if (GW.user.current_userid == owner && GW.user.current_userid != "111111") {
        detailsContent += '<div class="form-check form-check-inline">' +
          '<input class="form-check-input" type="radio" name="confidential" value="TRUE" checked id="private_radio">' +
          '<label class="form-check-label" for="private_radio">Private</label>' +
          '</div>';
      }
    }
    
    detailsContent += '</div></div>';
    detailsContent += '</form>';
    detailsContent += '</div>';

    // Build the main content with tabs at the top, including details content
    var content = '<div style="width: 100%;">';
    
    // Add toolbar (tabs) at the top, passing detailsContent so it's included directly
    content += this.getToolbar(hostid, hosttype, detailsContent);
    
    content += '</div>';

    $("#main-host-content").html(content);
    
    // Update host name in header after content is loaded
    setTimeout(function() {
      try {
        var hostNameElement = document.getElementById('_host_name');
        if (hostNameElement) {
          var hostName = hostNameElement.value || hostNameElement.textContent || '';
          var headerNameElement = document.getElementById('host-header-name');
          if (headerNameElement && hostName) {
            headerNameElement.textContent = hostName;
          }
        }
      } catch (e) {
        // Ignore if elements not found
      }
    }, 100);

    GW.ssh.current_process_log_length = 0;
    GW.general.switchTab("host");
  },

  viewJupyter: function (history_id) {
    $.ajax({
      url: "log",

      method: "POST",

      data: "type=host&id=" + history_id,
    })
      .done(function (msg) {
        if (msg == "") {
          alert("Cannot find the host history in the database.");

          return;
        }

        msg = $.parseJSON(msg);

        var code = msg.output;

        if (code != null && typeof code != "undefined") {
          if (typeof code != "object") {
            code = $.parseJSON(code);
          }

          var notebook = nb.parse(code.content);

          var rendered = notebook.render();

          var content =
            '<div class="modal-body">' + $(rendered).html() + "</div>";

          content +=
            '<div class="modal-footer">' +
            '   <button type="button" id="host-history-download-btn" class="btn btn-outline-primary">Download</button> ' +
            '   <button type="button" id="host-history-cancel-btn" class="btn btn-outline-secondary">Cancel</button>' +
            "</div>";

          //                  console.log(content);

          GW.host.his_frame = GW.process.createJSFrameDialog(
            800,
            600,
            content,
            "History Jupyter Notebook " + history_id,
          );

          $("#host-history-download-btn").click(function () {
            GW.host.downloadJupyter(history_id);
          });

          $("#host-history-cancel-btn").click(function () {
            GW.host.his_frame.closeFrame();
          });
        }
      })
      .fail(function (e) {
        console.error(e);
      });
  },

  deleteSelectedJupyter: function () {
    if (
      confirm(
        "Are you sure to remove all the selected history? This is permanent.",
      )
    ) {
      $(".hist-checkbox:checked").each(function () {
        var histid = $(this).attr("id");

        console.log("Removing " + histid);

        GW.host.deleteJupyterDirectly(histid.substring(9));
      });
    }
  },

  deleteJupyterDirectly: function (history_id) {
    $.ajax({
      url: "del",

      method: "POST",

      data: "type=history&id=" + history_id,
    }).done(function (msg) {
      if (msg == "") {
        alert("Cannot find the host history in the database.");

        return;
      } else if (msg == "done") {
        console.log("The history " + history_id + " is removed");

        $("#host_history_row_" + history_id).remove();
      } else {
        alert("Fail to delete the jupyter notebook");

        console.error("Fail to delete jupyter: " + msg);
      }
    });
  },

  deleteJupyter: function (history_id) {
    if (confirm("Are you sure to remove this history? This is permanent.")) {
      this.deleteJupyterDirectly(history_id);
    }
  },

  downloadJupyter: function (history_id) {
    $.ajax({
      url: "log",

      method: "POST",

      data: "type=host&id=" + history_id,
    }).done(function (msg) {
      if (msg == "") {
        alert("Cannot find the host history in the database.");

        return;
      }

      msg = $.parseJSON(msg);

      var code = msg.output;

      if (code != null && typeof code != "undefined") {
        if (typeof code != "object") {
          code = $.parseJSON(code);
        }

        //              function download(content, fileName, contentType) {

        var a = document.createElement("a");

        var file = new Blob([JSON.stringify(code.content)], {
          type: "application/json",
        });

        a.href = URL.createObjectURL(file);

        a.target = "_blank";

        a.download = "jupyter-" + history_id + ".ipynb";

        a.click();

        //              }
      }

      //              download(jsonData, 'json.txt', 'text/plain');
    });
  },

  historyTableCellUpdateCallBack: function (updatedCell, updatedRow, oldValue) {
    console.log("The new value for the cell is: " + updatedCell.data());
    console.log("The old value for that cell was: " + oldValue);
    console.log(
      "The values for each cell in that row are: " + updatedRow.data(),
    );

    // The values for each cell in that row are: <input type="checkbox" class="hist-checkbox" id="selected_3naxi3l8o52j">,http://localhost:8888/api/contents/work/GMU%20workspace/COVID/covid_win_laptop.ipynb,xyz,2021-03-03 22:00:32.913,<a href="javascript: GW.host.viewJupyter('3naxi3l8o52j')">View</a> <a href="javascript: GW.host.downloadJupyter('3naxi3l8o52j')">Download</a> <a href="javascript: GW.host.deleteJupyter('3naxi3l8o52j')">Delete</a>

    var thecheckbox = updatedRow.data()[0];

    var hisid = $(thecheckbox).attr("id").substring(9);

    console.log("history id: " + hisid);

    var newvalue = updatedRow.data()[2];

    GW.history.updateNotesOfAHistory(hisid, newvalue);
  },

  recent: function (hid) {
    console.log(
      "Show the history of all previously executed scripts/jupyter notebok",
    );

    $.ajax({
      url: "recent",

      method: "POST",

      data: "type=host&hostid=" + hid + "&number=100",
    })
      .done(function (msg) {
        if (!msg.length) {
          alert("no history found");

          return;
        }

        msg = $.parseJSON(msg);

        var content =
          '<h4 class="border-bottom">Recent History  <button type="button" class="btn btn-secondary btn-sm" id="closeHostHistoryBtn" >close</button></h4>' +
          '<div class="modal-body" style="font-size: 12px;">' +
          '<div class="row"><button type="button" class="btn btn-danger btn-sm" id="deleteHostHistoryBtn" >Delete Selected</button> ' +
          '<button type="button" class="btn btn-danger btn-sm" id="deleteHostHistoryNoNoteBtn" >Delete No-Notes</button> ' +
          '<button type="button" class="btn btn-danger btn-sm" id="deleteHostHistoryAllBtn" >Delete All</button> ' +
          '<button type="button" class="btn btn-primary btn-sm" id="compareHistoryBtn" >Compare</button> ' +
          '<button type="button" class="btn btn-primary btn-sm" id="refreshHostHistoryBtn" >Refresh</button> </div>' +
          '<table class="table host_history_table table-color"> ' +
          "  <thead> " +
          "    <tr> " +
          '      <th scope="col"><input type="checkbox" id="all-selected" ></th> ' +
          '      <th scope="col">Process</th> ' +
          '      <th scope="col" style="width:200px;">Notes (Click to Edit)</th> ' +
          '      <th scope="col">Begin Time</th> ' +
          '      <th scope="col">End Time</th> ' +
          //              "      <th scope=\"col\">Status</th> "+
          '      <th scope="col">Action</th> ' +
          "    </tr> " +
          "  </thead> " +
          "  <tbody> ";

        for (var i = 0; i < msg.length; i++) {
          //                  var status_col = GW.process.getStatusCol(msg[i].id, msg[i].status);

          var detailbtn =
            "      <td ><a href=\"javascript: GW.host.viewJupyter('" +
            msg[i].id +
            "')\">View</a> <a href=\"javascript: GW.host.downloadJupyter('" +
            msg[i].id +
            "')\">Download</a> <a href=\"javascript: GW.host.deleteJupyter('" +
            msg[i].id +
            "')\">Delete</a></td> ";

          content +=
            '    <tr id="host_history_row_' +
            msg[i].id +
            '"> ' +
            '      <td><input type="checkbox" class="hist-checkbox" id="selected_' +
            msg[i].id +
            '" /></td>' +
            "      <td>" +
            msg[i].name +
            "</td> " +
            "      <td>" +
            msg[i].notes +
            "</td> " +
            "      <td>" +
            msg[i].begin_time +
            "</td> " +
            "      <td>" +
            msg[i].end_time +
            "</td> " +
            //                      status_col +
            detailbtn +
            "    </tr>";
        }

        content += "</tbody></div>";

        // Put content in the history-tab-pane
        $("#history-tab-pane").html(content);
        
        // Also update the old container for backward compatibility
        if ($("#host-history-browser").length) {
          $("#host-history-browser").html(content);
        }

        // initialize the tab with editable cells

        var table = $(".host_history_table").DataTable();

        table.MakeCellsEditable({
          onUpdate: GW.host.historyTableCellUpdateCallBack,
          columns: [3],
          allowNulls: {
            columns: [3],
            errorClass: "error",
          },
          confirmationButton: {
            // could also be true
            confirmCss: "my-confirm-class",
            cancelCss: "my-cancel-class",
          },
          inputTypes: [
            {
              column: 3,
              type: "text",
              options: null,
            },
          ],
        });

        //              $("#all-selected").on("click", function(){});

        $("#all-selected").change(function () {
          if ($(this).is(":checked")) {
            //check all the rows
            $(".hist-checkbox").prop("checked", true);
          } else {
            $(".hist-checkbox").prop("checked", false);
          }
        });

        $("#closeHostHistoryBtn").on("click", function () {
          $("#history-tab-pane").html("");
          if ($("#host-history-browser").length) {
            $("#host-history-browser").html("");
          }
        });

        $("#deleteHostHistoryBtn").on("click", function () {
          GW.host.deleteSelectedJupyter();
        });

        $("#deleteHostHistoryNoNoteBtn").on("click", function () {
          GW.history.deleteNoNotesJupyter(hid, GW.host.recent);
        });

        $("#deleteHostHistoryAllBtn").on("click", function () {
          GW.history.deleteAllJupyter(hid, GW.host.recent);
        });

        $("#compareHistoryBtn").on("click", function () {
          GW.comparison.show();
        });

        $("#refreshHostHistoryBtn").on("click", function () {
          GW.host.recent(hid);
        });

        //              var frame = GW.process.createJSFrameDialog(720, 480, content, 'History of ' + msg.name)
      })
      .fail(function (jxr, status) {
        console.error(status);
      });
  },

  getNewDialogContentByHostType: function (host_type) {
    var content = "";

    if (host_type == "jupyter") {
      content =
        '     <div class="form-group row required">' +
        '     <label for="hostname" class="col-sm-3 col-form-label control-label">Host Name </label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="hostname" value="New Host">' +
        "     </div>" +
        "    </div>" +
        '    <div class="form-group row required">' +
        '     <label for="hostname" class="col-sm-3 col-form-label control-label">Jupyter URL </label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="jupyter_home_url" placeholder="http://localhost:8888/">' +
        "     </div>" +
        "    </div>";
    } else if (host_type == "jupyterhub") {
      content =
        '     <div class="form-group row required">' +
        '     <label for="hostname" class="col-sm-3 col-form-label control-label">Host Name </label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="hostname" value="New Host">' +
        "     </div>" +
        "    </div>" +
        '    <div class="form-group row required">' +
        '     <label for="hostname" class="col-sm-3 col-form-label control-label">JupyterHub URL </label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="jupyter_home_url" placeholder="http://localhost:8000/">' +
        "     </div>" +
        "    </div>";
    } else if (host_type == "jupyterlab") {
      content =
        '     <div class="form-group row required">' +
        '     <label for="hostname" class="col-sm-3 col-form-label control-label">Host Name </label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="hostname" value="New Host">' +
        "     </div>" +
        "    </div>" +
        '    <div class="form-group row required">' +
        '     <label for="hostname" class="col-sm-3 col-form-label control-label">JupyterLab URL </label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="jupyter_home_url" placeholder="http://localhost:8888/">' +
        "     </div>" +
        "    </div>";
    } else if (host_type == "ssh") {
      content =
        '     <div class="form-group row required">' +
        '     <label for="hostname" class="col-sm-3 col-form-label control-label">Host Name </label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="hostname" value="New Host">' +
        "     </div>" +
        "    </div>" +
        '    <div class="form-group row required">' +
        '     <label for="hostip" class="col-sm-3 col-form-label control-label">Hose IP</label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="hostip" placeholder="Host IP">' +
        "     </div>" +
        "    </div>" +
        '    <div class="form-group row required">' +
        '     <label for="hostport" class="col-sm-3 col-form-label control-label">Port</label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="hostport" placeholder="">' +
        "     </div>" +
        "    </div>" +
        '    <div class="form-group row required">' +
        '     <label for="username" class="col-sm-3 col-form-label control-label">User Name</label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="username" placeholder="">' +
        "     </div>" +
        "    </div>";
    } else if (host_type == "gee") {
      content =
        '     <div class="form-group row required">' +
        '     <label for="hostname" class="col-sm-3 col-form-label control-label">Host Name </label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="hostname" value="New Host">' +
        "     </div>" +
        "    </div>" +
        '    <div class="form-group row required">' +
        '     <label for="hostname" class="col-sm-3 col-form-label control-label">Client ID </label>' +
        '     <div class="col-sm-9">' +
        '       <input type="text" class="form-control" id="client_id" placeholder="ee.Authenticate client ID..">' +
        "     </div>" +
        "    </div>";
    }

    content +=
      '    <div class="form-group row required">' +
      '     <label for="hostname" class="col-sm-3 col-form-label control-label">Confidential </label>' +
      '     <div class="col-sm-9" style="padding-left: 30px;">' +
      '       <input type="radio" name="confidential" value="FALSE" checked> ' +
      '       <label id="public_radio" for="confidential">Public</label>';

    if (GW.user.current_userid != null && GW.user.current_userid != "111111")
      content +=
        '       <input type="radio" name="confidential" value="TRUE"> ' +
        '       <label id="private_radio" for="confidential">Private</label>';

    content += "     </div>" + "       </div>";

    return content;
  },

  newDialog: function (category) {
    if (GW.host.new_host_frame != null) {
      try {
        GW.host.new_host_frame.closeFrame();
      } catch (e) {
        console.error("Fail to close old frame. Maybe it is already closed.");
      }

      GW.host.new_host_frame = null;
    }

    var content =
      '<div class="modal-body" id="newhostdialog" style="font-size: 12px;">' +
      "<form>" +
      '   <div class="form-group row required">' +
      '     <label for="hosttype" class="col-sm-3 col-form-label control-label">Host Type </label>' +
      '     <div class="col-sm-9">' +
      //             '       <input type="text" class="form-control" id="hosttype" value="Host Type">'+
      '        <select class="form-control" id="hosttype"> ' +
      '            <option value="ssh">SSH Linux/Macintosh/Windows</option> ' +
      //    '            <option value="jupyter">Jupyter Notebook</option> '+
      //    '            <option value="jupyterhub">JupyterHub</option> '+
      //    '            <option value="jupyterlab">Jupyter Lab</option> '+
      //    '            <option value="gee">Google Earth Engine</option>'+
      "        </select> " +
      "     </div>" +
      "   </div>" +
      '   <div id="host_dynamic_form">' +
      this.getNewDialogContentByHostType("ssh") +
      "   </div>" +
      " </form>" +
      "</div>";

    content +=
      '<div class="modal-footer">' +
      '   <button type="button" id="host-add-btn" class="btn btn-outline-primary">Add</button> ' +
      '   <button type="button" id="host-cancel-btn" class="btn btn-outline-secondary">Cancel</button>' +
      "</div>";

    GW.host.new_host_frame = GW.process.createJSFrameDialog(
      500,
      450,
      content,
      "Add new host",
    );

    $("#hosttype").change(function () {
      var op = $("#hosttype option:selected").val();

      $("#host_dynamic_form").html(GW.host.getNewDialogContentByHostType(op));
    });

    $("#host-add-btn").click(function () {
      GW.host.add(function () {
        try {
          GW.host.new_host_frame.closeFrame();
        } catch (e) {}
      });
    });

    $("#host-cancel-btn").click(function () {
      GW.host.new_host_frame.closeFrame();
    });

    if (category) $("#hosttype").val(category).trigger("change");
  },
};
