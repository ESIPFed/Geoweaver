/**
 * Manage users
 */

GW.user = {

    login_frame: null,

    signup_frame: null,

    forget_password_frame: null,

    /**
     * Initialization
     */
    init: function(){


    },

    /**
     * Pop up the login dialog
     */
    login_dialog: function(){

        GW.user.closeOtherFrames();

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\">"+
        
        "<div class=\"row\">"+
        
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="username" class=\"input-lg\" name="username" placeholder="username">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="password" class=\"input-lg\" name="password" placeholder="password">'+
        "   </div>"+
        "</div>"+

        "<div class=\"row\">"+
        "   <div class=\"col-md-12\" >"+
        "       <button onclick=\"GW.user.login()\" class=\"btn btn-lg btn-primary\" >Sign In</button>"+
        "       <button onclick=\"GW.user.signupdialog()\"  class=\"btn btn-lg btn-primary\">Sign Up</button>"+
        '   </div>'+
        "</div>"+

        "<div class=\"row\">"+
        '   <div class=\"col-md-12\">'+
        
        "       <button onclick=\"GW.user.forgetpassdialog()\"  class=\"btn btn-lg btn-primary\">Forgot Password?</button>"+

        "   </div>"+
        "</div>"+
        
        
        "</div>";
        
        this.login_frame = GW.process.createJSFrameDialog(300, 400, content, "Login")

    },

    precheck: function(){

        var isvalid = true;

        if(!$("#username").val()){

            isvalid = false;

        }else if(!$("#password").val()){

            isvalid = false;

        }

        if(!isvalid){

            alert("Either usename or password is missing. ");

        }

        return isvalid;

    },

    /**
     * Send login request
     */
    login: function(){

        if(this.precheck()){

            //password need to be encrypted first

            $.ajax({

                url: "signin",
				
                method: "POST",
            
                data: { usename: $("#username").val(), password: $("#password").val()}

            }).done(function(msg){

                msg = $.parseJSON(msg);



            }).fail(function(jxr, status){

                console.error("Sign In Failed");

            });

        }

    },

    /**
     * Sign up a new user
     */
    signup: function(){



    },

    closeOtherFrames: function(){

        if(GW.user.login_frame!=null) GW.user.login_frame.closeFrame();

        if(GW.user.signup_frame!=null) GW.user.signup_frame.closeFrame();

        if(GW.user.forget_password_frame!=null) GW.user.forget_password_frame.closeFrame();

    },

    signupdialog: function(){

        console.log("Enter sign up dialog..");

        GW.user.closeOtherFrames();

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\">"+
        
        "<div class=\"row\">"+
        
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="username" class=\"input-lg\" name="username" placeholder="username"/>'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="password" class=\"input-lg\" name="password" placeholder="new password"/>'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="rpassword" class=\"input-lg\" name="rpassword" placeholder="re-enter password"/>'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="email" class=\"input-lg\" name="email" placeholder="email"/>'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       Agree terms: <input type="text" id="email" class=\"input-lg\" name="email" placeholder="email"/>'+
        "   </div>"+
        "</div>"+

        "<div class=\"row\">"+
        "   <div class=\"col-md-12\" >"+
        "       <button onclick=\"GW.user.register()\"  class=\"btn btn-lg btn-primary\">Register</button>"+
        "       <button onclick=\"GW.user.closeOtherFrames()\" class=\"btn btn-lg btn-primary\">Cancel</button>"+
        '   </div>'+
        "</div>"+

        "<div class=\"row\">"+
        '   <div class=\"col-md-12\">'+
        
        "       <button onclick=\"GW.user.forgetpassdialog()\" class=\"btn btn-lg btn-primary\">Forgot Password?</button>"+

        "   </div>"+
        "</div>"+
        
        
        "</div>";
        
        this.signup_frame = GW.process.createJSFrameDialog(400, 400, content, "Registration")

    },

    register: function(){

        if(this.precheck()){

            $.ajax({

                url: "forgetpassword",
				
                method: "POST",
            
                data: { recover_email: $("#recover_email").val()}

            }).done(function(msg){
    
    
    
            }).fail(function(jxr, status){
    
    
            });

        }

    },

    resetPassword: function(){

        if(this.precheck()){

            $.ajax({



            });

        }

    },

    forget_password_dialog: function(){

        GW.user.closeOtherFrames();

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\">"+
        
        "   <div class=\"row\">"+
        
        "      <div class=\"col-md-12\">"+
        '          <input type="text" id="recover_email"  class=\"input-lg\" name="recover_email" placeholder="email address">'+
        "      </div>"+
        "   </div>"+

        "   <div class=\"row\">"+
        "      <div class=\"col-md-12\" >"+
        "          <button onclick=\"GW.user.resetPassword()\"  class=\"btn btn-lg btn-primary\">Send</button>"+
        "          <button onclick=\"GW.user.closeOtherFrames()\"  class=\"btn btn-lg btn-primary\">Cancel</button>"+
        '      </div>'+
        "   </div>"+
        
        "</div>";
        
        this.forget_password_frame = GW.process.createJSFrameDialog(400, 400, content, "Login")

    },

    /**
     * Send logout request
     */
    logout: function(){



    }


}
