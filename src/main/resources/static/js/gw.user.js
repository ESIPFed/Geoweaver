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
        '       <input type="text" id="username"  name="username" placeholder="username">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="password" name="password" placeholder="password">'+
        "   </div>"+
        "</div>"+

        "<div class=\"row\">"+
        "   <div class=\"col-md-12\" >"+
        "       <button click=\"GW.user.login()\">Sign In</button>"+
        "       <button click=\"GW.user.signupdialog()\">Sign Up</button>"+
        '   </div>'+
        "</div>"+

        "<div class=\"row\">"+
        '   <div class=\"col-md-12\">'+
        
        "       <button click=\"GW.user.forgetpassdialog()\">Forgot Password?</button>"+

        "   </div>"+
        "</div>"+
        
        
        "</div>";
        
        var frame = GW.process.createJSFrameDialog(300, 300, content, "Login")

    },

    /**
     * Send login request
     */
    login: function(){



    },

    /**
     * Sign up a new user
     */
    signup: function(){

        GW.user.closeOtherFrames();

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\">"+
        
        "<div class=\"row\">"+
        
        "   <div class=\"col-md-12\">"+
        '       User Name: <input type="text" id="username"  name="username" placeholder="username">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       Enter Password: <input type="text" id="password" name="password" placeholder="new password">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       Re-enter Password: <input type="text" id="rpassword" name="rpassword" placeholder="re-enter password">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       Email: <input type="text" id="email" name="email" placeholder="email">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       Agree terms: <input type="text" id="email" name="email" placeholder="email">'+
        "   </div>"+
        "</div>"+

        "<div class=\"row\">"+
        "   <div class=\"col-md-12\" >"+
        "       <button click=\"GW.user.register()\">Register</button>"+
        "       <button click=\"GW.user.closeOtherFrames()\">Cancel</button>"+
        '   </div>'+
        "</div>"+

        "<div class=\"row\">"+
        '   <div class=\"col-md-12\">'+
        
        "       <button click=\"GW.user.forgetpassdialog()\">Forgot Password?</button>"+

        "   </div>"+
        "</div>"+
        
        
        "</div>";
        
        var frame = GW.process.createJSFrameDialog(300, 300, content, "Login")

    },

    closeOtherFrames: function(){

        if(GW.user.login_frame!=null) GW.user.login_frame.closeFrame();

        if(GW.user.signup_frame!=null) GW.user.signup_frame.closeFrame();

        if(GW.user.forget_password_frame!=null) GW.user.forget_password_frame.closeFrame();

    },

    signupdialog: function(){

        GW.user.closeOtherFrames();

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\">"+
        
        "<div class=\"row\">"+
        
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="login"  name="login" placeholder="login">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="password" name="password" placeholder="password">'+
        "   </div>"+
        "</div>"+

        "<div class=\"row\">"+
        "   <div class=\"col-md-12\" >"+
        "       <button click=\"GW.user.signup()\">Register</button>"+
        "       <button click=\"GW.user.signupdialog()\">Cancel</button>"+
        '   </div>'+
        "</div>"+

        "<div class=\"row\">"+
        
        '   <div class=\"col-md-12\">'+
        
        "       Already has an account? Click to <button click=\"GW.user.login_dialog()\">Sign In</button>"+

        "   </div>"+

        "</div>"+
        
        
        "</div>";
        
        var frame = GW.process.createJSFrameDialog(300, 300, content, "Sign Up")

    },

    forget_password_dialog: function(){

        GW.user.closeOtherFrames();

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\">"+
        
        "<div class=\"row\">"+
        
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="login"  name="login" placeholder="login">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="password" name="password" placeholder="password">'+
        "   </div>"+
        "</div>"+

        "<div class=\"row\">"+
        "   <div class=\"col-md-12\" >"+
        "       <button click=\"GW.user.login()\">Sign In</button>"+
        "       <button click=\"GW.user.signupdialog()\">Sign Up</button>"+
        '   </div>'+
        "</div>"+

        "<div class=\"row\">"+
        '   <div class=\"col-md-12\">'+
        
        "       <button click=\"GW.user.forgetpassdialog()\">Forgot Password?</button>"+

        "   </div>"+
        "</div>"+
        
        
        "</div>";
        
        var frame = GW.process.createJSFrameDialog(300, 300, content, "Login")

    },

    /**
     * Send logout request
     */
    logout: function(){



    }


}
