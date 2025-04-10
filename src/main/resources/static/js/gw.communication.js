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
     * Check the server's preferred communication channel
     * This queries the server for its configuration setting
     */
    checkServerPreference: function() {
        var xhr = new XMLHttpRequest();
        xhr.open("GET", "/Geoweaver/api/config/communication-channel", true);
        xhr.timeout = 5000;
        
        xhr.onload = function() {
            if (xhr.status === 200) {
                try {
                    var response = JSON.parse(xhr.responseText);
                    if (response && response.defaultChannel) {
                        // If server prefers polling, start with that
                        if (response.defaultChannel === "polling") {
                            console.log("Server prefers HTTP long polling, starting with that");
                            GW.communication.usingFallback = true;
                            GW.communication.startPolling();
                        } else {
                            // Otherwise use WebSocket (default)
                            console.log("Server prefers WebSocket, starting with that");
                            GW.communication.connect();
                        }
                    } else {
                        // Default to WebSocket if response is invalid
                        GW.communication.connect();
                    }
                } catch (error) {
                    console.error("Error parsing server preference:", error);
                    GW.communication.connect();
                }
            } else {
                // Default to WebSocket if request fails
                GW.communication.connect();
            }
        };
        
        xhr.onerror = function() {
            console.error("Error checking server preference");
            GW.communication.connect();
        };
        
        xhr.ontimeout = function() {
            console.error("Timeout checking server preference");
            GW.communication.connect();
        };
        
        xhr.send();
    },
    
    /**
     * Connect to the server using WebSocket
     * Falls back to long polling if WebSocket connection fails
     */
    connect: function() {
        // Reset connection status
        this.isConnected = false;
        this.usingFallback = false;
        
        try {
            // Get WebSocket URL prefix
            var wsPrefix = (window.location.protocol === "https:" ? "wss://" : "ws://") + 
                          window.location.host + "/Geoweaver/";
            
            // Create WebSocket connection
            this.ws = new WebSocket(wsPrefix + "command-socket");
            
            // Set up WebSocket event handlers
            this.ws.onopen = function(e) {
                console.log("WebSocket connection established");
                GW.communication.isConnected = true;
                
                // Send token to register the session
                setTimeout(function() {
                    GW.communication.send("token:" + GW.communication.token);
                }, 1000);
            };
            
            this.ws.onclose = function(e) {
                console.log("WebSocket connection closed");
                GW.communication.isConnected = false;
                
                // Switch to fallback if not already using it
                if (!GW.communication.usingFallback) {
                    console.log("Switching to long polling fallback");
                    GW.communication.usingFallback = true;
                    GW.communication.startPolling();
                }
            };
            
            this.ws.onerror = function(e) {
                console.error("WebSocket error:", e);
                GW.communication.isConnected = false;
                
                // Switch to fallback if not already using it
                if (!GW.communication.usingFallback) {
                    console.log("WebSocket error, switching to long polling fallback");
                    GW.communication.usingFallback = true;
                    GW.communication.startPolling();
                }
            };
            
            this.ws.onmessage = function(e) {
                // Handle incoming WebSocket messages
                if (GW.communication.messageHandler) {
                    GW.communication.messageHandler(e.data);
                }
            };
        } catch (error) {
            console.error("Error creating WebSocket connection:", error);
            
            // Switch to fallback
            this.usingFallback = true;
            this.startPolling();
        }
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
        xhr.open("GET", "/Geoweaver/api/longpoll/poll/" + this.token, true);
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
     * Send a message to the server
     * Uses WebSocket if available, falls back to HTTP POST if not
     * 
     * @param {string} message - The message to send
     */
    send: function(message) {
        if (!this.usingFallback && this.ws && this.ws.readyState === WebSocket.OPEN) {
            // Use WebSocket
            this.ws.send(message);
        } else {
            // Use HTTP POST fallback
            var xhr = new XMLHttpRequest();
            xhr.open("POST", "/Geoweaver/api/longpoll/send/" + this.token, true);
            xhr.setRequestHeader("Content-Type", "text/plain");
            xhr.send(message);
        }
    },
    
    /**
     * Check connection status and reconnect if needed
     */
    checkConnection: function() {
        if (!this.usingFallback && (!this.ws || this.ws.readyState === WebSocket.CLOSED)) {
            console.log("Connection check: WebSocket is closed, reconnecting...");
            this.connect();
        }
    },
    
    /**
     * Close the connection
     */
    close: function() {
        // Stop polling
        this.stopPolling();
        
        // Close WebSocket if open
        if (this.ws) {
            try {
                this.ws.close();
            } catch (error) {
                console.error("Error closing WebSocket:", error);
            }
            this.ws = null;
        }
        
        this.isConnected = false;
    }
};