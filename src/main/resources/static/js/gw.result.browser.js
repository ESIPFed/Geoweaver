
GW.result.browser = {

    init: function () {

        GW.result.browser.loadFileList();

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
