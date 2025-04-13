/**
 * Geoweaver Chatbot Module
 * Provides AI assistant functionality for Geoweaver
 */

GW.chatbot = {
    isOpen: false,
    isMinimized: false,
    conversations: [],
    currentWorkflow: null,
    currentModel: null,
    isTyping: false,
    isConfigured: false,

    init: function() {
        // Check if LLM API is configured before creating the UI
        this.checkApiConfiguration();
    },
    
    checkApiConfiguration: function() {
        // Make an AJAX call to check if the LLM API is configured
        $.ajax({
            url: GW.path.getBasePath() + 'api/chatbot/config-status',
            type: 'GET',
            success: (response) => {
                this.isConfigured = response.configured;
                
                // Only create the chatbot UI if the API is configured
                if (this.isConfigured) {
                    // Create chatbot UI if it doesn't exist
                    if (!document.getElementById('chatbot-panel')) {
                        this.createChatbotUI();
                    }
                    
                    // Add event listeners
                    this.addEventListeners();
                    
                    // Initialize with a welcome message
                    this.addMessage('ai', 'Hello! I\'m your Geoweaver AI assistant. I can help you create and modify regression models, analyze workflow results, and automate tasks. How can I help you today?');
                }
            },
            error: (error) => {
                console.error('Error checking chatbot configuration:', error);
                this.isConfigured = false;
            }
        });
    },

    createChatbotUI: function() {
        // Only create UI elements if the API is configured
        if (!this.isConfigured) return;
        
        // Create toggle button
        const toggleButton = document.createElement('div');
        toggleButton.className = 'chatbot-toggle-button';
        toggleButton.innerHTML = '<i class="fas fa-robot"></i>';
        toggleButton.id = 'chatbot-toggle-button';
        document.body.appendChild(toggleButton);

        // Create chatbot panel
        const chatbotPanel = document.createElement('div');
        chatbotPanel.className = 'chatbot-panel hidden';
        chatbotPanel.id = 'chatbot-panel';
        
        chatbotPanel.innerHTML = `
            <div class="chatbot-header" id="chatbot-header">
                <h3>Geoweaver AI Assistant</h3>
                <div class="chatbot-header-buttons">
                    <button class="chatbot-header-button" id="chatbot-minimize-button">
                        <i class="fas fa-minus"></i>
                    </button>
                    <button class="chatbot-header-button" id="chatbot-close-button">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            </div>
            <div class="chatbot-body" id="chatbot-body"></div>
            <div class="chatbot-input-container">
                <input type="text" class="chatbot-input" id="chatbot-input" placeholder="Type your message here...">
                <button class="chatbot-send-button" id="chatbot-send-button">
                    <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        `;
        
        document.body.appendChild(chatbotPanel);
    },

    addEventListeners: function() {
        // Toggle chatbot visibility
        document.getElementById('chatbot-toggle-button').addEventListener('click', () => {
            this.toggleChatbot();
        });

        // Close chatbot
        document.getElementById('chatbot-close-button').addEventListener('click', () => {
            this.closeChatbot();
        });

        // Minimize chatbot
        document.getElementById('chatbot-minimize-button').addEventListener('click', () => {
            this.minimizeChatbot();
        });

        // Send message on button click
        document.getElementById('chatbot-send-button').addEventListener('click', () => {
            this.sendMessage();
        });

        // Send message on Enter key
        document.getElementById('chatbot-input').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.sendMessage();
            }
        });

        // Make header draggable for repositioning
        this.makeDraggable(document.getElementById('chatbot-panel'), document.getElementById('chatbot-header'));
    },

    makeDraggable: function(element, handle) {
        let pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
        
        handle.onmousedown = dragMouseDown;

        function dragMouseDown(e) {
            e = e || window.event;
            e.preventDefault();
            // Get the mouse cursor position at startup
            pos3 = e.clientX;
            pos4 = e.clientY;
            document.onmouseup = closeDragElement;
            // Call a function whenever the cursor moves
            document.onmousemove = elementDrag;
        }

        function elementDrag(e) {
            e = e || window.event;
            e.preventDefault();
            // Calculate the new cursor position
            pos1 = pos3 - e.clientX;
            pos2 = pos4 - e.clientY;
            pos3 = e.clientX;
            pos4 = e.clientY;
            // Set the element's new position
            element.style.top = (element.offsetTop - pos2) + "px";
            element.style.left = (element.offsetLeft - pos1) + "px";
            element.style.bottom = 'auto';
            element.style.right = 'auto';
        }

        function closeDragElement() {
            // Stop moving when mouse button is released
            document.onmouseup = null;
            document.onmousemove = null;
        }
    },

    toggleChatbot: function() {
        const chatbotPanel = document.getElementById('chatbot-panel');
        chatbotPanel.classList.toggle('hidden');
        this.isOpen = !chatbotPanel.classList.contains('hidden');
        
        if (this.isOpen) {
            document.getElementById('chatbot-toggle-button').style.display = 'none';
            // Scroll to bottom of chat
            this.scrollToBottom();
        } else {
            document.getElementById('chatbot-toggle-button').style.display = 'flex';
        }
    },

    closeChatbot: function() {
        document.getElementById('chatbot-panel').classList.add('hidden');
        document.getElementById('chatbot-toggle-button').style.display = 'flex';
        this.isOpen = false;
    },

    minimizeChatbot: function() {
        const chatbotPanel = document.getElementById('chatbot-panel');
        chatbotPanel.classList.toggle('minimized');
        this.isMinimized = chatbotPanel.classList.contains('minimized');
        
        // Change minimize button icon
        const minimizeButton = document.getElementById('chatbot-minimize-button');
        if (this.isMinimized) {
            minimizeButton.innerHTML = '<i class="fas fa-expand"></i>';
        } else {
            minimizeButton.innerHTML = '<i class="fas fa-minus"></i>';
            this.scrollToBottom();
        }
    },

    sendMessage: function() {
        const inputElement = document.getElementById('chatbot-input');
        const message = inputElement.value.trim();
        
        if (message === '') return;
        
        // Add user message to chat
        this.addMessage('user', message);
        
        // Clear input
        inputElement.value = '';
        
        // Show typing indicator
        this.showTypingIndicator();
        
        // Send message to backend
        this.sendToBackend(message);
    },

    addMessage: function(sender, content) {
        const chatbotBody = document.getElementById('chatbot-body');
        const messageElement = document.createElement('div');
        messageElement.className = `chatbot-message ${sender}`;
        
        // Process markdown-like formatting
        content = this.formatMessage(content);
        
        messageElement.innerHTML = content;
        chatbotBody.appendChild(messageElement);
        
        // Save to conversation history
        this.conversations.push({
            sender: sender,
            content: content,
            timestamp: new Date()
        });
        
        // Scroll to bottom
        this.scrollToBottom();
    },

    formatMessage: function(content) {
        // Convert code blocks
        content = content.replace(/```(\w*)\n([\s\S]*?)```/g, function(match, language, code) {
            return `<pre><code class="language-${language}">${code.trim()}</code></pre>`;
        });
        
        // Convert inline code
        content = content.replace(/`([^`]+)`/g, '<code>$1</code>');
        
        // Convert line breaks
        content = content.replace(/\n/g, '<br>');
        
        return content;
    },

    showTypingIndicator: function() {
        if (this.isTyping) return;
        
        this.isTyping = true;
        const chatbotBody = document.getElementById('chatbot-body');
        const typingElement = document.createElement('div');
        typingElement.className = 'chatbot-typing ai';
        typingElement.id = 'chatbot-typing';
        typingElement.innerHTML = `
            <div class="dot"></div>
            <div class="dot"></div>
            <div class="dot"></div>
        `;
        chatbotBody.appendChild(typingElement);
        
        this.scrollToBottom();
    },

    hideTypingIndicator: function() {
        const typingElement = document.getElementById('chatbot-typing');
        if (typingElement) {
            typingElement.remove();
        }
        this.isTyping = false;
    },

    scrollToBottom: function() {
        const chatbotBody = document.getElementById('chatbot-body');
        chatbotBody.scrollTop = chatbotBody.scrollHeight;
    },

    sendToBackend: function(message) {
        // Make API call to backend
        $.ajax({
            url: GW.path.getBasePath() + 'api/chatbot/message',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                message: message,
                workflowId: this.currentWorkflow,
                modelId: this.currentModel,
                conversationHistory: this.conversations
            }),
            success: (response) => {
                this.hideTypingIndicator();
                this.handleResponse(response);
            },
            error: (error) => {
                this.hideTypingIndicator();
                console.error('Error sending message to chatbot:', error);
                this.addMessage('ai', 'Sorry, I encountered an error. Please try again later.');
            }
        });
    },

    handleResponse: function(response) {
        // Add AI response to chat
        this.addMessage('ai', response.message);
        
        // Handle any actions from the AI
        if (response.actions && response.actions.length > 0) {
            this.processActions(response.actions);
        }
        
        // Update current workflow/model if provided
        if (response.workflowId) {
            this.currentWorkflow = response.workflowId;
        }
        
        if (response.modelId) {
            this.currentModel = response.modelId;
        }
        
        // Display metrics if available
        if (response.metrics) {
            this.displayMetrics(response.metrics);
        }
    },

    processActions: function(actions) {
        actions.forEach(action => {
            switch(action.type) {
                case 'open_workflow':
                    // Open workflow in Geoweaver
                    if (action.workflowId) {
                        GW.workflow.load(action.workflowId);
                    }
                    break;
                    
                case 'run_workflow':
                    // Run workflow
                    if (action.workflowId) {
                        GW.workflow.execute(action.workflowId);
                    }
                    break;
                    
                case 'modify_process':
                    // Modify process code
                    if (action.processId && action.code) {
                        GW.process.updateCode(action.processId, action.code);
                    }
                    break;
                    
                case 'create_process':
                    // Create new process
                    if (action.name && action.code && action.language) {
                        GW.process.createNew(action.name, action.code, action.language);
                    }
                    break;
                    
                case 'display_progress':
                    // Display progress update
                    this.displayProgress(action.progress, action.message);
                    break;
            }
        });
    },

    displayProgress: function(progress, message) {
        const chatbotBody = document.getElementById('chatbot-body');
        const progressElement = document.createElement('div');
        progressElement.className = 'chatbot-progress';
        progressElement.innerHTML = `
            <h4>${message || 'Progress Update'}</h4>
            <div class="chatbot-progress-bar">
                <div class="chatbot-progress-bar-fill" style="width: ${progress}%"></div>
            </div>
            <div>${progress}% Complete</div>
        `;
        chatbotBody.appendChild(progressElement);
        this.scrollToBottom();
    },

    displayMetrics: function(metrics) {
        const chatbotBody = document.getElementById('chatbot-body');
        const metricsElement = document.createElement('div');
        metricsElement.className = 'chatbot-progress';
        
        let metricsHTML = '<h4>Model Evaluation Metrics</h4><div class="chatbot-metrics">';
        
        for (const [key, value] of Object.entries(metrics)) {
            metricsHTML += `<div class="chatbot-metric">${key}: ${value}</div>`;
        }
        
        metricsHTML += '</div>';
        metricsElement.innerHTML = metricsHTML;
        
        chatbotBody.appendChild(metricsElement);
        this.scrollToBottom();
    },

    // Helper method to extract metrics from workflow output
    extractMetricsFromOutput: function(output) {
        const metrics = {};
        
        // Look for common metrics patterns in the output
        const r2Match = output.match(/R²\s*[:=]\s*([0-9.]+)/i);
        if (r2Match) metrics.R2 = parseFloat(r2Match[1]);
        
        const maeMatch = output.match(/MAE\s*[:=]\s*([0-9.]+)/i);
        if (maeMatch) metrics.MAE = parseFloat(maeMatch[1]);
        
        const rmseMatch = output.match(/RMSE\s*[:=]\s*([0-9.]+)/i);
        if (rmseMatch) metrics.RMSE = parseFloat(rmseMatch[1]);
        
        const mseMatch = output.match(/MSE\s*[:=]\s*([0-9.]+)/i);
        if (mseMatch) metrics.MSE = parseFloat(mseMatch[1]);
        
        return metrics;
    }
};

// Chatbot is initialized in GW.main.js
// CSS is included in head.html template