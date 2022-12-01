/**
 * Upload file to RPA Server.
 * author@ Dominic D Prabhu
 */

$(document).ready(function() {
  $("#select1").on("change", selectprocess);
  $("#select2").on("change", selectfolder);
  $('#submitFile').click(uploadForm);
});

function uploadForm(){
	
	//stop submit the form, we will post it manually.
    event.preventDefault();

    // disabled the submit button
    $("#submitFile").prop("disabled", true);
    
    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: "/rpa/uploadSingleFile",
        data: new FormData($("#upload-file-form")[0]),
        processData: false,  // Important!
        contentType: false,
        cache: false,
        success: function(data){
            $("#submitFile").prop("disabled", false);
            alert(data);
        },
        error: function(xhr, status, error) {
        	alert(xhr.responseText);
        }
    });
}

function selectprocess() {
  $.ajax({
    url: "/rpa/getProcessesByBankNameForFileUpload",
    type: "GET",
    data: {bankName: $(this).val()}, // parameters
    success: function(result){
		var html = '<option disabled="disabled" selected="selected" value="0">Please select</option>';
		$.each(result, function(i, data){
			html += '<option value="' + data.processName + '">'
            + data.processName + '</option>';
        });
        html += '</option>';
        $('#select2').html(html)
        
	},
    error: function(xhr, status, error) {
    	  alert(xhr.responseText);
    }
  });
} 

function selectfolder() {
  $.ajax({
    url: "/rpa/getFoldersByBankNameForFileUpload",
    type: "GET",
    data: {bankName: $("#select1").val(),processName: $(this).val()}, // parameters
    success: function(result){
		var html = '<option disabled="disabled" selected="selected" value="0">Please select</option>';
		$.each(result, function(i, data){
			html += '<option value="' + data.folderDesc + '">'
            + data.folderDesc + '</option>';
        });
        html += '</option>';
        $('#select3').html(html)
	},
    error: function(xhr, status, error) {
    	  alert(xhr.responseText);
    }
  });
} 