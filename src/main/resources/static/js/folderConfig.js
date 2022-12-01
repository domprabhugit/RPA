/**
 * Upload file to RPA Server.
 * author@ Dominic D Prabhu
 */

$(document).ready(function() {
  $("#select1").on("change", selectBank);
  $("#select2").on("change", selectfolder);
  
	$("#buttonFolderConfigDelete").click(function(){

		var valuesChecked = $("#tableFolderConfig input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");

		if(valuesChecked==""){
			toastr.clear();toastr.warning("Please select atleast one Row");
			return false;
		}else{
			$(this).callFolderConfigAjax("deleteFolderConfig", valuesChecked);
		}

	});

	$("#buttonFolderConfigActivate").click(function(){

		var valuesChecked = $("#tableFolderConfig input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");

		if(valuesChecked==""){
			toastr.clear();toastr.warning("Please select atleast one Row");
			return false;
		}else{
			$(this).callFolderConfigAjax("activateFolderConfig", valuesChecked);
		}

	});
  
  var tableFolderConfig = $('#tableFolderConfig').DataTable({
		"autoWidth": false,
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getFolderConfigDetails",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){

		            			   tableFolderConfig.row.add([
		            			                             obj.id,
		            			                             "<input type='checkbox' value='"+obj.id+"' id=''>",
		            			                             obj.processName,
		            			                             obj.customerName,
		            			                             obj.fileType,
		            			                             obj.status,
		            			                             "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.id+"' data-target='#folderConfigModal'>Edit</a>",
		            			                             ]).draw();
		            		   });
		            	   }
		               },
	});
  
  
	$.fn.callFolderConfigAjax = function( method, checkeds ){
		showLoader();
		var commaSeparatedValues ="";
		

		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			timeout : 100000,
			data: { checked: checkeds },

				success: function(data){
					tableFolderConfig.clear().draw();
					tableFolderConfig.ajax.reload();
					toastr.clear();toastr.info(data);
					hideLoader();
				},
				error: function(e){
					hideLoader();
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
		});
	}
	
  $("#buttonFolderConfigRefresh").click(function(){
		tableFolderConfig.clear().draw();
		tableFolderConfig.ajax.reload();
	});
  
  $("#buttonfolderConfigInsert").click(function(){
		$(this).insertFolderConfigAjax("insertFolderConfig", "");
	});
  
  $.fn.insertFolderConfigAjax = function( method, checkeds ){
		showLoader();
		
		var processName = "";
		if($("#select1").val()!=null && $("#select1").val()!=undefined && $("#select1").val()!="0"){
			processName =$("#select1").val();
		}else{
			hideLoader();
			toastr.warning("Please Select process");
			return false;
		}
		
		var customerName = "";
		if($("#select2").val()!=null && $("#select2").val()!=undefined && $("#select2").val()!="0"){
			customerName =$("#select2").val();
		}else{
			hideLoader();
			toastr.warning("Please Select Customer");
			return false;
		}
		
		var fileType = "";
		if($("#select3").val()!=null && $("#select3").val()!=undefined && $("#select3").val()!="0"){
			fileType =$("#select3").val();
		}else{
			hideLoader();
			toastr.warning("Please Select File Type");
			return false;
		}
		
		var folderPath = "";
		if($("#folderpath").val()!=null && $("#folderpath").val()!=undefined && $("#folderpath").val()!=""){
			folderPath =$("#folderpath").val();
		}else{
			hideLoader();
			toastr.warning("Please enter folder path");
			return false;
		}
		
		var isRestrcictedFolder = "";
		if($("#isRestrcictedFolder").val()!=null && $("#isRestrcictedFolder").val()!=undefined && $("#isRestrcictedFolder").val()!=""){
			isRestrcictedFolder =$("#isRestrcictedFolder").val();
		}else{
			hideLoader();
			toastr.warning("Please enter Folder Type");
			return false;
		}
		
		var folderUsername = "";
		if($("#folderUsername").val()!=null && $("#folderUsername").val()!=undefined && $("#folderUsername").val()!=""){
			folderUsername =$("#folderUsername").val();
		}else{
			hideLoader();
			if(isRestrcictedFolder=="Y"){
			toastr.warning("Please enter username ");
			return false;
			}
		}
		
		var folderPassword = "";
		if($("#folderPassword").val()!=null && $("#folderPassword").val()!=undefined && $("#folderPassword").val()!=""){
			folderPassword =$("#folderPassword").val();
		}else{
			hideLoader();
			if(isRestrcictedFolder=="Y"){
			toastr.warning("Please enter password");
			return false;
			}
		}
		
		
		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			timeout : 100000,
			data: { processName : processName, customerName: customerName, fileType: fileType, folderPath: folderPath, isRestrcictedFolder: isRestrcictedFolder,folderUsername: folderUsername,folderPassword: folderPassword  },

			success: function(data){
				toastr.clear();toastr.info(data);
				hideLoader();
				tableFolderConfig.clear().draw();
				tableFolderConfig.ajax.reload();
				$("#select1,#select2,#select3").val('0');
				$("#folderpath").val('');
			},
			error: function(e){
				hideLoader();
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
			}
		});
  };
  
  $("#buttonfolderConfigUpdate").click(function(){
		$(this).updateFolderConfigAjax("updateFolderConfig", "");
	});

