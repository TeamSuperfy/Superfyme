/* 
 * @author arthur.grohe
 */

// Beispiel.....
// function myObject(){
//       var privatVariable = 'default';
//       return {
//           getVal : function(){
//                return privatVariable;
//           }
//       }
//   }
//
//var obj = myObject();

/**
 * Responsible for superfyme server communications
 * 
 * @param gMap:google.maps.Map Google Map object
 */
function SuperfymeHelper(gMap){
    /**
     * Global superfyme object.
     * Contains static properties and stuff
     */
    var superfyme = {
        /**
         * Defines the functions that can be called
         * by the superfyme server
         */
        types: {
            // Request Posts Map 
            RPM:"RPM",
            // Request Post List
            RPL:"RPL"
        },
        serverUrl: "http://superfyme.com:8080/LokaServer/LokaServlet"
    }
    
    var googleMap = gMap;
    
    var jsonMapPosts;
    
    /**
     *  Sends a request to Superfyme Server
     *  and delegates the response to the corresponding methods
     *  
     *  @param command:string one of the commands specified in superfyme-server-client-protocoll
     *  @param query:string in json format
     */
    function sendRequestToSuperfymeServer(command, query){
        $.ajax({
            type: "POST",
            dataType: "text",
            url: superfyme.serverUrl,
            data: {
                "cmd": command,
                "json": query
            }
        }).done(function ( data ) {
            var type = data.substring(0, data.indexOf('#'));
            jsonResponse = jQuery.parseJSON(data.substr(type.length+1, data.length - type.length));
            // check type and call responsible function
            if(type == superfyme.types.RPM){
                updatePostsMap(jsonResponse);
                // Now that we have the json RPM we can send anoter request
                // to get the RPL json object
                var queryRPL = jsonRequestPostList(jsonResponse);
               // console.log("queryRPL:"+queryRPL);
                sendRequestToSuperfymeServer(superfyme.types.RPL, queryRPL);
            }
            else if(type == superfyme.types.RPL){
               
                updatePostsList(jsonResponse);
            }
        });
    }

    /**
     * returns a json-Loc object, specified in superfyme-server-client-protocoll by alexej.grohe
     * @param latlng:google.maps.LatLng 
     */
    function jsonLoc(latlng){
        return '{"longitude":'+parseInt(latlng.lng() * 1e6)+', "latitude":'+parseInt(latlng.lat() * 1e6)+'}';
    }


    /**
     * returns a json-DoubleLoc object, specified in superfyme-server-client-protocoll by alexej.grohe
     * @param latlng1:google.maps.LatLng
     * @param latlng2:google.maps.LatLng
     */
    function jsonDoubleLoc(latlng1, latlng2){
        return '{"upLeft":'+jsonLoc(latlng1)+', "bottomRight":'+jsonLoc(latlng2)+'}';
    }

    /**
     * builds a json-RPM object,  specified in superfyme-server-client-protocoll by alexej.grohe
     * @param jsonDoubleLoc:String 
     * @param gID:number    group ID
     * @param que:string    query, for filtering
     */
    function jsonRequestPostMap(jsonDoubleLoc, gID, que){
        if(que == undefined || que == ""){
            return '{"L":'+jsonDoubleLoc+', "gID":'+gID+'}';
        }
        else{
            return '{"L":'+jsonDoubleLoc+', "gID":'+gID+', "Que":"'+que+'"}';
        }
    }
    
    /**
     * builds a json-RPL object,  specified in superfyme-server-client-protocoll by alexej.grohe
     * @param jsonPosts a jsonRPM object
     */
    function jsonRequestPostList(jsonPosts){
        if(jsonPosts != undefined && jsonPosts.length > 0){
            var query = "{P:[";
            for(var i=0; i<jsonPosts.length; i++){
                query += ""+jsonPosts[i].postID;
                if((i+1)<jsonPosts.length){
                    query += ",";
                }
            }
            return query += "]}";
        }
        return undefined;
    }
    

    function updatePostsMap(jsonPosts){
        var image = '/superfyme-webapp/javax.faces.resource/marker.png.xhtml?ln=images';
        var myLatLng ;
        var marker;
        for(var i=0; i<jsonPosts.length; i++){
            var post = jsonPosts[i];
            myLatLng = new google.maps.LatLng(post.location.latitude / 1e6, post.location.longitude / 1e6);
            
            marker = new google.maps.Marker({
                position: myLatLng,
                map: googleMap,
                icon: image
            });
            
            //console.log("marker#"+i+" latlng:"+post.location.latitude+","+post.location.longitude);
        }
    }
    
    function updatePostsList(jsonPostList){
        jQuery("#postList").html("");
        for(var i=0; i<jsonPostList.length; i++){
            var post = jsonPostList[i];
            console.log(post);
            jQuery("#postList").append("<div><strong>picID:</strong>"+post.picID+"<br/><p>"+post.text+"</p></div>");
        }
    }

    return {
        /**
         *  Sends a RequestPostMap and RequestPostList-Request to Superfyme Server
         *  
         *  
         */
        sendPostRequest: function(){
            var bounds = googleMap.getBounds();
            var ne = bounds.getNorthEast();
            var sw = bounds.getSouthWest();
            
            var upperLeft = new google.maps.LatLng(ne.lat(), sw.lng());
            var bottomRight = new google.maps.LatLng(sw.lat(), ne.lng());

            var query = jsonRequestPostMap(jsonDoubleLoc(upperLeft, bottomRight), -1, "");
            //console.log(query);
            sendRequestToSuperfymeServer(superfyme.types.RPM, query);
        }

    }
}
