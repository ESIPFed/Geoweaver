
GW.result.browser = {

    init: function () {

        // GW.result.browser.loadFileList();
        GW.result.browser.render_file_list()

        $('#result-refresh-button').on('click', function() {
            GW.result.browser.loadFolderContents("", GW.result.browser.fileTable);; // Reload the data from the server
        });

    },

    render_file_list: function(){
        GW.result.browser.fileTable = $('#file-list-table').DataTable({
            columns: [
                { data: 'name', render: function (data, type, row) {
                    let icon = '<i class="fas fa-file"></i>'; // Default icon
                    if (row.isDirectory) {
                        icon = '<i class="fas fa-folder"></i>'; // Folder icon
                    } else {
                        const ext = data.split('.').pop().toLowerCase(); // Get file extension
                        switch (ext) {
                            case 'pdf':
                                icon = '<i class="fas fa-file-pdf"></i>';
                                break;
                            case 'doc':
                            case 'docx':
                                icon = '<i class="fas fa-file-word"></i>';
                                break;
                            case 'xls':
                            case 'xlsx':
                                icon = '<i class="fas fa-file-excel"></i>';
                                break;
                            case 'jpg':
                            case 'jpeg':
                            case 'png':
                            case 'gif':
                                icon = '<i class="fas fa-file-image"></i>';
                                break;
                            case 'txt':
                                icon = '<i class="fas fa-file-alt"></i>';
                                break;
                            default:
                                icon = '<i class="fas fa-file"></i>';
                        }
                    }
                    return icon + ' ' + data; // Return icon + file name
                }},
                { data: 'size', render: function (data, type, row) {
                    if (row.isDirectory) return ''; // Don't show size for directories
                    return GW.result.browser.formatFileSize(data); // Convert size to appropriate unit
                }},
                { data: 'modified' },
                {
                    data: null,
                    render: function (data, type, row) {
                        if (row.isDirectory) {
                            return ''; // No actions for directories
                        }
                        
                        const ext = row.name.split('.').pop().toLowerCase();
                        let actionButtons = '';
    
                        // "Download" button for all files
                        actionButtons += `<a href="#" class="btn btn-primary btn-download" data-path="${row.path}">Download</a> `;
    
                        // "Display" button for image files
                        if (['jpg', 'jpeg', 'png', 'gif'].includes(ext)) {
                            actionButtons += `<a href="#" class="btn btn-info btn-display" data-path="${row.path}">Display</a>`;
                        }
    
                        return actionButtons;
                    }
                }
            ]
        });

        GW.result.browser.loadFolderContents("", GW.result.browser.fileTable);

        // Add click event to folder rows
        $('#file-list-table tbody').on('click', 'tr td:first-child', function () {
            var rowData = GW.result.browser.fileTable.row($(this).closest('tr')).data();
            if (rowData.isDirectory) {
                // If the row is a folder, navigate into it
                var path = rowData.path;
                GW.result.browser.loadFolderContents(path, GW.result.browser.fileTable);
            }
        });

        // Download button click event
        $('#file-list-table tbody').on('click', '.btn-download', function (e) {
            e.preventDefault(); // Prevent default action
            console.log("download called once")
            var filePath = $(this).data('path');
            window.open('/Geoweaver/download?path=' + encodeURIComponent(filePath), '_blank');
        });

        // Display button click event for images
        $('#file-list-table tbody').on('click', '.btn-display', function (e) {
            e.preventDefault(); // Prevent default action
            console.log("display called once")
            var path = $(this).data('path'); // Get the value of the data-path attribute
            console.log('Display path:', path); // Print the data-path value to the console
            var filePath = $(this).data('path');
            var imgWindow = window.open('', '_blank');
            imgWindow.document.write('<img src="/Geoweaver/download?path=' + encodeURIComponent(filePath) + '" style="width:100%">');
        });

    },

    formatFileSize: function(bytes){
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },

    // Function to load folder contents
    loadFolderContents: function(folderPath = '', fileTable) {
        $('#file-list-table').before(`
            <div id="loading-message" style="text-align: center; margin-bottom: 10px;">
                <i class="fas fa-spinner fa-spin" style="font-size: 24px; margin-right: 10px;"></i>
                Loading Folder `+folderPath+` ...
            </div>
        `);

        // Clear existing table data
        fileTable.clear().draw();

        $.ajax({
            url: '/Geoweaver/results', // API endpoint to get file list
            data: { subfolder: folderPath }, // Send the current folder path
            method: 'GET',
            success: function (data) {
                // Remove loading message
                $('#loading-message').remove();
                if(folderPath!=""){
                    var newItem = {
                        "path": GW.result.browser.resolvePath(folderPath+"/.."),
                        "size": 0,
                        "name": "..",
                        "modified": "",
                        "isDirectory": true
                    };
                    data.unshift(newItem);
                }
                
                // Add new data to the table
                fileTable.rows.add(data);
                fileTable.draw();
            },
            error: function (err) {
                // Remove loading message
                $('#loading-message').remove();

                // Handle error (e.g., display an error message)
                alert('Failed to load data: ' + error);
            }
        });
    },

    resolvePath: function(folderPath) {
        const parts = folderPath.split('/'); // Split path into parts
        const resolvedParts = [];
    
        for (let part of parts) {
            if (part === '' || part === '.') {
                continue; // Ignore empty or current directory parts
            }
            if (part === '..') {
                resolvedParts.pop(); // Go up one directory
            } else {
                resolvedParts.push(part); // Add valid part to the result
            }
        }
    
        return resolvedParts.join('/');
    },

    loadFileList: function() {
        $.ajax({
            url: '/Geoweaver/results',
            method: 'GET',
            success: function(data) {
                // Populate the file list
                $('#result-file-list').empty();
                data.forEach(function(filename) {
                    $('#result-file-list').append(`
                        <li class="list-group-item file-item" data-filename="${filename}">${filename}</li>
                    `);
                });

                // Add click event to each file item
                $('.file-item').click(function() {
                    let selectedFile = $(this).data('filename');
                    GW.result.browser.previewFile(selectedFile);
                });
            },
            error: function(err) {
                console.error("Error fetching file list", err);
            }
        });
    },

    previewFile: function(filename) {
        // Determine if file is an image or text based on file extension
        const isImage = /\.(jpg|jpeg|png|gif)$/.test(filename);

        if (isImage) {
            // Display image
            $('#image-preview').attr('src', `/Geoweaver/results/${filename}`).show();
            $('#text-preview').hide();
        } else {
            // Display text content
            $.ajax({
                url: `/Geoweaver/results/${filename}`,
                method: 'GET',
                success: function(data) {
                    $('#text-preview').text(data).show();
                    $('#image-preview').hide();
                },
                error: function(err) {
                    console.error("Error fetching file content", err);
                }
            });
        }
    }
    
}