$.fn.updateFolderConfigAjax = function( method, checkeds ){
		showLoader();
		
		var folderPath = "";
		if($("#folderPathModal").val()!=null && $("#folderPathModal").val()!=undefined && $("#folderPathModal").val()!=""){
			folderPath =$("#folderPathModal").val();
		}else{
			hideLoader();
			toastr.warning("Please enter folder path");
			return false;
		}
		
		
		var isRestrcictedFolderModal = "";
		if($("#isRestrcictedFolderModal").val()!=null && $("#isRestrcictedFolderModal").val()!=undefined && $("#isRestrcictedFolderModal").val()!=""){
			isRestrcictedFolderModal =$("#isRestrcictedFolderModal").val();
		}else{
			hideLoader();
			toastr.warning("Please enter Folder Type");
			return false;
		}
		
		var folderUsernameModal = "";
		if($("#folderUsernameModal").val()!=null && $("#folderUsernameModal").val()!=undefined && $("#folderUsernameModal").val()!=""){
			folderUsernameModal =$("#folderUsernameModal").val();
		}else{
			hideLoader();
			if(isRestrcictedFolderModal=="Y"){
			toastr.warning("Please enter username ");
			return false;
			}
		}
		
		var folderPasswordModal = "";
		if($("#folderPasswordModal").val()!=null && $("#folderPasswordModal").val()!=undefined && $("#folderPasswordModal").val()!=""){
			folderPasswordModal =$("#folderPasswordModal").val();
		}else{
			hideLoader();
			if(isRestrcictedFolderModal=="Y"){
			toastr.warning("Please enter password");
			return false;
			}
		}
		
		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			timeout : 100000,
			data: { id : $("#folderProcess_id").val(), folderPath: folderPath, isRestrcictedFolder: isRestrcictedFolderModal,folderUsername: folderUsernameModal,folderPassword: folderPasswordModal},

			success: function(data){
				toastr.clear();toastr.info(data);
				hideLoader();
				tableFolderConfig.clear().draw();
				tableFolderConfig.ajax.reload();
			},
			error: function(e){
				hideLoader();
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
			}
		});
};
  
  
  $('#folderConfigModal').on('show.bs.modal', function(e) {

		var $modal = $(this),
		esseyId = e.relatedTarget.id;
		$.ajax({
			cache: false,
			type: 'POST',
			url: "/rpa/getFolderConfigById",
			data: { id: esseyId },
			success: function(data) {
				$(".modal-body #folderProcess_id").val( data.id );
				$(".modal-body #processNameModal").val( data.processName );
				$(".modal-body #customerNameModal").val( data.customerName );
				$(".modal-body #fileTypeModal").val( data.fileType );
				$(".modal-body #folderPathModal").val(data.folderPath );
				
				$(".modal-body #isRestrcictedFolderModal").val(data.isRestrictedFolder );
				if(data.isRestrictedFolder=="N"){
					$(".modal-body #folderUsernameModal").val("");
					$(".modal-body #folderPasswordModal").val("");
					$(".restrictedFieldsModal").hide();
				}else{
				$(".modal-body #folderUsernameModal").val(data.username );
				$(".modal-body #folderPasswordModal").val(data.password );
				$(".restrictedFieldsModal").show();
				}
			},
			error: function(e){
				toastr.clear();toastr.error("Record not found.");
			}
		});
	});
  
  $("#isRestrcictedFolder").change(function (){
		var val = $("#isRestrcictedFolder").val();
		if(val!=""){
			if(val=="Y"){
				$(".restrictedFields").show();
			}else{
				$(".restrictedFields").hide();
			}
		}
	});
  
  $("#isRestrcictedFolderModal").change(function (){
		var val = $("#isRestrcictedFolderModal").val();
		if(val!=""){
			if(val=="Y"){
				$(".restrictedFieldsModal").show();
			}else{
				$(".restrictedFieldsModal").hide();
			}
		}
	});
  
  
  
  
});


function selectBank() {
  $.ajax({
    url: "/rpa/getBanksByProcessName",
    type: "POST",
    data: {processName: $(this).val()}, // parameters
    success: function(result){
		var html = '<option disabled="disabled" selected="selected" value="0">Please select</option>';
		$.each(result, function(i, data){
			html += '<option value="' + data.customerName + '">'
            + data.customerName + '</option>';
        });
        html += '</option>';
        $('#select2').html(html);
        
       /* $("#select2 option").val(function(idx, val) {
      	  $(this).siblings('[value="'+ val +'"]').remove();
      	});*/
        
	},
    error: function(xhr, status, error) {
    	  alert(xhr.responseText);
    }
  });

} 

function selectfolder() {
  $.ajax({
    url: "/rpa/getFileTypesByBankAndProcessName",
    type: "POST",
    data: {custName: $("#select2").val(),processName: $("#select1").val()}, // parameters
    success: function(result){
		var html = '<option disabled="disabled" selected="selected" value="0">Please select</option>';
		$.each(result, function(i, data){
			html += '<option value="' + data.fileType + '">'
            + data.fileType + '</option>';
        });
        html += '</option>';
        $('#select3').html(html)
	},
    error: function(xhr, status, error) {
    	  alert(xhr.responseText);
    }
  });
} 

