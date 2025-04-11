/**
 * Communication module for Geoweaver
 * 
 * Provides a unified interface for communication between client and server
 * with automatic fallback from WebSocket to HTTP long polling when WebSocket
 * connections fail (e.g., due to proxy issues).
 */

GW.communication = {
    // WebSocket connection
    ws: null,
    
    // Token for identifying the client
    token: null,
    
    // Flag to track if we're using the fallback mechanism
    usingFallback: false,
    
    // Polling interval in milliseconds
    pollingInterval: 1000,
    
    // Flag to track if polling is active
    isPolling: false,
    
    // Callback for handling messages
    messageHandler: null,
    
    // Flag to track connection status
    isConnected: false,
    
    /**
     * Initialize the communication module
     * 
     * @param {string} token - The client token
     * @param {function} messageHandler - Callback for handling messages
     */
    init: function(token, messageHandler) {
        this.token = token;
        this.messageHandler = messageHandler;
        
        // Check server's preferred communication channel
        this.checkServerPreference();
    },
    
    /**
     * Initialize communication using HTTP long polling
     * WebSocket support has been removed, polling is now the primary method
     */
    checkServerPreference: function() {
        console.log("Using HTTP long polling as primary communication channel");
        GW.communication.usingFallback = true;
        GW.communication.startPolling();
    },
    
    /**
     * Connect to the server using HTTP long polling
     * WebSocket support has been removed
     */
    connect: function() {
        // Reset connection status
        this.isConnected = false;
        this.usingFallback = true;
        
        console.log("Initializing HTTP long polling connection");
        
        // Start polling immediately
        this.startPolling();
        
        // Set connection as established
        this.isConnected = true;
        
        // Send token to register the session
        setTimeout(function() {
            // Use HTTP POST to send the token
            var xhr = new XMLHttpRequest();
            xhr.open("POST", GW.path.getBasePath() + "api/longpoll/send/" + GW.communication.token, true);
            xhr.setRequestHeader("Content-Type", "text/plain");
            xhr.send("token:" + GW.communication.token);
        }, 1000);
    },
    
    /**
     * Start long polling for messages
     */
    startPolling: function() {
        if (this.isPolling) {
            return; // Already polling
        }
        
        this.isPolling = true;
        this.poll();
    },
    
    /**
     * Stop long polling
     */
    stopPolling: function() {
        this.isPolling = false;
    },
    
    /**
     * Poll for messages using long polling
     */
    poll: function() {
        if (!this.isPolling) {
            return; // Polling has been stopped
        }
        
        var xhr = new XMLHttpRequest();
        xhr.open("GET", GW.path.getBasePath() + "api/longpoll/poll/" + this.token, true);
        xhr.timeout = 35000; // Slightly longer than server timeout
        
        xhr.onload = function() {
            if (xhr.status === 200) {
                // Process messages
                try {
                    var messages = JSON.parse(xhr.responseText);
                    if (messages && messages.length > 0) {
                        for (var i = 0; i < messages.length; i++) {
                            if (GW.communication.messageHandler) {
                                GW.communication.messageHandler(messages[i]);
                            }
                        }
                    }
                } catch (error) {
                    console.error("Error parsing long polling response:", error);
                }
            }
            
            // Continue polling
            setTimeout(function() {
                GW.communication.poll();
            }, 100); // Small delay before next poll
        };
        
        xhr.onerror = function() {
            console.error("Long polling request failed");
            
            // Retry after delay
            setTimeout(function() {
                GW.communication.poll();
            }, GW.communication.pollingInterval);
        };
        
        xhr.ontimeout = function() {
            // This is normal for long polling - just start a new request
            GW.communication.poll();
        };
        
        xhr.send();
    },
    
    /**
     * Send a message to the server using HTTP POST
     * WebSocket support has been removed
     * 
     * @param {string} message - The message to send
     */
    send: function(message) {
        // Use HTTP POST for all communication
        var xhr = new XMLHttpRequest();
        xhr.open("POST", GW.path.getBasePath() + "api/longpoll/send/" + this.token, true);
        xhr.setRequestHeader("Content-Type", "text/plain");
        xhr.send(message);
    },
    
    /**
     * Check connection status and reconnect if needed
     */
    checkConnection: function() {
        if (!this.isConnected) {
            console.log("Connection check: Polling connection is not active, reconnecting...");
            this.connect();
        }
    },
    
    /**
     * Close the connection
     */
    close: function() {
        // Stop polling
        this.stopPolling();
        
        // Reset connection state
        this.isConnected = false;
    }
};