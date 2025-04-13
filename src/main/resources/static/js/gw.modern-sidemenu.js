/**
 * Modern Side Menu functionality for Geoweaver
 * Enhances the left panel with modern UI interactions
 */

GW.modernSidemenu = {
    init: function() {
        console.log("Initializing modern side menu...");
        
        // Initialize search functionality
        this.initSearch();
        
        // Initialize category toggles
        this.initCategoryToggles();
        
        // Initialize responsive behavior
        this.initResponsiveBehavior();
        
        // Initialize item hover effects
        this.initItemEffects();
    },
    
    /**
     * Initialize search functionality
     */
    initSearch: function() {
        const searchInput = document.getElementById('instant_search_bar');
        const clearButton = document.getElementById('clean_search_field');
        
        if (!searchInput || !clearButton) return;
        
        // Show/hide clear button based on input content
        searchInput.addEventListener('input', function() {
            if (this.value.length > 0) {
                clearButton.style.opacity = '1';
            } else {
                clearButton.style.opacity = '0';
            }
            
            // Use the existing search functionality from GW.search
            if (typeof GW.search !== 'undefined' && GW.search.search) {
                GW.search.search(this.value);
            }
        });
        
        // Clear search input
        clearButton.addEventListener('click', function() {
            searchInput.value = '';
            clearButton.style.opacity = '0';
            
            // Reset search results
            if (typeof GW.search !== 'undefined' && GW.search.resetSearch) {
                GW.search.resetSearch();
            }
            
            // Focus back on the input
            searchInput.focus();
        });
    },
    
    /**
     * Initialize category toggle functionality
     */
    initCategoryToggles: function() {
        // Category headers toggle
        const categoryHeaders = document.querySelectorAll('.modern-category-header');
        
        categoryHeaders.forEach(header => {
            header.addEventListener('click', function() {
                // Toggle the expanded class on the next sibling (subcategory)
                const subcategory = this.nextElementSibling;
                if (subcategory && subcategory.classList.contains('modern-subcategory')) {
                    subcategory.classList.toggle('expanded');
                    this.classList.toggle('collapsed');
                }
            });
        });
        
        // Subcategory headers toggle
        const subcategoryHeaders = document.querySelectorAll('.modern-subcategory-header');
        
        subcategoryHeaders.forEach(header => {
            header.addEventListener('click', function() {
                // Toggle the expanded class on the next sibling (items)
                const items = this.nextElementSibling;
                if (items && items.classList.contains('modern-subcategory')) {
                    items.classList.toggle('expanded');
                    this.classList.toggle('collapsed');
                }
            });
        });
    },
    
    /**
     * Initialize responsive behavior
     */
    initResponsiveBehavior: function() {
        const collapseBtn = document.querySelector('.modern-side-menu-collapse-btn');
        const sideMenu = document.querySelector('.modern-nav-side-menu');
        const sideMenuContainer = document.getElementById('sidemenu');
        
        if (!collapseBtn || !sideMenu || !sideMenuContainer) return;
        
        collapseBtn.addEventListener('click', function() {
            sideMenuContainer.classList.toggle('collapsed');
            
            // Update button icon
            const icon = this.querySelector('i');
            if (icon) {
                if (sideMenuContainer.classList.contains('collapsed')) {
                    icon.classList.remove('fa-chevron-left');
                    icon.classList.add('fa-chevron-right');
                    
                    // Collapse the menu
                    sideMenuContainer.style.width = '60px';
                    document.querySelectorAll('.modern-category-header .title, .modern-category-header .new-btn, .modern-subcategory').forEach(el => {
                        el.style.display = 'none';
                    });
                    document.querySelectorAll('.modern-category-header').forEach(el => {
                        el.style.justifyContent = 'center';
                    });
                } else {
                    icon.classList.remove('fa-chevron-right');
                    icon.classList.add('fa-chevron-left');
                    
                    // Expand the menu
                    sideMenuContainer.style.width = '';
                    document.querySelectorAll('.modern-category-header .title, .modern-category-header .new-btn').forEach(el => {
                        el.style.display = '';
                    });
                    document.querySelectorAll('.modern-subcategory.expanded').forEach(el => {
                        el.style.display = '';
                    });
                    document.querySelectorAll('.modern-category-header').forEach(el => {
                        el.style.justifyContent = '';
                    });
                }
            }
        });
        
        // Handle window resize
        window.addEventListener('resize', function() {
            if (window.innerWidth <= 768) {
                // Mobile view
                if (!sideMenuContainer.classList.contains('collapsed')) {
                    sideMenu.classList.add('show');
                }
            } else {
                // Desktop view
                sideMenu.classList.remove('show');
            }
        });
    },
    
    /**
     * Initialize item hover effects and actions
     */
    initItemEffects: function() {
        // Add hover effects to items
        const items = document.querySelectorAll('.modern-item');
        
        items.forEach(item => {
            // Handle click to set active state
            item.addEventListener('click', function() {
                // Remove active class from all items
                document.querySelectorAll('.modern-item').forEach(i => {
                    i.classList.remove('active');
                });
                
                // Add active class to clicked item
                this.classList.add('active');
            });
        });
    },
    
    /**
     * Update the side menu with new items
     * @param {string} type - The type of items (host, process, workflow)
     * @param {Array} items - Array of items to display
     */
    updateItems: function(type, items) {
        if (!type || !items) return;
        
        const container = document.getElementById(`${type}_folder_${type}_target`) || 
                         document.getElementById(`${type}s`);
        
        if (!container) return;
        
        // Clear existing items
        container.innerHTML = '';
        
        // Add new items
        items.forEach(item => {
            const itemElement = document.createElement('li');
            itemElement.className = 'modern-item';
            itemElement.id = `${type}-${item.id}`;
            
            // Create item content
            itemElement.innerHTML = `
                <i class="${this.getIconForType(type)} item-icon"></i>
                <span class="item-title">${item.name}</span>
                <div class="item-actions">
                    <button type="button" class="edit-btn" title="Edit">
                        <i class="fa fa-edit"></i>
                    </button>
                    <button type="button" class="delete-btn" title="Delete">
                        <i class="fa fa-trash"></i>
                    </button>
                </div>
            `;
            
            // Add click handler
            itemElement.addEventListener('click', function(e) {
                // Prevent bubbling if clicking on action buttons
                if (e.target.closest('.item-actions')) {
                    e.stopPropagation();
                    return;
                }
                
                // Call appropriate handler based on type
                if (type === 'host') {
                    GW.menu.details(item.id, 'host');
                } else if (type === 'process') {
                    GW.menu.details(item.id, 'process');
                } else if (type === 'workflow') {
                    GW.menu.details(item.id, 'workflow');
                }
            });
            
            // Add action button handlers
            const editBtn = itemElement.querySelector('.edit-btn');
            const deleteBtn = itemElement.querySelector('.delete-btn');
            
            if (editBtn) {
                editBtn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    // Call appropriate edit function
                    if (type === 'host') {
                        GW.host.edit(item.id);
                    } else if (type === 'process') {
                        GW.process.edit(item.id);
                    } else if (type === 'workflow') {
                        GW.workflow.edit(item.id);
                    }
                });
            }
            
            if (deleteBtn) {
                deleteBtn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    GW.menu.del(item.id, type);
                });
            }
            
            container.appendChild(itemElement);
        });
    },
    
    /**
     * Get appropriate icon class for item type
     * @param {string} type - The type of item
     * @returns {string} - Font Awesome icon class
     */
    getIconForType: function(type) {
        switch(type) {
            case 'host':
                return 'fa fa-server';
            case 'process':
                return 'fa fa-cog';
            case 'workflow':
                return 'fa fa-cogs';
            default:
                return 'fa fa-file';
        }
    }
};

// Initialize when document is ready
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're using the modern side menu
    if (document.querySelector('.modern-nav-side-menu')) {
        GW.modernSidemenu.init();
    }
});