/**
 * 
 * author: Z.S.
 * date: Mar 12 2021
 * 
 */

GW.history = {

    deleteAllJupyter: function(hostid, callback){

        if(confirm("WARNING: Are you sure to remove all the history? This is permanent and cannot be recovered.")){
            
            $.ajax({
				
                url: "delAllHistory",
                
                method: "POST",
                
                data: { id: hostid}
                
            }).done(function(msg){
                
                console.log("All the history has been deleted, refresh the history table");

                callback(hostid);
    
            }).fail(function(jxr, status){
                    
                console.error(status + " failed to update notes, the server may lose connection. Try again. ");
                
            });
        
        }

    },
    
    deleteNoNotesJupyter: function(hostid, callback){
        
        if(confirm("WARNING: Are you sure to remove all the history without notes? This is permanent and cannot be recovered.")){
            
            $.ajax({
				
                url: "delNoNotesHistory",
                
                method: "POST",
                
                data: { id: hostid}
                
            }).done(function(msg){
                
                console.log("history without notes are deleted, refresh the history table");

                callback(hostid);
    
            }).fail(function(jxr, status){
                    
                console.error(status + " failed to update notes, the server may lose connection. Try again. ");
                
            });
        
        }
        
    },

    updateNotesOfAHistory: function(hisid, notes){

        $.ajax({
				
            url: "edit",
            
            method: "POST",
            
            data: { type: "history", id: hisid, notes: notes}
            
        }).done(function(msg){
            
            console.log("notes is updated");

        }).fail(function(jxr, status){
				
            console.error(status + " failed to update notes, the server may lose connection. Try again. ");
            
        });
    },


}
