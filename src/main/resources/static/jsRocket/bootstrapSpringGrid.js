
var omnicheckURL = "";

$(document).ready(function(){
	
	//$(".omniStatusYear,.omniStatusMonth,.omniStatusDate,.omniStatusDiv").hide();
	
	var today = new Date();
	var dd = today.getDate();
	var mm = today.getMonth() + 1; //January is 0!

	var yyyy = today.getFullYear();
	if (dd < 10) {
	  dd = '0' + dd;
	} 
	if (mm < 10) {
	  mm = '0' + mm;
	} 
	var today = dd + '/' + mm + '/' + yyyy;
	$("#policyPdfMailRetriggerDate").val(today);
	
	 var d = new Date();
     for (var i = 0; i <= 10; i++) {
         var option = "<option value=" + parseInt(d.getFullYear() - i) + ">" + parseInt(d.getFullYear() - i) + "</option>"
         $('[id=omniStatusYear]').append(option);
         $('[id=dashboardStatusYear]').append(option);
     }
     for (var i = 0; i <= 2; i++) {
         var option = "<option value=" + parseInt(d.getFullYear() - i) + ">" + parseInt(d.getFullYear() - i) + "</option>"
         $('[id=incentiveYear]').append(option);
     }
     $("#incentiveYear").val(yyyy);
     $("#incentiveMonth").val(mm);
     
     $(".yearSpan").text(yyyy);
     
     var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
     for (var i = 1; i <= 12; i++) {
         var option = "<option value=" + ("0" + i).slice(-2) + ">" + monthNames[i-1] + "</option>"
         $('[id=omniStatusMonth]').append(option);
     }
     
     $("#omniStatusType").change(function(){
    	 var statusType = $("#omniStatusType").val();
    	 if(statusType=="1"){
    		 $(".omniStatusYear,.omniStatusMonth,.omniStatusDate,.omniStatusDiv").hide();
    		 $(".omniDateWiseStatus").show();
    	 }else if(statusType=="2"){
    		 $(".omniStatusYear,.omniStatusDiv").show();
    		 $(".omniDateWiseStatus,.omniStatusMonth,.omniStatusDate").hide();
    	 }else if(statusType=="3"){
    		 $(".omniStatusYear,.omniStatusMonth,.omniStatusDiv").show();
    		 $(".omniDateWiseStatus,.omniStatusDate").hide();
    	 }else if(statusType=="4"){
    		 $(".omniStatusDate,.omniStatusDiv").show();
    		 $(".omniStatusYear,.omniStatusMonth,.omniDateWiseStatus").hide();
    	 }
    	 
     });
     
	omnicheckURL = $("#marutiDmsCheckHostDetails").val();

	 $('#forgetPasswordModal').on('show.bs.modal', function(e) {
		 $('#forgetPasswordModal .input-lg').val('');
	 });
	 
	 $('#changePasswordModal').on('hidden.bs.modal', function () {
		 window.location.href = "/rpa/login";
	 });
	 
	$("input[name='processState']").on("change", function() {
		showLoader();
		var isChecked = $(this).is(':checked');
		var selectedData;
		var $switchLabel = $('.switch-label');

		if(isChecked) {
			selectedData = $switchLabel.attr('data-on');
		} else {
			selectedData = $switchLabel.attr('data-off');
		}

		$.ajax({
			type: "GET",
			url: "/rpa/controlroute",
			timeout : 100000,
			data: { processState: selectedData, processName: $(this).attr("value") },
			success: function(data){
				hideLoader();
				toastr.clear();
				toastr.warning(data);
				setTimeout(function() 
						{
						location.reload();  //Refresh page
						}, 8000);
			},
			error: function(e){
				hideLoader();
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
			}
		});
	});

	$(".statusFilter").hide();
	// Activated the table
	var tableUser = $('#tableUser').DataTable({
		"autoWidth": false,
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getAllUsers",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){

		            			   tableUser.row.add([
		            			                      obj.id,
		            			                      "<input type='checkbox' value='"+obj.id+"' id=''>",
		            			                      obj.username,
		            			                      obj.emailId,
		            			                      obj.phoneNo,
		            			                      obj.active,
		            			                      "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.id+"' data-target='#userModal'>Edit</a>",
		            			                      ]).draw();
		            		   });
		            	   }
		               },
	});


	$('#userModal').on('show.bs.modal', function(e) {

		var $modal = $(this),
		esseyId = e.relatedTarget.id;
		$.ajax({
			cache: false,
			type: 'POST',
			url: "/rpa/viewUser",
			data: { id: esseyId },
			success: function(data) {
				$(".modal-body #usernameModal").val( data.username );
				$(".modal-body #emailIdModal").val( data.emailId );
				$(".modal-body #phoneNoModal").val( data.phoneNo );
				$(".modal-body #roleNameModal").val( data.roleName );
				if(data.processName.indexOf(",")!=-1){
					$.each(data.processName.split(","), function(i,e){
						$(".modal-body #processNameModal option[value='" + e + "']").prop("selected", true);
					});
				}else{
					$(".modal-body #processNameModal").val( data.processName );
				}
			},
			error: function(e){
				toastr.clear();toastr.error("Record not found.");
			}
		});
	});


	var tableEmailConfig = $('#tableEmailConfig').DataTable({
		"autoWidth": false,
		"fnDrawCallback": function( oSettings ) {
			$("#tableEmailConfig tbody tr").find("td:eq(2),td:eq(3)").css("max-width","200px");
		},
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getEmailDetails",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){

		            			   tableEmailConfig.row.add([
		            			                             obj.id,
		            			                             "<input type='checkbox' value='"+obj.id+"' id=''>",
		            			                             obj.processName,
		            			                             obj.toEmailIds,
		            			                             obj.ccEmailIds,
		            			                             obj.status,
		            			                             "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.id+"' data-target='#emailConfigModal'>Edit</a>",
		            			                             ]).draw();
		            		   });
		            	   }
		               },
	});

	$('#emailConfigModal').on('show.bs.modal', function(e) {

		var $modal = $(this),
		esseyId = e.relatedTarget.id;
		$.ajax({
			cache: false,
			type: 'POST',
			url: "/rpa/viewEmailConfigModal",
			data: { id: esseyId },
			success: function(data) {
				$(".emailConfigDynamicDivs").remove();
				$(".modal-body #processNameModal option").hide();
				$("#emailConfigId").val(data.id);
				if(data.toEmailIds.indexOf(",")!=-1){
					var toEmailIdsArr = data.toEmailIds.split(",");
					for(var i =0;i<toEmailIdsArr.length;i++){
						if(i==0){
							$(".modal-body #toEmailIdsModal_0").val( toEmailIdsArr[i] );
						}else{
							var txt ='<div class="input-group m-b-1 toemail_VW emailConfigDynamicDivs"><span class="input-group-addon">@</span> <input  name="toEmailIdsModal" type="text" class="form-control" placeholder="TO" value="'+toEmailIdsArr[i]+'" /><span class="input-group-addon" onclick="removeToEmail(this);" style="font-size:x-large;">-</span></div>';
							$("#toEmailIdsModalDiv").append(txt);
						}
					}
				}else{
					$(".modal-body #toEmailIdsModal_0").val( data.toEmailIds );
				}

				if(data.ccEmailIds!=null){
					if(data.ccEmailIds.indexOf(",")!=-1){
						var ccEmailIdsArr = data.ccEmailIds.split(",");
						for(var i =0;i<ccEmailIdsArr.length;i++){
							if(i==0){
								$(".modal-body #ccEmailIdsModal_0").val( ccEmailIdsArr[i] );
							}else{
								var txt ='<div class="input-group m-b-1 copyemail_VW emailConfigDynamicDivs"><span class="input-group-addon">@</span> <input  name="ccEmailIdsModal" type="text" class="form-control" placeholder="CC" value="'+ccEmailIdsArr[i]+'" /><span class="input-group-addon" onclick="removeCopyEmail(this);" style="font-size:x-large;">-</span></div>';
								$("#ccEmailIdsModalDiv").append(txt);
							}
						}
					}else{
						$(".modal-body #ccEmailIdsModal_0").val( data.ccEmailIds );
					}
				}

				if(data.processName.indexOf(",")!=-1){
					$.each(data.processName.split(","), function(i,e){
						$(".modal-body #processNameModal option[value='" + e + "']").prop("selected", true);
						$(".modal-body #processNameModal option[value='" + e + "']").show();
					});
				}else{
					$(".modal-body #processNameModal").val( data.processName );
					$(".modal-body #processNameModal option[value='" + data.processName + "']").show();
				}
			},
			error: function(e){
				toastr.clear();toastr.error("Record not found.");
			}
		});
	});

	var tableTransaction = $('#tableTransaction').DataTable( {
	
	} );

	$("#pickerDateBirth").datetimepicker({
		format: 'DD/MM/YYYY'
	}).on('dp.show dp.update', function () {
		$(".picker-switch").css('cursor','none');
		$(".picker-switch").removeAttr('title')
		    //.css('cursor', 'default')  <-- this is not needed if the CSS above is used
		    //.css('background', 'inherit')  <-- this is not needed if the CSS above is used
		    .on('click', function (e) {
		        e.stopPropagation();
		    });
		});
	$("#startDate").datetimepicker({
		format: 'DD/MM/YYYY',
		defaultDate:new Date(),
		showClose:true
		//language:"en"

	}).on('dp.show dp.update', function () {
		$(".picker-switch").css('cursor','none');
		$(".picker-switch").removeAttr('title')
		    //.css('cursor', 'default')  <-- this is not needed if the CSS above is used
		    //.css('background', 'inherit')  <-- this is not needed if the CSS above is used
		    .on('click', function (e) {
		        e.stopPropagation();
		    });
		});
	
	$("#endDate").datetimepicker({
		format: 'DD/MM/YYYY',
		defaultDate:new Date(),
		showClose:true
	}).on('dp.show dp.update', function () {
		$(".picker-switch").css('cursor','none');
		$(".picker-switch").removeAttr('title')
		    //.css('cursor', 'default')  <-- this is not needed if the CSS above is used
		    //.css('background', 'inherit')  <-- this is not needed if the CSS above is used
		    .on('click', function (e) {
		        e.stopPropagation();
		    });
		});

	$(window).load(function() {

	});

	$("#buttonRefresh").click(function(){

		tableUser.clear().draw();
		tableUser.ajax.reload();

	});

	$("#buttonEmailConfigRefresh").click(function(){
		tableEmailConfig.clear().draw();
		tableEmailConfig.ajax.reload();
	});

	$("#buttonInsert").click(function(){
		//toastr.clear();toastr.warning
		if (!validateUserName($("#username").val())) {
			toastr.clear();toastr.warning('Please enter a valid User Name.');
			return false;
		} else if (!validateEmail($("#emailId").val())) {
			toastr.clear();toastr.warning('Please enter a valid Email Id.');
			return false;
		} else if (!validatePhoneNumber($("#phoneNo").val())) {
			toastr.clear();toastr.warning('Please enter a valid Phone Number.');
			return false;
		} else if($("#roleName").val()=="0"){
			toastr.clear();toastr.warning("Please select a Role.")
			return false;
		} else if($("#processName").val()==null){
			toastr.clear();toastr.warning("Please select a Business Process.")
			return false;
		}

		$(this).callAjax("insertUser", "");

		$(".form-control").val("");

	});

	$("#buttonUpdate").click(function(){

		if (!validateEmail($("#emailIdModal").val())) {
			toastr.clear();toastr.warning('Please enter a valid Email Id.');
			return false;
		} else if (!validatePhoneNumber($("#phoneNoModal").val())) {
			toastr.clear();toastr.warning('Please enter a valid Phone Number.');
			return false;
		} else if($("#roleNameModal").val()==""){
			toastr.clear();toastr.warning("Please select a Role.")
			return false;
		} else if($("#processNameModal").val()==null){
			toastr.clear();toastr.warning("Please select a Business Process.")
			return false;
		}

		var valuesChecked = $("#tableUser input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");

		$(this).callUpdateAjax("updateUser", valuesChecked);

	});

	$("#buttonDelete").click(function(){

		var valuesChecked = $("#tableUser input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");

		if(valuesChecked==""){
			toastr.clear();toastr.warning("Please select atleast one user");
			return false;
		}else{
			$(this).callAjax("deleteUser", valuesChecked);
		}

	});

	$("#buttonEmailConfigDelete").click(function(){

		var valuesChecked = $("#tableEmailConfig input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");

		if(valuesChecked==""){
			toastr.clear();toastr.warning("Please select atleast one Process");
			return false;
		}else{
			$(this).callAjax("deleteEmailConfig", valuesChecked);
		}

	});

	$("#buttonEmailConfigActivate").click(function(){

		var valuesChecked = $("#tableEmailConfig input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");

		if(valuesChecked==""){
			toastr.clear();toastr.warning("Please select atleast one Process");
			return false;
		}else{
			$(this).callAjax("activateEmailConfig", valuesChecked);
		}

	});


	$("#buttonBlock").click(function(){

		var valuesChecked = $("#tableUser input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");
		if(valuesChecked==""){
			toastr.clear();toastr.warning("Please select atleast one user");
			return false;
		}else{
			$(this).callAjax("blockUser", valuesChecked);
		}

	});

	$("#buttonActivate").click(function(){

		var valuesChecked = $("#tableUser input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");
		if(valuesChecked==""){
			toastr.clear();toastr.warning("Please select atleast one user");
			return false;
		}else{
			$(this).callAjax("activateUser", valuesChecked);
		}

	});

	$("#buttonResetPwd").click(function(){

		var valuesChecked = $("#tableUser input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");

		if(valuesChecked==""){
			toastr.clear();toastr.warning("Please select atleast one user");
			return false;
		}else{
			if (confirm("Are you sure you want to reset password?")) {
				$(this).callAjax("resetPassword", valuesChecked);
			} else {
				return false;
			}
		}

	});

	$("#changePasswordBtn").click(function(){
		var value = $("#cpassword").val();
		if(value.length<4){
			toastr.clear();toastr.warning("Password should have min length 0f 4");
			return false;
		}else if($("#cpassword").val()!=$("#repassword").val()){
			toastr.clear();toastr.warning("Passwords should be same");
			return false;
		}else{
			$("#changePasswordBtn").prop("disabled",false);
			$(this).callPwdAjax("changePassword", "");
			$(".form-control").val("");
		}
	});


	$("#forgetPasswordBtn").click(function(){

		if($("#reg_username").val()==""){
			toastr.clear();toastr.warning("Username cannot be empty");
			return false;
		}else if($("#reg_email").val()==""){
			toastr.clear();toastr.warning("Email cannot be empty");
			return false;
		}else{
			$(this).callchangePwdAjax("getNewPassword", "");
		}

	});


	$("#buttonFilter").click(function(){

		if($("#startDate").val()=="") {
			toastr.clear();toastr.warning('Please enter a valid Start Date.');
			return false;
		} else if($("#endDate").val()=="") {
			toastr.clear();toastr.warning('Please enter a valid End Date.');
			return false;
		} else if($("#processName").val()==""){
			toastr.clear();toastr.warning("Please select a Process.")
			return false;
		}

		$(this).filterTransactionDetailAjax("filterTransactionDetails", "");

	});


	$("#successFileDownload").click(function(){

		if($("#successFilePath").val().length > 0) {
			var successFilePath = $("#successFilePath").val();
			var linkURL = "?downloadFilePath="+successFilePath;
			$("#hiddenAnchor").attr("href","/rpa/downloadXLS"+linkURL);
			$("#hiddenAnchor")[0].click();
		} else {
			if($("#transactionStatus").val() !="Success") {
				toastr.clear();toastr.warning('No Success File');
				return false;
			}

			var extRef = $("#externalTransactionRefNo").val();
			//var startDt =  moment( $("#glStartDate").val(),"DD/MM/YYYY HH:mm:ss:sss").format("DD/MM/YYYY");
			var startDt =  $("#glStartDate").val();
			var trnStatus =  $("#transactionStatus").val();
			var linkURL = "?externalTranRefNo="+extRef+"&startDate="+startDt+"&status="+trnStatus;

			$("#hiddenAnchor").attr("href","/rpa/getFileDownload"+linkURL);
			$("#hiddenAnchor")[0].click();
		}
	});

	$("#errorFileDownload").click(function(){

		if($("#errorFilePath").val().length > 0) {
			var errorFilePath = $("#errorFilePath").val();
			var linkURL = "?downloadFilePath="+errorFilePath;
			$("#hiddenAnchor").attr("href","/rpa/downloadXLS"+linkURL);
			$("#hiddenAnchor")[0].click();
		} else {
			if($("#transactionStatus").val() !="Error") {
				toastr.clear();toastr.warning('No Error File');
				return false;
			}		
			var extRef = $("#externalTransactionRefNo").val();
			var startDt =  moment( $("#glStartDate").val(),"DD/MM/YYYY HH:mm:ss:sss").format("DD/MM/YYYY");
			var startDt =  $("#glStartDate").val();

			var trnStatus =  $("#transactionStatus").val();
			var linkURL = "?externalTranRefNo="+extRef+"&startDate="+startDt+"&status="+trnStatus;

			$("#hiddenAnchor").attr("href","/rpa/getFileDownload"+linkURL);
			$("#hiddenAnchor")[0].click();
		}
	});

	$("#uploadFileDownload").click(function(){

		if($("#uploadFilePath").val().length > 0) {
			var uploadFilePath = $("#uploadFilePath").val();
			var linkURL = "?downloadFilePath="+uploadFilePath;
			$("#hiddenAnchor").attr("href","/rpa/downloadXLS"+linkURL);
			$("#hiddenAnchor")[0].click();
		} 
	});
	
	$("#logFileDownload").click(function(){

		if($("#logFilePath").val().length > 0) {
			var successFilePath = $("#logFilePath").val();
			var linkURL = "?downloadFilePath="+successFilePath;
			$("#hiddenAnchor").attr("href","/rpa/downloadXLS"+linkURL);
			$("#hiddenAnchor")[0].click();
		} else {
				toastr.clear();toastr.warning('No Log File');
				return false;
			}
	});

	$.fn.callAjax = function( method, checkeds ){
		showLoader();
		var commaSeparatedValues ="";
		if($("#processName").val()!=null && $("#processName").val()!=undefined){
			commaSeparatedValues =$("#processName").val().join(",");
		}

		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			timeout : 100000,
			data: { username: $("#username").val(), emailId: $("#emailId").val(), phoneNo: $("#phoneNo").val(), roleName: $("#roleName").val(), 
				processName: commaSeparatedValues, checked: checkeds },

				success: function(data){
					tableUser.clear().draw();
					tableUser.ajax.reload();
					tableEmailConfig.clear().draw();
					tableEmailConfig.ajax.reload();
					toastr.clear();toastr.info(data);
					hideLoader();
				},
				error: function(e){
					hideLoader();
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
		});
	}


	$.fn.callUpdateAjax = function( method, checkeds ){
		showLoader();
		var commaSeparatedValues ="";
		if($("#processNameModal").val()!=null && $("#processNameModal").val()!=undefined){
			commaSeparatedValues =$("#processNameModal").val().join(",");
		}

		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			timeout : 100000,
			data: { username: $("#usernameModal").val(), emailId: $("#emailIdModal").val(), phoneNo: $("#phoneNoModal").val(), roleName: $("#roleNameModal").val(), 
				processName: commaSeparatedValues, checked: checkeds },

				success: function(data){
					tableUser.clear().draw();
					tableUser.ajax.reload();
					toastr.clear();toastr.info(data);
					hideLoader();
				},
				error: function(e){
					hideLoader();
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
		});
	} 

	$.fn.filterTransactionDetailAjax = function( method, checkeds ){
		showLoader();
		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			dataType: "json",
			timeout : 100000,
			data: { startDate: $("#startDate").val(), endDate: $("#endDate").val(), processName: $("#processName").val(), checked: checkeds },

			"success" :  function(data){
				hideLoader();
				tableTransaction.clear().draw();
				$.each(data, function(ind, obj){

					var FetchId="#viewTranModal";
					if($("#processName").val()=="FirstGenBatchProcess"){
						FetchId="#glBatchModal";		
					}else if($("#processName").val()=="IlongueLifelineInwardUpload"){
						FetchId="#uploadTranModal";	
					}else if($("#processName").val()=="policyExtractionMaruti" || $("#processName").val()=="policyExtractionMarutiBacklog"
						|| $("#processName").val()=="policyExtractionHonda" || $("#processName").val()=="policyExtractionHondaBacklog"
						|| $("#processName").val()=="policyExtractionFord" || $("#processName").val()=="policyExtractionHondaBackFord"
						|| $("#processName").val()=="policyExtractionTataPV" || $("#processName").val()=="policyExtractionTataPVBacklog"
						|| $("#processName").val()=="policyExtractionTataCV" || $("#processName").val()=="policyExtractionTataCVBacklog"
						|| $("#processName").val()=="policyExtractionAbibl" || $("#processName").val()=="policyExtractionAbiblBacklog"
						|| $("#processName").val()=="policyExtractionMibl" || $("#processName").val()=="policyExtractionMiblBacklog"
						|| $("#processName").val()=="policyExtractionVolvo" || $("#processName").val()=="policyExtractionVolvoBacklog"
						|| $("#processName").val()=="policyExtractionTafe" || $("#processName").val()=="policyExtractionTafeBacklog"
						|| $("#processName").val()=="policyExtractionPiaggio" || $("#processName").val()=="policyExtractionTafePiaggio"
						|| $("#processName").val()=="PolicyExtracterFirstgen" || $("#processName").val()=="PolicyExtracterFirstgenBacklog"
						|| $("#processName").val()=="adroitExtractor" || $("#processName").val()=="adroitExtractorBacklog"
						|| $("#processName").val()=="autoInskeptExtractor" || $("#processName").val()=="autoInskeptExtractorBacklog"
						|| $("#processName").val()=="virDocumentExtractor" || $("#processName").val()=="virDocumentExtractorBacklog"){
						FetchId="#marutiPolicyExtractionTranModal";	
					}else if($("#processName").val()=="policyUploadMaruti" || $("#processName").val()=="policyUploadMarutiBacklog" 
						|| $("#processName").val()=="policyUploadHonda" || $("#processName").val()=="policyUploadHondaBacklog"
						|| $("#processName").val()=="policyUploadFord" || $("#processName").val()=="policyUploadFordaBacklog"
						|| $("#processName").val()=="policyUploadTata" || $("#processName").val()=="policyUploadTataPVBacklog"
						|| $("#processName").val()=="policyUploadTataCV" || $("#processName").val()=="policyUploadTataCVBacklog"
						|| $("#processName").val()=="policyUploadAbibl" || $("#processName").val()=="policyUploadAbiblBacklog"
						|| $("#processName").val()=="policyUploadMibl" || $("#processName").val()=="policyUploadMiblBacklog"
						|| $("#processName").val()=="policyUploadVolvo" || $("#processName").val()=="policyUploadVolvoBacklog"
						|| $("#processName").val()=="policyUploadTafe" || $("#processName").val()=="policyUploadTafeBacklog"
						|| $("#processName").val()=="policyUploadPiaggio" || $("#processName").val()=="policyUploadPiaggioBacklog"
						|| $("#processName").val()=="policyUploadFirstgen" || $("#processName").val()=="policyUploadFirstgenBacklog"
							|| $("#processName").val()=="docUploadAutoInspektVir" || $("#processName").val()=="docUploadAutoInspektVirBacklog"
								|| $("#processName").val()=="docUploadAdroitVir" || $("#processName").val()=="docUploadAdroitVirBacklog"
									|| $("#processName").val()=="docUploadVirApp" || $("#processName").val()=="docUploadVirAppBacklog"){
						FetchId="#marutiPolicyUploadTranModal";	
					}else if($("#processName").val()=="GridMasterUploadProcess" || $("#processName").val()=="ModelCodeCreationProcess" || $("#processName").val()=="GridWithModelSheetMasterUploadProcess" ){
						FetchId="#gridUploadModal";	
					}else if($("#processName").val()=="OmniDocClaimsDocDownload"){
						FetchId="#claimsDownloadTranModal";	
						$("#claimsDownload_TotalClaims_cs").val( '-' );
						$("#claimsDownload_processedClaims_cs").val( '-' );
						$("#claimsDownload_withFile_cs").val( '-' );
						$("#claimsDownload_withoutFile_cs").val( '-' );
					}else if($("#processName").val()=="securedFileTransfer"){
						FetchId="#dailyFileTransferTranModal";	
					}else if($("#processName").val()=="policyPDFMailRetrigger"){
						FetchId="#policyPdfMailTriggerTranModal";	
					}else if($("#processName").val()=="AgentIncentiveCalculatorProcess"){
						FetchId="#AgentIncentiveCalculatorProcessTranModal";	
					}
					
					if($("#processName").val()=="policyPDFMailRetrigger"){
						//$("#tableTransaction thead").find("th:eq(3)").text("Transaction Status");
						
						tableTransaction.row.add([
						                          obj.id,
						                          "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.id+"' data-target='"+FetchId+"'>Fetch Results</a>",
						                          obj.processName,
						                          obj.transactionStatus,
						                          obj.startDate,
						                          obj.endDate
						                          ]).draw();

					}else if($("#processName").val()=="AgentIncentiveCalculatorProcess"){
						//$("#tableTransaction thead").find("th:eq(3)").text("Transaction Status");
						
						tableTransaction.row.add([
						                          obj.id,
						                          "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.id+"' data-target='"+FetchId+"'>Fetch Results</a>",
						                          obj.processName,
						                          obj.transactionStatus,
						                          obj.startDate,
						                          obj.endDate
						                          ]).draw();

					}
					else if($("#processName").val()=="LifelineMigrationProcess"){
						
						$("#tableTransaction thead").find("th:eq(3)").text("Migration Status");
						
						var status = "";
						if(obj.migrationStatus=="P"){
							status = "<span style='color:orange;'>Pending</span>";
						}else if(obj.migrationStatus=="C"){
							status = "<span style='color:green;' >Completed</span>";
						}else{
							status = "<span style='color:red;' >Failed</span>";
						}
						/*var id = "";
						if(obj.migrationDate!=null){
							date = moment(obj.migrationDate).format("DD/MM/YYYY");
						}*/
						tableTransaction.row.add([
						                          obj.id,
						                           "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+ obj.id +"' data-target='#migrationModal'>Fetch Results</a>",
						                          obj.processName,
						                          status,
						                          obj.startDate,
						                          obj.endDate
						                          ]).draw();
						$("#tableTransaction tbody tr").find("td:eq(3)").css("text-align","center");
					}else{
						$("#tableTransaction thead").find("th:eq(3)").text("Transaction Status");
						
						tableTransaction.row.add([
						                          obj.id,
						                          "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.id+"' data-target='"+FetchId+"'>Fetch Results</a>",
						                          obj.processName,
						                          obj.transactionStatus,
						                          obj.startDate,
						                          obj.endDate
						                          ]).draw();

					}

				});
				//toastr.clear();toastr.info("Action completed successfully.");
			},
			error: function(e){
				hideLoader();
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
			}
		});

	}

	$('#viewTranModal').on('show.bs.modal', function(e) {

		var $modal = $(this),
		esseyId = e.relatedTarget.id;
		$.ajax({
			cache: false,
			type: 'POST',
			url: "/rpa/viewTransaction",
			data: { id: esseyId },
			success: function(data) {
				$(".modal-body #transactionId").val( data.id );
				$(".modal-body #externalTransactionRefNo").val( data.externalTransactionRefNo );
				$(".modal-body #transactionStatus").val( data.transactionStatus );
				$(".modal-body #processPhase").val( data.processPhase );
				$(".modal-body #processStatus").val( data.processStatus );
				$(".modal-body #processSuccessReason").val( data.processSuccessReason );
				$(".modal-body #processFailureReason").val( data.processFailureReason );
				$(".modal-body #totalRecords").val( data.totalRecords );
				$(".modal-body #totalSuccessRecords").val( data.totalSuccessRecords );
				$(".modal-body #totalErrorRecords").val( data.totalErrorRecords );
				$(".modal-body #errorFilePath").val( data.errorFileDownload );
				$(".modal-body #successFilePath").val( data.successFileDownload );
				$(".modal-body #uploadFilePath").val( data.uploadFileDownload );
				$(".modal-body #glStartDate").val(moment(data.transactionStartDate ,"x").format("DD/MM/YYYY"));
				$(".modal-body #ReprocessedFlag").val( data.reprocessedFlag );
				$(".modal-body #oldRunNo").val( data.oldRunNo );
				$(".modal-body #newRunNo").val( data.runNo );

				if( data.runNo !=null &&  data.runNo != "" )
				{
					$(".modal-body .hideDiv").hide();
				}	
				else
				{
					$(".modal-body .hideDiv").show();
				}	
				
				
				getTransactionExceptionLog(data.id,"vb64_exceptionLog");
			},
			error: function(e){
				toastr.clear();toastr.error("Record not found.");
			}
		});
	});

	$('#migrationModal').on('show.bs.modal', function(e) {

		var $modal = $(this),
		esseyId = e.relatedTarget.id;
		$.ajax({
			cache: false,
			type: 'POST',
			url: "/rpa/getMigrationStatusByTranId",
			data: { id: esseyId },
			success: function(data) {
				if($.isEmptyObject(data.lifeLineMigrationList)){
					$(".migrationDataDiv").hide();
				}
				else{
					$(".migrationDataDiv").show();
				$.each(data.lifeLineMigrationList, function(ind, obj){
					$("#migrationDate").text(obj.countDate);
					if(obj.appType=="F"){
						$(".modal-body #approvedCount_F").val( obj.approvedCount );
						$(".modal-body #cancelledCount_F").val( obj.cancelledCount );
						$(".modal-body #heathClaimsCount_F").val( obj.healthclaims );
						$(".modal-body #heathInwardCount_F").val( obj.healthInwardCount );
						$(".modal-body #licenseAgentCount_F").val( obj.licenseAgent );
						$(".modal-body #lifeLineCount_F").val( obj.lifelineInward );
						$(".modal-body #mobileInwardCount_F").val( obj.mobileInwardCount );
						$(".modal-body #motorClaimsCount_F").val( obj.motorClaims );
						$(".modal-body #pipeLineCount_F").val( obj.pipelineCount );
						$(".modal-body #renewalpolicyCount_F").val( obj.renewalPolicyCount );
						$(".modal-body #xgenHealthClaimsCount_F").val( obj.xgenHealthclaims );
						$(".modal-body #receiptCount_F").val( obj.receiptCount );
					}else{
						$(".modal-body #approvedCount_I").val( obj.approvedCount );
						$(".modal-body #cancelledCount_I").val( obj.cancelledCount );
						$(".modal-body #heathClaimsCount_I").val( obj.healthclaims );
						$(".modal-body #heathInwardCount_I").val( obj.healthInwardCount );
						$(".modal-body #licenseAgentCount_I").val( obj.licenseAgent );
						$(".modal-body #lifeLineCount_I").val( obj.lifelineInward );
						$(".modal-body #mobileInwardCount_I").val( obj.mobileInwardCount );
						$(".modal-body #motorClaimsCount_I").val( obj.motorClaims );
						$(".modal-body #pipeLineCount_I").val( obj.pipelineCount );
						$(".modal-body #renewalpolicyCount_I").val( obj.renewalPolicyCount );
						$(".modal-body #xgenHealthClaimsCount_I").val( obj.xgenHealthclaims );
						$(".modal-body #receiptCount_I").val( obj.receiptCount );
					}
				});
			}
				var status = "FAILED";
				if(data.transactionInfoList[0].migrationStatus!=null){
					if(data.transactionInfoList[0].migrationStatus=="P"){
						status="Pending";
					}else{
						status="Completed";
					}
				}
					$("#mig_transactionStatus").val(status);
				
				$("#mig_processPhase").val(data.transactionInfoList[0].processPhase);
				$("#mig_processStatus").val(data.transactionInfoList[0].processStatus);
			},
			error: function(e){
				toastr.clear();toastr.error("Record not found.");
			}
		});
	});
	
	$('#uploadTranModal').on('show.bs.modal', function(e) {

		var $modal = $(this),
		esseyId = e.relatedTarget.id;
		$.ajax({
			cache: false,
			type: 'POST',
			url: "/rpa/viewTransaction",
			data: { id: esseyId },
			success: function(data) {
				$(".modal-body #liflineUpload_transactionId").val( data.id );
				//$(".modal-body #liflineUpload_externalTransactionRefNo").val( data.externalTransactionRefNo );
				$(".modal-body #liflineUpload_transactionStatus").val( data.transactionStatus );
				$(".modal-body #liflineUpload_processPhase").val( data.processPhase );
				$(".modal-body #liflineUpload_processStatus").val( data.processStatus );
				$(".modal-body #liflineUpload_processSuccessReason").val( data.processSuccessReason );
				$(".modal-body #liflineUpload_processFailureReason").val( data.processFailureReason );
				$(".modal-body #liflineUpload_totalValidatedRecords").val( data.totalRecords );
				$(".modal-body #liflineUpload_totalValidationSuccessRecords").val( data.totalSuccessRecords );
				$(".modal-body #liflineUpload_totalValidationErrorRecords").val( data.totalErrorRecords );
				$(".modal-body #liflineUpload_totalRecords").val( data.totalUploadRecords );
				$(".modal-body #liflineUpload_totalSuccessRecords").val( data.totalSuccessUploads );
				$(".modal-body #liflineUpload_totalErrorRecords").val( data.totalErrorUploads );
				$(".modal-body #liflineUpload_errorFilePath").val( data.errorFileDownload );
				$(".modal-body #liflineUpload_successFilePath").val( data.successFileDownload );
				$(".modal-body #liflineUpload_uploadFilePath").val( data.uploadFileDownload );
				$(".modal-body #liflineUpload_logFilePath").val( data.logFileDownload );
				$(".modal-body #liflineUpload_glStartDate").val(moment(data.transactionStartDate ,"x").format("DD/MM/YYYY"));
				$(".modal-body #liflineUpload_ReprocessedFlag").val( data.reprocessedFlag );
				$(".modal-body #liflineUpload_uploadId").val(data.externalTransactionRefNo)
			},
			error: function(e){
				toastr.clear();toastr.error("Record not found.");
			}
		});
	});

	$.fn.callPwdAjax = function( method, checkeds ){
		showLoader();
		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			timeout : 100000,
			data: { userId: $("#userId").val(), password: $("#cpassword").val() },

			success: function(data){
				hideLoader();
				toastr.clear();toastr.info(data);
				location.reload();

			},
			error: function(e){
				hideLoader();
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
			}
		});
	} 

	$.fn.callchangePwdAjax = function( method, checkeds ){
		showLoader();
		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			timeout : 100000,
			data: { username: $("#reg_username").val(), email : $("#reg_email").val() },

			success: function(data){
				if(data=="Temporary password sent to ur email"){
					toastr.success(data);
					$('#forgetPasswordModal .input-lg').val('');
				}else{
					
					toastr.warning(data);
				}
				hideLoader();
			},
			error: function(e){
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				hideLoader();
			}
		});
	}

	$(".addToMail").click(function(){
		var txt ='<div class="input-group m-b-1 toemail"><span class="input-group-addon">@</span> <input  name="toEmailIds" type="text" class="form-control" placeholder="TO" /><span class="input-group-addon" onclick="removeToEmail(this);" style="font-size:x-large;">-</span></div>';
		$("#toEmailDiv").append(txt);
	});

	$(".addCcMail").click(function(){
		var txt ='<div class="input-group m-b-1 copyemail"><span class="input-group-addon">@</span> <input  name="ccEmailIds" type="text" class="form-control" placeholder="CC" /><span class="input-group-addon" onclick="removeCopyEmail(this);" style="font-size:x-large;">-</span></div>';
		$("#ccEmailDiv").append(txt);
	});

	$(".addToMail_Modal").click(function(){
		var txt ='<div class="input-group m-b-1 toemail_VW emailConfigDynamicDivs"><span class="input-group-addon">@</span> <input  name="toEmailIdsModal" type="text" class="form-control" placeholder="TO"  /><span class="input-group-addon" onclick="removeToEmail(this);" style="font-size:x-large;">-</span></div>';
		$("#toEmailIdsModalDiv").append(txt);
	});

	$(".addCcMail_Modal").click(function(){
		var txt ='<div class="input-group m-b-1 copyemail_VW emailConfigDynamicDivs"><span class="input-group-addon">@</span> <input  name="ccEmailIdsModal" type="text" class="form-control" placeholder="CC"  /><span class="input-group-addon" onclick="removeCopyEmail(this);" style="font-size:x-large;">-</span></div>';
		$("#ccEmailIdsModalDiv").append(txt);
	});

	$("#buttonEmailConfigInsert").click(function(){
		$(this).buttonEmailConfigInsertAjax("insertEmailConfig", "");
	});

	$("#buttonEmailConfigUpdate").click(function(){
		$(this).updateEmailConfigAjax("updateEmailConfig", "");
	});

	$.fn.buttonEmailConfigInsertAjax = function( method, checkeds ){
		showLoader();
		var commaSeparatedValues ="";
		if($("#processName").val()!=null && $("#processName").val()!=undefined){
			commaSeparatedValues =$("#processName").val().join(",");
		}else{
			hideLoader();
			toastr.warning("Please Select atleast one process");
			return false;
		}

		var toEmailIds = "";
		$("input[name='toEmailIds']").each(function(){
			debugger;
			if($(this).val()!=""){
				toEmailIds +=$(this).val()+",";
			}
		});
		if(toEmailIds==""){
			hideLoader();
			toastr.warning("Please enter atleast one TO: Recipient");
			return false;
		}

		var ccEmailIds = "";
		$("input[name='ccEmailIds']").each(function(){
			debugger;
			if($(this).val()!=""){
				ccEmailIds +=$(this).val()+",";
			}
		});

		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			timeout : 100000,
			data: { toEmailIds: toEmailIds, ccEmailIds: ccEmailIds, processName: commaSeparatedValues},

			success: function(data){
				tableEmailConfig.clear().draw();
				tableEmailConfig.ajax.reload();
				toastr.clear();toastr.info(data);
				hideLoader();
				if(data=="Email configuration added successfully"){
				$(".toemail,.copyemail").remove();
				$("#toEmailIds,#ccEmailIds").val('');
				$("#processName option:selected").prop("selected", false);
				}
			},
			error: function(e){
				hideLoader();
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
			}
		});
	}

	$.fn.updateEmailConfigAjax = function( method, checkeds ){
		showLoader();
		var commaSeparatedValues ="";
		if($("#processNameModal").val()!=null && $("#processNameModal").val()!=undefined){
			commaSeparatedValues =$("#processNameModal").val().join(",");
		}else{
			hideLoader();
			toastr.warning("Please Select atleast one process");
			return false;
		}

		var toEmailIds = "";
		$("input[name='toEmailIdsModal']").each(function(){
			debugger;
			if($(this).val()!=""){
				toEmailIds +=$(this).val()+",";
			}
		});
		if(toEmailIds==""){
			hideLoader();
			toastr.warning("Please enter atleast one TO: Recipient");
			return false;
		}

		var ccEmailIds = "";
		$("input[name='ccEmailIdsModal']").each(function(){
			debugger;
			if($(this).val()!=""){
				ccEmailIds +=$(this).val()+",";
			}
		});

		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			timeout : 100000,
			data: { id : $("#emailConfigId").val(), toEmailIds: toEmailIds, ccEmailIds: ccEmailIds, processName: commaSeparatedValues},

			success: function(data){
				tableEmailConfig.clear().draw();
				tableEmailConfig.ajax.reload();
				toastr.clear();toastr.info(data);
				hideLoader();
			},
			error: function(e){
				hideLoader();
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
			}
		});
	}
	
	$("#buttonApplicationConfigRefresh").click(function(){

		tableApplicationConfig.clear().draw();
		tableApplicationConfig.ajax.reload();

	});
	
	$("#buttonApplicationConfigInsert").click(function(){
		$(this).callAppProcessAjax("insertApplicationConfig", "");
	});
	
	$("#buttonApplicationConfigUpdate").click(function(){
		$(this).callAppProcessUpdateAjax("updateApplicationConfig", "");
	});
	
	$.fn.callAppProcessAjax = function( method, checkeds ){
		showLoader();
		var commaSeparatedValues ="";
		if($("#appDetail_processName").val()!=null && $("#appDetail_processName").val()!=undefined && $("#appDetail_processName").val()!=""){
			commaSeparatedValues = $("#appDetail_processName").val();
		}else{
			hideLoader();
			toastr.warning("Please Select process");
			return false;
		}
		
		var appName ="";
		if($("#appDetail_appName").val()!=null && $("#appDetail_appName").val()!=undefined && $("#appDetail_appName").val()!=""){
			appName = $("#appDetail_appName").val();
		}else{
			hideLoader();
			toastr.warning("Please Select application");
			return false;
		}
		
		var url = "";
		if($("#appDetail_url").val()!=null && $("#appDetail_url").val()!=undefined && $("#appDetail_url").val()!=""){
			url = $("#appDetail_url").val();
		}else{
			hideLoader();
			toastr.warning("Please Enter application url");
			return false;
		}
		
		var username = "";
		if($("#appDetail_username").val()!=null && $("#appDetail_username").val()!=undefined && $("#appDetail_username").val()!=""){
			username = $("#appDetail_username").val();
		}else{
			hideLoader();
			toastr.warning("Please Enter username");
			return false;
		}
		
		var password = "";
		if($("#appDetail_password").val()!=null && $("#appDetail_password").val()!=undefined && $("#appDetail_password").val()!=""){
			password = $("#appDetail_password").val();
		}else{
			hideLoader();
			toastr.warning("Please Enter password");
			return false;
		}

		$.ajax({
			type: "POST",
			url: "/rpa/" + method,
			timeout : 100000,
			data: { appName: appName, url: url, username :username,password:password,processName: commaSeparatedValues},

				success: function(data){
					tableApplicationConfig.clear().draw();
					tableApplicationConfig.ajax.reload();
					toastr.clear();toastr.info(data);
					$("#appDetail_processName,#appDetail_appName,#appDetail_url,#appDetail_username,#appDetail_password").val('');
					hideLoader();
				},
				error: function(e){
					hideLoader();
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
		});
	}
	
	$.fn.callAppProcessUpdateAjax = function( method, checkeds ){
		showLoader();
		var commaSeparatedValues ="";
		if($("#appDetail_processNameVw").val()!=null && $("#appDetail_processNameVw").val()!=undefined){
			commaSeparatedValues = $("#appDetail_processNameVw").val();
		}else{
			hideLoader();
			toastr.warning("Please Select process");
			return false;
		}
		
		var appName ="";
		if($("#appDetail_appNameVw").val()!=null && $("#appDetail_appNameVw").val()!=undefined){
			appName = $("#appDetail_appNameVw").val();
		}else{
			hideLoader();
			toastr.warning("Please Select application");
			return false;
		}
		var isPasswordExpired = "";
		if($("#appDetail_isPasswordExpiredVw").val()!=null && $("#appDetail_isPasswordExpiredVw").val()!=undefined && $("#appDetail_isPasswordExpiredVw").val()!=""){
			isPasswordExpired = $("#appDetail_isPasswordExpiredVw").val();
		}else{
			hideLoader();
			//toastr.warning("Please Enter application url");
			return false;
		}
		
		var url = "";
		if($("#appDetail_urlVw").val()!=null && $("#appDetail_urlVw").val()!=undefined && $("#appDetail_urlVw").val()!=""){
			url = $("#appDetail_urlVw").val();
		}else{
			hideLoader();
			toastr.warning("Please Enter application url");
			return false;
		}
		
		var username = "";
		if($("#appDetail_usernameVw").val()!=null && $("#appDetail_usernameVw").val()!=undefined && $("#appDetail_usernameVw").val()!=""){
			username = $("#appDetail_usernameVw").val();
		}else{
			hideLoader();
			toastr.warning("Please Enter username");
			return false;
		}
		
		var password = "";
		if($("#appDetail_passwordVw").val()!=null && $("#appDetail_passwordVw").val()!=undefined && $("#appDetail_passwordVw").val()!=""){
			password = $("#appDetail_passwordVw").val();
		}else{
			hideLoader();
			toastr.warning("Please Enter password");
			return false;
		}
	

	$.ajax({
		type: "POST",
		url: "/rpa/" + method,
		timeout : 100000,
		data: { id: $("#appProcess_id").val() , appName: appName, url: url, username :username,password:password,processName: commaSeparatedValues, isPasswordExpired: isPasswordExpired},
			success: function(data){
				tableApplicationConfig.clear().draw();
				tableApplicationConfig.ajax.reload();
				toastr.clear();toastr.info(data);
				hideLoader();
			},
			error: function(e){
				hideLoader();
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
			}
	});
}

	var tableApplicationConfig = $('#tableApplicationConfig').DataTable({
		"autoWidth": false,
		/* "fnDrawCallback": function( oSettings ) {
			 $("#tableProcessMail tbody tr").find("td:eq(2),td:eq(3)").css("max-width","200px");
		    },*/
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getApplicationConfigurationDetails",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){

		            			   tableApplicationConfig.row.add([
		            			                      obj.id,
		            			                      "<input type='checkbox' value='"+obj.id+"' id=''>",
		            			                    /*  obj.processId,*/
		            			                      obj.processName,
		            			                      obj.appName,
		            			                      obj.status,
		            			                      "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.id+"' data-target='#applicationConfigModal'>Edit</a>",
		            			                      ]).draw();
		            		   });
		            	   }
		               },
		              
	});
	
	$('#applicationConfigModal').on('show.bs.modal', function(e) {

		var $modal = $(this),
		esseyId = e.relatedTarget.id;
		$.ajax({
			cache: false,
			type: 'POST',
			url: "/rpa/viewApplicationConfigModal",
			data: { id: esseyId },
			success: function(data) {
					$("#appProcess_id").val(data.id);
					$("#appDetail_processNameVw").val(data.processName);
					$("#appDetail_appNameVw").val(data.appId);
					$("#appDetail_urlVw").val(data.url);
					$("#appDetail_usernameVw").val(data.username);
					$("#appDetail_passwordVw").val(data.password);
					$("#appDetail_isPasswordExpiredVw").val(data.isPasswordExpired);
			},
			error: function(e){
				toastr.clear();toastr.error("Record not found.");
			}
		});
	});
	
	$("#buttonApplicationConfigDelete").click(function(){

		var valuesChecked = $("#tableApplicationConfig input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");

		if(valuesChecked==""){
			toastr.clear();toastr.warning("Please select atleast one Process");
			return false;
		}else{
			$(this).callAjax("deleteApplicationConfig", valuesChecked);
		}

	});

	$("#buttonApplicationConfigActivate").click(function(){

		var valuesChecked = $("#tableApplicationConfig input[type='checkbox']:checkbox:checked").map(
				function () {
					return this.value;
				}).get().join(",");

		if(valuesChecked==""){
			toastr.clear();toastr.warning("Please select atleast one Process");
			return false;
		}else{
			$(this).callAjax("activateApplicationConfig", valuesChecked);
		}

	});

	function validateIntermediaryNo(sIntermediaryNo) {
		var filter = /^[0-9a-zA-Z',-]+$/;
		if (filter.test(sIntermediaryNo)) {
			return true;
		}
		else {
			return false;
		}
	}

	function validateUserName(sUserName) {
		var filter = /^[a-zA-Z]+$/;
		if (filter.test(sUserName)) {
			return true;
		}
		else {
			return false;
		}
	}

	function validateEmail(sEmail) {
		var filter = /^([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$/;
		if (filter.test(sEmail)) {
			return true;
		}
		else {
			return false;
		}
	}

	function validatePhoneNumber(sPhoneNumber) {
		var filter = /\(?([0-9]{3})\)?([ .-]?)([0-9]{3})\2([0-9]{4})/;
		if (filter.test(sPhoneNumber)) {
			return true;
		}
		else {
			return false;
		}
	}
});

function removeToEmail(thisObj){
	$(thisObj).parent().remove();
}

function removeCopyEmail(thisObj){
	$(thisObj).parent().remove();
}

function StatusFilter(thisObj){
	if($(thisObj).val()=="LifelineMigrationProcess"){
		$(".statusFilter").show();
	}else{
		$(".statusFilter").hide();
	}
}

$("#glSuccessFileDownload").click(function(){

	if($("#glSuccessFilePath").val().length > 0) {
		var successFilePath = $("#glSuccessFilePath").val();

		var contentType="application/pdf";
		var linkURL = "?downloadFilePath="+successFilePath+"&contentType="+contentType;
		$("#hiddenAnchor").attr("href","/rpa/commonDownloadMethod"+linkURL);
		$("#hiddenAnchor")[0].click();
	} else {
		if($("#transactionStatus").val() !="Success") {
			toastr.clear();toastr.warning('No Success File');
			return false;
		}

		/*	var extRef = $("#externalTransactionRefNo").val();
		//var startDt =  moment( $("#glStartDate").val(),"DD/MM/YYYY HH:mm:ss:sss").format("DD/MM/YYYY");
		var startDt =  $("#glStartDate").val();
		var trnStatus =  $("#transactionStatus").val();
		var linkURL = "?externalTranRefNo="+extRef+"&startDate="+startDt+"&status="+trnStatus;

		$("#hiddenAnchor").attr("href","/rpa/getFileDownload"+linkURL);
		$("#hiddenAnchor")[0].click();*/
	}
});


$("#glErrorFileDownload").click(function(){

	if($("#glErrorFilePath").val().length > 0) {
		var errorFilePath = $("#glErrorFilePath").val();
		var contentType="application/pdf";
		var linkURL = "?downloadFilePath="+errorFilePath+"&contentType="+contentType;;
		$("#hiddenAnchor").attr("href","/rpa/commonDownloadMethod"+linkURL);
		$("#hiddenAnchor")[0].click();
	} else {
		if($("#transactionStatus").val() !="Error") {
			toastr.clear();toastr.warning('No Error File');
			return false;
		}		
		/*	var extRef = $("#externalTransactionRefNo").val();
		var startDt =  moment( $("#glStartDate").val(),"DD/MM/YYYY HH:mm:ss:sss").format("DD/MM/YYYY");
		var startDt =  $("#glStartDate").val();

		var trnStatus =  $("#transactionStatus").val();
		var linkURL = "?externalTranRefNo="+extRef+"&startDate="+startDt+"&status="+trnStatus;

		$("#hiddenAnchor").attr("href","/rpa/getFileDownload"+linkURL);
		$("#hiddenAnchor")[0].click();*/
	}
});

$('#glBatchModal').on('show.bs.modal', function(e) {

	var $modal = $(this),
	esseyId = e.relatedTarget.id;
	$.ajax({
		cache: false,
		type: 'POST',
		url: "/rpa/viewTransaction",
		data: { id: esseyId },
		success: function(data) {
			
			if(data.externalTransactionRefNo==null){
				data.externalTransactionRefNo="Status";
			}
			if(data.externalTransactionRefNo!=null){
			if(data.externalTransactionRefNo.indexOf("Status")!=-1){
				$(".processGLStatus").show();
			}else{
				$(".processGLStatus").hide();
			}
			}
			
			$(".modal-body #transactionId").val( data.id );
			$(".modal-body #externalTransactionRefNo").val( data.externalTransactionRefNo );
			$(".modal-body #transactionStatus").val( data.transactionStatus );
			$(".modal-body #glErrorFilePath").val( data.errorFileDownload );
			$(".modal-body #glSuccessFilePath").val(data.successFileDownload );
			$(".modal-body #glStartDate").val(moment(data.transactionStartDate ,"x").format("DD/MM/YYYY"));
			$(".modal-body #ReprocessedFlag").val( data.reprocessedFlag );
			$(".modal-body #oldRunNo").val( data.oldRunNo );
			$(".modal-body #newRunNo").val( data.runNo );
			$(".modal-body #firstGen_processPhase").val( data.processPhase );
			$(".modal-body #firstGen_processStatus").val( data.processStatus );
			$(".modal-body #firstGen_processFaliureReason").val( data.processFailureReason );
			
			getTransactionExceptionLog(data.id,"firstGen_exceptionLog");
		},
		error: function(e){
			toastr.clear();toastr.error("Record not found.");
		}
	});
});

$("#liflineUpload_successFileDownload").click(function(){

	if($("#liflineUpload_successFilePath").val().length > 0) {
		var successFilePath = $("#liflineUpload_successFilePath").val();
		var linkURL = "?downloadFilePath="+successFilePath;
		$("#liflineUpload_hiddenAnchor").attr("href","/rpa/downloadXLS"+linkURL);
		$("#liflineUpload_hiddenAnchor")[0].click();
	} else {
		if($("#liflineUpload_transactionStatus").val() !="Success") {
			toastr.clear();toastr.warning('No Success File');
			return false;
		}
	}
});

$("#liflineUpload_errorFileDownload").click(function(){

	if($("#liflineUpload_errorFilePath").val().length > 0) {
		var errorFilePath = $("#liflineUpload_errorFilePath").val();
		var linkURL = "?downloadFilePath="+errorFilePath;
		$("#liflineUpload_hiddenAnchor").attr("href","/rpa/downloadXLS"+linkURL);
		$("#liflineUpload_hiddenAnchor")[0].click();
	} else {
		if($("#liflineUpload_transactionStatus").val() !="Error") {
			toastr.clear();toastr.warning('No Error File');
			return false;
		}		
	}
});


$("#liflineUpload_logFileDownload").click(function(){

	if($("#liflineUpload_logFilePath").val().length > 0) {
		var successFilePath = $("#liflineUpload_logFilePath").val();
		var linkURL = "?downloadFilePath="+successFilePath;
		$("#liflineUpload_hiddenAnchor").attr("href","/rpa/downloadXLS"+linkURL);
		$("#liflineUpload_hiddenAnchor")[0].click();
	} else {
			toastr.clear();toastr.warning('No Log File');
			return false;
		}
});

$('#marutiPolicyExtractionTranModal').on('show.bs.modal', function(e) {

	var $modal = $(this),
	esseyId = e.relatedTarget.id;
	$.ajax({
		cache: false,
		type: 'POST',
		url: "/rpa/viewTransaction",
		data: { id: esseyId },
		success: function(data) {
			
			$(".modal-body #marutiPolicyExtraction_transactionId").val( data.id );
			$(".modal-body #marutiPolicyExtraction_externalTransactionRefNo").val( data.externalTransactionRefNo );
			$(".modal-body #marutiPolicyExtraction_transactionStatus").val( data.transactionStatus );
			$(".modal-body #marutiPolicyExtraction_processPhase").val( data.processPhase );
			$(".modal-body #marutiPolicyExtraction_processStatus").val( data.processStatus );
			$(".modal-body #marutiPolicyExtraction_processSuccessReason").val( data.processSuccessReason );
			$(".modal-body #marutiPolicyExtraction_processFailureReason").val( data.processFailureReason );
			$(".modal-body #marutiPolicyExtraction_PoliciedTaken").val( data.totalRecords );
			$(".modal-body #marutiPolicyExtraction_PolicyPdfExtracted").val( data.totalSuccessRecords );
			$(".modal-body #marutiPolicyExtraction_ProposalPdfExtracted").val( data.totalSuccessUploads );
			//getMarutiDailyStatus($("#marutiPolicyExtraction_statusDate").val());
			getTransactionExceptionLog(data.id,"marutiPolicyExtraction_exceptionLog");
		},
		error: function(e){
			toastr.clear();toastr.error("Record not found.");
		}
	});
});

$('#getMarutiDailyStatus').click(function(){
	if($("#marutiPolicyExtraction_monthYear").val()==""){
		toastr.warning("MM/YYYY cannot be empty");
		return false;
	}else if($("#statusSelection").val()==""){
		toastr.warning("Please select status type");
		return false;
	}else if($("#marutiPolicyDateList").val()=="0"){
		toastr.warning("Please select Date");
		return false;
	}else{
		getMarutiDailyStatus($("#marutiPolicyDateList").val());
	}
	
});

function getMarutiDailyStatus(date){
	$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicies").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfExtracted").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfExtracted").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUploaded").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUploaded").val( '-' );
	/*$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUnExtracted").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUnExtracted").val( '-' );*/
	$.ajax({
		cache: false,
		type: 'POST',
		url: "/rpa/getMarutiDailyStatus",
		data: { policyDate: date,carType : $("#carType").val() },
		success: function(data) {
			$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicies").val( data.totalPolciesCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfExtracted").val( data.policyPdfExtractedCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfExtracted").val( data.proposalPdfExtractedCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUploaded").val( data.policyPdfUploadedCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUploaded").val( data.proposalPdfUploadedCount );
			$/*(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUnExtracted").val( data.policyPdfErrorCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUnExtracted").val( data.proposalPdfErrorCount );*/
			$("#extractionStatusDate").text(date);
		},
		error: function(e){
			toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
		}
	});
}


function getMarutiMonthlyStatus(month,year){
	$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicies").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfExtracted").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfExtracted").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUploaded").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUploaded").val( '-' );
	/*$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUnExtracted").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUnExtracted").val( '-' );*/
	$.ajax({
		cache: false,
		type: 'POST',
		url: "/rpa/getMarutiMonthlyStatus",
		data: {policyMonth : month, policyYear : year,carType : $("#carType").val()},
		success: function(data) {
			$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicies").val( data.totalPolciesCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfExtracted").val( data.policyPdfExtractedCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfExtracted").val( data.proposalPdfExtractedCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUploaded").val( data.policyPdfUploadedCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUploaded").val( data.proposalPdfUploadedCount );
			$/*(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUnExtracted").val( data.policyPdfErrorCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUnExtracted").val( data.proposalPdfErrorCount );*/
			//$("#extractionStatusDate").text(date);
		},
		error: function(e){
			toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
		}
	});
}

function getMarutiYearlyStatus(year){
	$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicies").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfExtracted").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfExtracted").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUploaded").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUploaded").val( '-' );
	/*$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUnExtracted").val( '-' );
	$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUnExtracted").val( '-' );*/
	$.ajax({
		cache: false,
		type: 'POST',
		url: "/rpa/getMarutiYearlyStatus",
		data: { policyYear : year,carType : $("#carType").val()},
		success: function(data) {
			$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicies").val( data.totalPolciesCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfExtracted").val( data.policyPdfExtractedCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfExtracted").val( data.proposalPdfExtractedCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUploaded").val( data.policyPdfUploadedCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUploaded").val( data.proposalPdfUploadedCount );
			$/*(".omniStatusDiv #marutiPolicyExtraction_TotalPolicyPdfUnExtracted").val( data.policyPdfErrorCount );
			$(".omniStatusDiv #marutiPolicyExtraction_TotalProposalPdfUnExtracted").val( data.proposalPdfErrorCount );*/
		//	$("#extractionStatusDate").text(date);
		},
		error: function(e){
			toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
		}
	});
}

$("#marutiPolicyExtraction_statusDate").datetimepicker({
	format: 'MM/DD/YYYY',
	defaultDate:getYesterdayDateObj(),
	showClose:true
}).on('dp.show dp.update', function () {
	$(".picker-switch").css('cursor','none');
	$(".picker-switch").removeAttr('title')
	    //.css('cursor', 'default')  <-- this is not needed if the CSS above is used
	    //.css('background', 'inherit')  <-- this is not needed if the CSS above is used
	    .on('click', function (e) {
	        e.stopPropagation();
	    });
	});

function getYesterdayDateObj(){
	var todayTimeStamp = +new Date; // Unix timestamp in milliseconds
	var oneDayTimeStamp = 1000 * 60 * 60 * 24; // Milliseconds in a day
	var diff = todayTimeStamp - oneDayTimeStamp;
	var yesterdayDate = new Date(diff);
	return yesterdayDate;
}

$("#statusSelection").change(function(){
	$("#marutiPolicyDateList").find('option').not(':first').remove();
	$(".modal-body #marutiPolicyExtraction_TotalPolicies").val( '-' );
	$(".modal-body #marutiPolicyExtraction_TotalPolicyPdfExtracted").val( '-' );
	$(".modal-body #marutiPolicyExtraction_TotalProposalPdfExtracted").val( '-' );
	$(".modal-body #marutiPolicyExtraction_TotalPolicyPdfUploaded").val( '-' );
	$(".modal-body #marutiPolicyExtraction_TotalProposalPdfUploaded").val( '-' );
/*	$(".modal-body #marutiPolicyExtraction_TotalPolicyPdfUnExtracted").val( '-' );
	$(".modal-body #marutiPolicyExtraction_TotalProposalPdfUnExtracted").val( '-' );*/
	if($("#marutiPolicyExtraction_monthYear").val()==""){
		toastr.warning("MM/YYYY cannot be empty");
		return false;
	}
	if($("#statusSelection").val()!=""){
	getMarutiPolicyDateList($("#statusSelection").val(),$("#marutiPolicyExtraction_monthYear").val());
	}
});

$("#marutiPolicyExtraction_monthYear").blur(function (){
	$("#statusSelection").val("");
	$("#marutiPolicyDateList").find('option').not(':first').remove();
	$(".modal-body #marutiPolicyExtraction_TotalPolicies").val( '-' );
	$(".modal-body #marutiPolicyExtraction_TotalPolicyPdfExtracted").val( '-' );
	$(".modal-body #marutiPolicyExtraction_TotalProposalPdfExtracted").val( '-' );
	$(".modal-body #marutiPolicyExtraction_TotalPolicyPdfUploaded").val( '-' );
	$(".modal-body #marutiPolicyExtraction_TotalProposalPdfUploaded").val( '-' );
	/*$(".modal-body #marutiPolicyExtraction_TotalPolicyPdfUnExtracted").val( '-' );
	$(".modal-body #marutiPolicyExtraction_TotalProposalPdfUnExtracted").val( '-' );*/
});

function getMarutiPolicyDateList(statusFlag,monthYear){
	$("#marutiPolicyDateList").find('option').not(':first').remove();
	$.ajax({
		cache: false,
		type: 'POST',
		url: "/rpa/getMarutiPolicyDateList",
		data: { statusFlag: statusFlag,monthYear : monthYear },
		success: function(data) {
			 $.each(data, function(ind, obj){
				 $("#marutiPolicyDateList").append( $("<option>")
						    .val(obj)
						    .text(obj)
						);
			 });
		},
		error: function(e){
			toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
		}
	});
}

var carPolicytableTransaction = $('#carPolicytableTransaction').DataTable( {
	 "pageLength":100,
     "processing": true,
     "deferRender": true
} );


$("#carExtractionButtonFilter").click(function(){
	
	if($("#omniStatusType").val()=="1"){

			if($("#startDateCarPolicy").val()=="") {
				toastr.clear();toastr.warning('Please enter a valid Start Date.');
				return false;
			} else if($("#endDateCarPolicy").val()=="") {
				toastr.clear();toastr.warning('Please enter a valid End Date.');
				return false;
			} else if($("#carType").val()==""){
				toastr.clear();toastr.warning("Please select a car type.")
				return false;
			}
		
			var omnidocPolicyNumberDmsCheckURL ="";
			var omnidocProposalCodeDmsCheckURL =""
			var policyLink ="";
			var proposalLink="";
			var uploadReference="";
			var serviceURL = "";
			
			if($("#carType").val()=="MAR"){
				serviceURL = "/rpa/getCarPolicyExtractionDetails";
			}else if($("#carType").val()=="HON"){
				serviceURL = "/rpa/getHondaPolicyExtractionDetails";
			}else if($("#carType").val()=="FRD"){
				serviceURL = "/rpa/getFordPolicyExtractionDetails";
			}else if($("#carType").val()=="TAT"){
				serviceURL = "/rpa/getTATAPolicyExtractionDetails";
			}else if($("#carType").val()=="ABL"){
				serviceURL = "/rpa/getABIBLPolicyExtractionDetails";
			}else if($("#carType").val()=="MBL"){
				serviceURL = "/rpa/getMIBLPolicyExtractionDetails";
			}else if($("#carType").val()=="VOL"){
				serviceURL = "/rpa/getVOLVOPolicyExtractionDetails";
			}else if($("#carType").val()=="TAF"){
				serviceURL = "/rpa/getTAFEPolicyExtractionDetails";
			}else if($("#carType").val()=="PIA"){
				serviceURL = "/rpa/getPIAGGIOPolicyExtractionDetails";
			}else if($("#carType").val()=="FGN"){
				serviceURL = "/rpa/getFirstgenPolicyExtractionDetails";
			}
			
				
			showLoader();
			$.ajax({
				type: "POST",
				url: serviceURL,
				dataType: "json",
				timeout : 100000,
				data: { startDate: $("#startDateCarPolicy").val(), endDate: $("#endDateCarPolicy").val(), carType: $("#carType").val()},
		
				"success" :  function(data){
					hideLoader();	
					carPolicytableTransaction.clear().draw();
					$.each(data, function(ind, obj){
						policyLink="Pending";proposalLink	="Pending";
						if(obj.isPolicyUploaded=="Y"){
							if($("#carType").val()=="FGN"){
								omnidocPolicyNumberDmsCheckURL = "http://10.46.192.89/omnidocs/integration/foldView/viewFoldList.jsp?Application=OEM_VIEW&DataClassName=DTC&DC.PolicyNumber="+obj.policyNo+"&P=PsessionIndexSet=false";
							}else{
								omnidocPolicyNumberDmsCheckURL = omnicheckURL+"omnidocs/integration/foldView/viewFoldList.jsp?Application=OEM_VIEW&DataClassName=OEM&DC.PolicyNumber="+obj.policyNo+"&P=PsessionIndexSet=false";
							}
							policyLink = "<a href='"+omnidocPolicyNumberDmsCheckURL+"' class='btn btn-info' target='_blank' >View</a>";
						}else if(obj.isPolicyUploaded=="E" || obj.isPolicyUploaded=="D"){
							policyLink="Error";
							policyLink = "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.policyNo+"' name='PO' data-target='#uploadReferenceModal'>Error</a>";
						}else if(obj.isPolicyDownloaded=="Y"){
							policyLink="Downloaded";
						}
						
						if(obj.isProposalUploaded=="Y"){
							omnidocProposalCodeDmsCheckURL = omnicheckURL+"omnidocs/integration/foldView/viewFoldList.jsp?Application=OEM_VIEW&DataClassName=OEM&DC.ProposalCode="+obj.proposalNumber+"&P=PsessionIndexSet=false"
							proposalLink = "<a href='"+omnidocProposalCodeDmsCheckURL+"' class='btn btn-info'  target='_blank' >View</a>";
						}else if(obj.isProposalUploaded=="E" || obj.isProposalUploaded=="D"){
							//proposalLink="Error";
							proposalLink = "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.policyNo+"' name='PR' data-target='#uploadReferenceModal'>Error</a>";
						}else if(obj.isProposalDownloaded=="Y"){
							proposalLink="Downloaded";
						}
						
						if($("#carType").val()=="FGN"){
							carPolicytableTransaction.row.add([
								 			                      obj.policyDate,
								 			                      obj.policyNo,
								 			                      policyLink,
								 			                     uploadReference
								 			                      ]).draw();
						}else{
							carPolicytableTransaction.row.add([
								 			                      obj.policyDate,
								 			                      obj.policyNo,
								 			                      policyLink,
								 			                     obj.proposalNumber,
								 			                     proposalLink,
								 			                      obj.inwardCode,
								 			                     uploadReference
								 			                      ]).draw();
						}
						
						
					});
				}
			});
	}else if($("#omniStatusType").val()=="2"){
		getMarutiYearlyStatus($("#omniStatusYear").val());
	}else if($("#omniStatusType").val()=="3"){
		getMarutiMonthlyStatus($("#omniStatusMonth").val(),$("#omniStatusYear").val());
	}else if($("#omniStatusType").val()=="4"){
		getMarutiDailyStatus($("#omniStatusDate").val());
	}
	});

$("#startDateCarPolicy").datetimepicker({
	format: 'MM/DD/YYYY',
	defaultDate:new Date(),
	showClose:true
	//language:"en"

}).on('dp.show dp.update', function () {
	$(".picker-switch").css('cursor','none');
	$(".picker-switch").removeAttr('title')
	    //.css('cursor', 'default')  <-- this is not needed if the CSS above is used
	    //.css('background', 'inherit')  <-- this is not needed if the CSS above is used
	    .on('click', function (e) {
	        e.stopPropagation();
	    });
	});

$("#omniStatusDate").datetimepicker({
	format: 'MM/DD/YYYY',
	defaultDate:new Date(),
	showClose:true
	//language:"en"

}).on('dp.show dp.update', function () {
	$(".picker-switch").css('cursor','none');
	$(".picker-switch").removeAttr('title')
	    //.css('cursor', 'default')  <-- this is not needed if the CSS above is used
	    //.css('background', 'inherit')  <-- this is not needed if the CSS above is used
	    .on('click', function (e) {
	        e.stopPropagation();
	    });
	});

$("#endDateCarPolicy").datetimepicker({
	format: 'MM/DD/YYYY',
	defaultDate:new Date(),
	showClose:true
}).on('dp.show dp.update', function () {
	$(".picker-switch").css('cursor','none');
	$(".picker-switch").removeAttr('title')
	    //.css('cursor', 'default')  <-- this is not needed if the CSS above is used
	    //.css('background', 'inherit')  <-- this is not needed if the CSS above is used
	    .on('click', function (e) {
	        e.stopPropagation();
	    });
	});

	function getTransactionExceptionLog(id,exceptionTextAreaId){
	$.ajax({
		cache: false,
		type: 'POST',
		url: "/rpa/getTransactionExceptionLog",
		data: { id: id },
		success: function(data) {
			$("#"+exceptionTextAreaId).val(data);
		}
	});
	}

	$('#gridUploadModal').on('show.bs.modal', function(e) {
		
		if($("#processName").val()=="ModelCodeCreationProcess")
			$("#gridUploadModal_CountFileDownload").hide();
		else
			$("#gridUploadModal_CountFileDownload").show();
		
		$('.gridStatus').hide();
		var $modal = $(this),
		esseyId = e.relatedTarget.id;
		$.ajax({
			cache: false,
			type: 'POST',
			url: "/rpa/viewTransaction",
			data: { id: esseyId },
			success: function(data) {
				debugger;
				
				$(".modal-body #gridUploadModal_transactionId").val( data.id );
				$(".modal-body #gridUploadModal_externalTransactionRefNo").val( data.externalTransactionRefNo );
				$(".modal-body #gridUploadModal_transactionStatus").val( data.transactionStatus );
				$(".modal-body #gridUploadModal_startDate").val( data.startDate );
				
				$(".modal-body #gridUploadModal_processPhase").val( data.processPhase );
				$(".modal-body #gridUploadModal_processStatus").val( data.processStatus );
				$(".modal-body #gridUploadModal_processSuccessReason").val( data.processSuccessReason );
				$(".modal-body #gridUploadModal_processFailureReason").val( data.processFailureReason );
				$(".modal-body #gridUploadModal_totalValidationFiles").val( data.totalRecords );
				$(".modal-body #gridUploadModal_totalValidationSuccess").val( data.totalSuccessRecords );
				$(".modal-body #gridUploadModal_totalValidationError").val( data.totalErrorRecords );
				$(".modal-body #gridUploadModal_totalUploadFiles").val( data.totalUploadRecords );
				$(".modal-body #gridUploadModal_totalUploadSuccess").val( data.totalSuccessUploads );
				$(".modal-body #gridUploadModal_totalUploadError").val( data.totalErrorUploads );
				$("#filesSelection").find('option').not(':first').remove();	
				getGridFileList(data.id,"");
				
				
				getTransactionExceptionLog(data.id,"gridUploadModal_exceptionLog");
			},
			error: function(e){
				toastr.clear();toastr.error("Record not found.");
			}
		});
	});

	function getGridFileList(transactionId,gridId)
	{
		debugger;
		$(".gridWithModelRows").remove();
		var serviceURL="";
		
		if($("#processName").val()=="GridMasterUploadProcess"){
			serviceURL="/rpa/getGridFilesList";
			getInsertedAndupdatedRowCounts(gridId,"G");
		}else if( $("#processName").val()=="GridWithModelSheetMasterUploadProcess" ){
			getInsertedAndupdatedRowCounts(gridId,"GWM");
			serviceURL="/rpa/getGridModelFilesList";
		}else{
			getInsertedAndupdatedRowCounts(gridId,"G");
			serviceURL="/rpa/getModelGridFilesList";
		}
		
		$.ajax({
			cache: false,
			type: 'GET',
			url: serviceURL,
			data: { transactionId: transactionId,gridId:gridId },
			dataType: "json",
			success: function(data) {
				if(gridId=="")
				{
					$.each(data, function(ind, obj){
					 $("#filesSelection").append( $("<option>")
							    .val(obj.id)
							    .text(obj.fileName)
							);
				 });
				}
				else
					{
					$.each(data, function(ind, obj){
						debugger;
						$(".modal-body #gridUploadModal_gridFileStatus").val( obj.fileStatus );
						$(".modal-body #gridUploadModal_gridTotalSheetCount").val( obj.totalSheetCount );
						$(".modal-body #gridUploadModal_gridFileType").val( obj.fileType );
						if($("#gridUploadModal_gridFileType").val()=="GridAndModel" || $("#gridUploadModal_gridFileType").val()=="GridWithModelAgentProcess" || $("#gridUploadModal_gridFileType").val()=="GridWithModelRegionProcess"){
							$(".modal-body #gridUploadModal_gridValidateNo").val(parseInt(obj.validateNo)+1 );
						}else{
							$(".modal-body #gridUploadModal_gridValidateNo").val(parseInt(obj.validateNo)+2 );
						}
						if(obj.isProcessed =="Y")
						{
							$(".modal-body #gridUploadModal_SuccessFilePath").val( obj.filePath );
							/*if($("#gridUploadModal_gridFileType").val()=="ModelAndAgent"){
								$(".modal-body #gridUploadModal_gridSuccessNo").val( parseInt(obj.successNo)+2 );
							}else{*/
								$(".modal-body #gridUploadModal_gridSuccessNo").val( parseInt(obj.successNo) );
							/*}*/
							
							$(".modal-body #gridUploadModal_ErrorFilePath").val('');
						}
						else
							{
							$(".modal-body #gridUploadModal_ErrorFilePath").val( obj.filePath );
							$(".modal-body #gridUploadModal_gridErrorSheetList").val( obj.errorSheetList );
							$(".modal-body #gridUploadModal_gridValidationSheetList").val(obj.validationSheetList );
							$(".modal-body #gridUploadModal_SuccessFilePath").val( '' );
							}
						
					 });
					
					}
			},
			error: function(e){
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
			}
		});
		
		};
		
		
		$("#filesSelection").change(function(){
			
			$("#filesSelection").val() !="0"?$('.gridStatus').show():$('.gridStatus').hide();
			
			
			$(".modal-body #gridUploadModal_gridFileStatus").val( '-' );
			$(".modal-body #gridUploadModal_gridTotalSheetCount").val( '-' );
			$(".modal-body #gridUploadModal_gridSuccessNo").val( '-' );
			$(".modal-body #gridUploadModal_gridValidateNo").val( '-' );
			$(".modal-body #gridUploadModal_gridFileType").val( '-' );
			$(".modal-body #gridUploadModal_gridErrorSheetList").val( '-' );
			$(".modal-body #gridUploadModal_gridValidationSheetList").val( '-' );
			
			getGridFileList($("#gridUploadModal_transactionId").val(),$("#filesSelection").val());
		});
		
		$("#gridUploadModal_SuccessFileDownload").click(function(){

			if($("#gridUploadModal_SuccessFilePath").val().length > 0) {
				var successFilePath = $("#gridUploadModal_SuccessFilePath").val();
				successFilePath=encodeURIComponent(successFilePath);
				var contentType="application/octet-stream";
				var linkURL = "?downloadFilePath="+successFilePath+"&contentType="+contentType;
				$("#hiddenAnchor").attr("href","/rpa/commonDownloadMethod"+linkURL);
				$("#hiddenAnchor")[0].click();
			} else {
				
					toastr.clear();toastr.warning('No Success File');
					return false;
				}

			
		});

		$("#gridUploadModal_ErrorFileDownload").click(function(){

			if($("#gridUploadModal_ErrorFilePath").val().length > 0) {
				var filePath = $("#gridUploadModal_ErrorFilePath").val();
				filePath=encodeURIComponent(filePath);
				var contentType="application/octet-stream";
				var linkURL = "?downloadFilePath="+filePath+"&contentType="+contentType;
				$("#hiddenAnchor").attr("href","/rpa/commonDownloadMethod"+linkURL);
				$("#hiddenAnchor")[0].click();
			} else {
				
					toastr.clear();toastr.warning('No Error File');
					return false;
				
			}
	
		});	
	
		$("#gridUploadModal_CountFileDownload").click(function(){

			if($("#gridUploadModal_SuccessFilePath").val().length > 0) {
				var fileName= encodeURIComponent($("#filesSelection :selected").text());

				var contentType="application/octet-stream";
				var linkURL = "?transactionId="+$("#gridUploadModal_transactionId").val()+"&gridId="+$("#filesSelection :selected").val()+"&fileName="+fileName+"&contentType="+contentType;
				$("#hiddenAnchor").attr("href","/rpa/getGridSheetCountList"+linkURL);
				$("#hiddenAnchor")[0].click();
			} else {
				
					toastr.clear();toastr.warning('No Success File');
					return false;
				}

			
		});
	
	
	
		$('#marutiPolicyUploadTranModal').on('show.bs.modal', function(e) {

			var $modal = $(this),
			esseyId = e.relatedTarget.id;
			$.ajax({
				cache: false,
				type: 'POST',
				url: "/rpa/viewTransaction",
				data: { id: esseyId },
				success: function(data) {
					
					$(".modal-body #marutiPolicyUpload_transactionId").val( data.id );
					$(".modal-body #marutiPolicyUpload_externalTransactionRefNo").val( data.externalTransactionRefNo );
					$(".modal-body #marutiPolicyUpload_transactionStatus").val( data.transactionStatus );
					$(".modal-body #marutiPolicyUpload_processPhase").val( data.processPhase );
					$(".modal-body #marutiPolicyUpload_processStatus").val( data.processStatus );
					$(".modal-body #marutiPolicyUpload_processSuccessReason").val( data.processSuccessReason );
					$(".modal-body #marutiPolicyUpload_processFailureReason").val( data.processFailureReason );
					$(".modal-body #marutiPolicyUpload_PoliciedTaken").val( data.totalRecords );
					$(".modal-body #marutiPolicyUpload_PolicyPdfExtracted").val( data.totalSuccessRecords );
					$(".modal-body #marutiPolicyUpload_ProposalPdfExtracted").val( data.totalSuccessUploads );
					//getMarutiDailyStatus($("#marutiPolicyUpload_statusDate").val());
					getTransactionExceptionLog(data.id,"marutiPolicyUpload_exceptionLog");
				},
				error: function(e){
					toastr.clear();toastr.error("Record not found.");
				}
			});
		});

		$('#uploadReferenceModal').on('show.bs.modal', function(e) {
			$(".modal-body #uploadTime").val( '-' );
			$(".modal-body #requestXml").text( '-' );
			$(".modal-body #responseXml").text( '-' );
			var $modal = $(this),
			esseyId = e.relatedTarget.id;
			$.ajax({
				cache: false,
				type: 'POST',
				url: "/rpa/getUploadReference",
				data: { id: esseyId, flag : $("#"+esseyId).attr("name") },
				success: function(data) {
					debugger;
					$(".modal-body #uploadTime").val( new Date(data.uploadTime) );
					$(".modal-body #requestXml").text( data.requestXml );
					$(".modal-body #responseXml").text( data.responseXml );
					
				},
				error: function(e){
					toastr.clear();toastr.error("Record not found.");
				}
			});
		});
	
		$('#claimsDownloadTranModal').on('show.bs.modal', function(e) {

			var $modal = $(this),
			esseyId = e.relatedTarget.id;
			$.ajax({
				cache: false,
				type: 'POST',
				url: "/rpa/viewTransaction",
				data: { id: esseyId },
				success: function(data) {
					
					$(".modal-body #claimsDownload_transactionId").val( data.id );
					$(".modal-body #claimsDownload_externalTransactionRefNo").val( data.externalTransactionRefNo );
					$(".modal-body #claimsDownload_transactionStatus").val( data.transactionStatus );
					$(".modal-body #claimsDownload_processPhase").val( data.processPhase );
					$(".modal-body #claimsDownload_processStatus").val( data.processStatus );
					$(".modal-body #claimsDownload_processSuccessReason").val( data.processSuccessReason );
					$(".modal-body #claimsDownload_processFailureReason").val( data.processFailureReason );
					$(".modal-body #claimsDownload_claimsTaken").val( data.totalRecords );
					$(".modal-body #claimsDownload_claimsProcessed").val( data.totalUploadRecords );
					$(".modal-body #claimsDownload_withFile").val( data.totalSuccessUploads );
					$(".modal-body #claimsDownload_withoutFile").val( data.totalErrorUploads );
					getTransactionExceptionLog(data.id,"claimsDownload_exceptionLog");
				},
				error: function(e){
					toastr.clear();toastr.error("Record not found.");
				}
			});
		});
	
		$('#getclaimsCurrentStatus').click(function(){
			getclaimsCurrentStatus();
		});
		
		function getclaimsCurrentStatus(date){
			$("#claimsDownload_TotalClaims_cs").val( '-' );
			$("#claimsDownload_processedClaims_cs").val( '-' );
			$("#claimsDownload_withFile_cs").val( '-' );
			$("#claimsDownload_withoutFile_cs").val( '-' );
			$.ajax({
				cache: false,
				type: 'POST',
				url: "/rpa/getclaimsCurrentStatus",
				data: { policyDate: date },
				success: function(data) {
					$("#claimsDownload_TotalClaims_cs").val( data.totalClaims );
					$("#claimsDownload_processedClaims_cs").val( data.processedClaims );
					$("#claimsDownload_withFile_cs").val( data.claimsWithFiles );
					$("#claimsDownload_withoutFile_cs").val( data.claimsWithoutFiles );
				},
				error: function(e){
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
			});
		}
	
		
		$('#dailyFileTransferTranModal').on('show.bs.modal', function(e) {

			var $modal = $(this),
			esseyId = e.relatedTarget.id;
			$.ajax({
				cache: false,
				type: 'POST',
				url: "/rpa/viewTransaction",
				data: { id: esseyId },
				success: function(data) {
					
					$(".modal-body #dailyFileTransfer_transactionId").val( data.id );
					$(".modal-body #dailyFileTransfer_externalTransactionRefNo").val( data.externalTransactionRefNo );
					$(".modal-body #dailyFileTransfer_transactionStatus").val( data.transactionStatus );
					$(".modal-body #dailyFileTransfer_processPhase").val( data.processPhase );
					$(".modal-body #dailyFileTransfer_processStatus").val( data.processStatus );
					$(".modal-body #dailyFileTransfer_transferredCount").val( data.totalSuccessRecords );
					getTransactionExceptionLog(data.id,"dailyFileTransfer_exceptionLog");
				},
				error: function(e){
					toastr.clear();toastr.error("Record not found.");
				}
			});
		});
		
		function getInsertedAndupdatedRowCounts(gridId,flag){
			$(".gridWithModelRows").remove();
			$.ajax({
				cache: false,
				type: 'GET',
				url: 'getInsertedGridWithModelRowCount',
				data: { gridId:gridId, flag : flag },
				dataType: "json",
				success: function(data) {
					var txt = "";var totalRows = "0";var totalUpdatedRows = "0",remarks="";
					txt ='<div class="form-group row gridWithModelRows" style="display: flex;"><div class="col-2"><input type="text" class="form-control" value="Set" readonly="readonly" ></div><div class="col-3"><input style="text-align:right;" type="text" class="form-control" value="Inserted Rows" readonly="readonly"></div><div class="col-3"><input style="text-align:right;" type="text" class="form-control" value="updated RNE" readonly="readonly"></div><div class="col-4"><input style="text-align:right;" type="text" class="form-control" value="updated NB/RN" readonly="readonly"></div></div>';
					$.each(data, function(ind, obj){
						remarks = obj.remarks;
						txt +='<div class="form-group row gridWithModelRows" style="display: flex;"><div class="col-2"><input type="text" class="form-control" value="'+obj.sheetPairNo+'" readonly="readonly" ></div><div class="col-3"><input style="text-align:right;" type="text" class="form-control" value="'+obj.gridMasterRows+'" readonly="readonly"></div><div class="col-3"><input style="text-align:right;" type="text" class="form-control" value='+parseInt(obj.updateRneRows)+' readonly="readonly"></div><div class="col-4"><input style="text-align:right;" type="text" class="form-control" value='+parseInt(obj.updateNonRneRows)+' readonly="readonly"></div></div>';
						totalRows=parseInt(totalRows)+parseInt(obj.gridMasterRows);
						totalUpdatedRows=parseInt(totalUpdatedRows)+parseInt(parseInt(obj.updateRneRows)+parseInt(obj.updateNonRneRows));
					});
					if(txt!=""){
					txt +='<div class="form-group row gridWithModelRows" style="display: flex;"><div class="col-5"><input type="text" class="form-control" value="Total Rows Inserted" readonly="readonly"></div><div class="col-7"><input style="text-align:right;" type="text" class="form-control" value="'+totalRows+'" readonly="readonly"></div></div>';
					txt +='<div class="form-group row gridWithModelRows" style="display: flex;"><div class="col-5"><input type="text" class="form-control" value="Total Rows Updated" readonly="readonly"></div><div class="col-7"><input style="text-align:right;" type="text" class="form-control" value="'+totalUpdatedRows+'" readonly="readonly"></div></div>';
					txt +='<div class="form-group row gridWithModelRows" style="display: flex;"><div class="col-5"><input type="text" class="form-control" value="Remarks" readonly="readonly"></div><div class="col-7"><input style="text-align:right;" type="text" class="form-control" value="'+remarks+'" readonly="readonly"></div></div>';
					if(data.length>0){
					$("#gridForm").append(txt);
					}
				}
				}
		});
		}
		
		var tableProcessConfig = $('#tableProcessConfig').DataTable({
			"autoWidth": false,
			/* "fnDrawCallback": function( oSettings ) {
				 $("#tableProcessMail tbody tr").find("td:eq(2),td:eq(3)").css("max-width","200px");
			    },*/
			"columnDefs": [
			               {"targets": [ 0 ],
			            	   "visible": false,
			            	   "searchable": false}
			               ],
			               "ajax": {
			            	   "url": "/rpa/getProcessConfigurationDetails",
			            	   "type": "POST",
			            	   "success" :  function(data){
			            		   $.each(data, function(ind, obj){

			            			   tableProcessConfig.row.add([
			            			                      ind+1,
			            			                      obj.id,
			            			                      obj.processDesc,
			            			                      obj.processName,
			            			                      "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.id+"' desc_id='"+obj.processDesc+"' name_id='"+obj.processName+"'  data-target='#processConfigModal'>Edit</a>",
			            			                      ]).draw();
			            		   });
			            	   }
			               },
			              
		});
		
		
		$('#processConfigModal').on('show.bs.modal', function(e) {

			$("#processConfig_processDescEdit").val(e.relatedTarget.getAttribute('desc_id'));
			$("#processConfig_processNameEdit").val(e.relatedTarget.getAttribute('name_id'));
			$("#processConfig_processIdEdit").val(e.relatedTarget.id);
		});
		
		
		$("#buttonProcessConfigRefresh").click(function(){

			tableProcessConfig.clear().draw();
			tableProcessConfig.ajax.reload();

		});
		
		
		$("#buttonProcessConfigInsert").click(function(){
			$(this).callProcessConfigAjax("insertProcessConfig", "");
		});
		
		
		$.fn.callProcessConfigAjax = function( method, checkeds ){
			showLoader();
			/*var processId ="";
			if($("#processConfig_id").val()!=null && $("#processConfig_id").val()!=undefined && $("#processConfig_id").val()!=""){
				processId = $("#processConfig_id").val();
			}else{
				hideLoader();
				toastr.warning("Process Id cannot be empty");
				return false;
			}*/
			
			var processDesc ="";
			if($("#processConfig_desc").val()!=null && $("#processConfig_desc").val()!=undefined && $("#processConfig_desc").val()!=""){
				processDesc = $("#processConfig_desc").val();
			}else{
				hideLoader();
				toastr.warning("Process Desc cannot be empty");
				return false;
			}
			
			var processName ="";
			if($("#processConfig_name").val()!=null && $("#processConfig_name").val()!=undefined && $("#processConfig_name").val()!=""){
				processName = $("#processConfig_name").val();
			}else{
				hideLoader();
				toastr.warning("Process Name cannot be empty");
				return false;
			}
			

			$.ajax({
				type: "POST",
				url: "/rpa/" + method,
				timeout : 100000,
				data: { /*processId: processId,*/ processDesc :processDesc,processName:processName},

					success: function(data){
						tableProcessConfig.clear().draw();
						tableProcessConfig.ajax.reload();
						toastr.clear();toastr.info(data);
						$("#processConfig_desc,#processConfig_name,#processConfig_id").val('');
						hideLoader();
					},
					error: function(e){
						hideLoader();
						toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
					}
			});
		}
		
		
		$("#buttonProcessConfigUpdate").click(function(){
			$(this).callProcessConfigAjaxUpdate("updateProcessConfig");
		});
		
		$.fn.callProcessConfigAjaxUpdate = function( method ){
			showLoader();

			$.ajax({
				type: "POST",
				url: "/rpa/" + method,
				timeout : 100000,
				data: { processId: $("#processConfig_processIdEdit").val(),processDesc: $("#processConfig_processDescEdit").val() , processName: $("#processConfig_processNameEdit").val()},

					success: function(data){
						tableProcessConfig.clear().draw();
						tableProcessConfig.ajax.reload();
						toastr.clear();toastr.info(data);
						hideLoader();
					},
					error: function(e){
						hideLoader();
						toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
					}
			});
		} 
		
		$("#buttonProcessConfigDelete").click(function(){
			$(this).callProcessConfigAjaxDelete("processConfigDelete");
		});
		
		$.fn.callProcessConfigAjaxDelete = function( method ){
			showLoader();

			$.ajax({
				type: "POST",
				url: "/rpa/" + method,
				timeout : 100000,
				data: { processId: $("#processConfig_processIdEdit").val()},

					success: function(data){
						tableProcessConfig.clear().draw();
						tableProcessConfig.ajax.reload();
						toastr.clear();toastr.info(data);
						hideLoader();
					},
					error: function(e){
						hideLoader();
						toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
					}
			});
		} 
		
		var tableUserProcessConfig = $('#tableUserProcessConfig').DataTable({
			"autoWidth": false,
			/* "fnDrawCallback": function( oSettings ) {
				 $("#tableProcessMail tbody tr").find("td:eq(2),td:eq(3)").css("max-width","200px");
			    },*/
			"columnDefs": [
			               {"targets": [ 0 ],
			            	   "visible": false,
			            	   "searchable": false}
			               ],
			               "ajax": {
			            	   "url": "/rpa/getUserProcessDetails",
			            	   "type": "POST",
			            	   "success" :  function(data){
			            		   $.each(data, function(ind, obj){

			            			   tableUserProcessConfig.row.add([
			            			                      ind+1,
			            			                      obj.userId,
			            			                      obj.processID,
			            			                      "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.userId+"' process_id='"+obj.processID+"'  data-target='#userProcessConfigModal'>Delete</a>",
			            			                      ]).draw();
			            		   });
			            	   }
			               },
			              
		});
		
		
		$('#userProcessConfigModal').on('show.bs.modal', function(e) {

			$("#userProcessConfig_userIdEdit").val(e.relatedTarget.getAttribute('id'));
			$("#userProcessConfig_processIdEdit").val(e.relatedTarget.getAttribute('process_id'));
		});
		
		
		$("#buttonUserProcessConfigRefresh").click(function(){

			tableUserProcessConfig.clear().draw();
			tableUserProcessConfig.ajax.reload();

		});
		
		
		$("#buttonUserProcessConfigInsert").click(function(){
			$(this).callUserProcessConfigAjax("insertUserProcessConfig", "");
		});
		
		
		$.fn.callUserProcessConfigAjax = function( method, checkeds ){
			showLoader();
			
			var userid ="";
			if($("#userProcess_userid").val()!=null && $("#userProcess_userid").val()!=undefined && $("#userProcess_userid").val()!=""){
				userid = $("#userProcess_userid").val();
			}else{
				hideLoader();
				toastr.warning("User Id cannot be empty");
				return false;
			}
			
			var processId ="";
			if($("#UserProcess_processid").val()!=null && $("#UserProcess_processid").val()!=undefined && $("#UserProcess_processid").val()!=""){
				processId = $("#UserProcess_processid").val();
			}else{
				hideLoader();
				toastr.warning("Process Id cannot be empty");
				return false;
			}
			

			$.ajax({
				type: "POST",
				url: "/rpa/" + method,
				timeout : 100000,
				data: { userId : userid ,processId:processId},

					success: function(data){
						tableUserProcessConfig.clear().draw();
						tableUserProcessConfig.ajax.reload();
						toastr.clear();toastr.info(data);
						$("#userProcess_userid,#UserProcess_processid").val('');
						hideLoader();
					},
					error: function(e){
						hideLoader();
						toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
					}
			});
		}
		
		
		$("#buttonUserProcessConfigDelete").click(function(){
			$(this).callUserProcessConfigAjaxDelete("userProcessConfigDelete");
		});
		
		$.fn.callUserProcessConfigAjaxDelete = function( method ){
			showLoader();

			$.ajax({
				type: "POST",
				url: "/rpa/" + method,
				timeout : 100000,
				data: { userId: $("#userProcessConfig_userIdEdit").val(),processId: $("#userProcessConfig_processIdEdit").val() },

					success: function(data){
						tableUserProcessConfig.clear().draw();
						tableUserProcessConfig.ajax.reload();
						toastr.clear();toastr.info(data);
						hideLoader();
					},
					error: function(e){
						hideLoader();
						toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
					}
			});
		} 
		
		
		
		
		var tableParamConfig = $('#tableParamConfig').DataTable({
			"autoWidth": false,
			/* "fnDrawCallback": function( oSettings ) {
				 $("#tableProcessMail tbody tr").find("td:eq(2),td:eq(3)").css("max-width","200px");
			    },*/
			"columnDefs": [
			               {"targets": [ 0 ],
			            	   "visible": false,
			            	   "searchable": false}
			               ],
			               "ajax": {
			            	   "url": "/rpa/getParamConfigDetails",
			            	   "type": "POST",
			            	   "success" :  function(data){
			            		   $.each(data, function(ind, obj){

			            			   tableParamConfig.row.add([
			            			                      ind+1,
			            			                      obj.paramKey,
			            			                      obj.paramValue,
			            			                      "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.paramKey+"' paramValue_id='"+obj.paramValue+"'  data-target='#paramConfigModal'>Edit</a>",
			            			                      ]).draw();
			            		   });
			            	   }
			               },
			              
		});
		
		
		$('#paramConfigModal').on('show.bs.modal', function(e) {

			$("#paramConfig_paramKeyEdit").val(e.relatedTarget.getAttribute('id'));
			$("#paramConfig_paramValueEdit").val(e.relatedTarget.getAttribute('paramValue_id'));
		});
		
		
		$("#buttonParamConfigRefresh").click(function(){

			tableParamConfig.clear().draw();
			tableParamConfig.ajax.reload();

		});
		
		
		$("#buttonParamConfigInsert").click(function(){
			$(this).callParamConfigAjax("insertParamConfig", "");
		});
		
		
		$.fn.callParamConfigAjax = function( method, checkeds ){
			showLoader();
			
			var paramKey ="";
			if($("#paramConfig_paramKey").val()!=null && $("#paramConfig_paramKey").val()!=undefined && $("#paramConfig_paramKey").val()!=""){
				paramKey = $("#paramConfig_paramKey").val();
			}else{
				hideLoader();
				toastr.warning("Param key cannot be empty");
				return false;
			}
			
			var paramValue ="";
			if($("#paramConfig_paramValue").val()!=null && $("#paramConfig_paramValue").val()!=undefined && $("#paramConfig_paramValue").val()!=""){
				paramValue = $("#paramConfig_paramValue").val();
			}else{
				hideLoader();
				toastr.warning("Param value cannot be empty");
				return false;
			}
			

			$.ajax({
				type: "POST",
				url: "/rpa/" + method,
				timeout : 100000,
				data: { paramKey : paramKey ,paramValue:paramValue},

					success: function(data){
						tableParamConfig.clear().draw();
						tableParamConfig.ajax.reload();
						toastr.clear();toastr.info(data);
						$("#paramConfig_paramKey,#paramConfig_paramValue").val('');
						hideLoader();
					},
					error: function(e){
						hideLoader();
						toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
					}
			});
		}
		
		
		$("#buttonParamConfigDelete").click(function(){
			$(this).callParamConfigAjaxDelete("paramConfigDelete");
		});
		
		$.fn.callParamConfigAjaxDelete = function( method ){
			showLoader();

			$.ajax({
				type: "POST",
				url: "/rpa/" + method,
				timeout : 100000,
				data: { paramKey: $("#paramConfig_paramKeyEdit").val(),paramValue: $("#paramConfig_paramValueEdit").val() },

					success: function(data){
						tableParamConfig.clear().draw();
						tableParamConfig.ajax.reload();
						toastr.clear();toastr.info(data);
						hideLoader();
					},
					error: function(e){
						hideLoader();
						toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
					}
			});
		} 
		
		
		$("#buttonParamConfigUpdate").click(function(){
			$(this).callParamConfigAjaxDelete("paramConfigUpdate");
		});
		
		
		$('#policyPdfMailTriggerTranModal').on('show.bs.modal', function(e) {

			var $modal = $(this),
			obj = e.relatedTarget.id;
			$.ajax({
				cache: false,
				type: 'POST',
				url: "/rpa/viewTransaction",
				data: { id: obj },
				success: function(data) {
					getPdfMailTriggerStatus(data.id);
					
					$(".modal-body #policyPdfMailTrigger_transactionId").val( data.id );
					$(".modal-body #policyPdfMailTrigger_transactionStatus").val( data.transactionStatus );
					$(".modal-body #policyPdfMailTrigger_processPhase").val( data.processPhase );
					$(".modal-body #policyPdfMailTrigger_processStatus").val( data.processStatus );
					$(".modal-body #policyPdfMailTrigger_processFaliureReason").val( data.processFailureReason );
					getTransactionExceptionLog(data.id,"policyPdfMailTrigger_exceptionLog");
				},
				error: function(e){
					toastr.clear();toastr.error("Record not found.");
				}
			});
		});
		
		
		function getPdfMailTriggerStatus(id){
			
			$.ajax({
				cache: false,
				type: 'POST',
				url: "/rpa/getPdfMailTriggerStatus",
				data: { id: id },
				success: function(data) {
					
					$(".modal-body #policyPdfMailTrigger_fileName").val( data.fileName );
					$(".modal-body #policyPdfMailTrigger_filePath").val( data.filePath );
					if(data.isExcelCreated=="Y"){
						$(".modal-body #policyPdfMailTrigger_isExcelCreated").val( "YES" );
					}else{
						$(".modal-body #policyPdfMailTrigger_isExcelCreated").val( "NO" );
					}
					
					if(data.isExcelUploaded=="Y"){
						$(".modal-body #policyPdfMailTrigger_isExcelUploaded").val( "YES" );
					}else{
						$(".modal-body #policyPdfMailTrigger_isExcelUploaded").val( "NO" );
					}
					
					
					$(".modal-body #policyPdfMailTrigger_quoteCount").val( data.quoteCount );
				},
				error: function(e){
					toastr.clear();toastr.error("Record not found.");
				}
			});
			
		}
		
		$(document).ready(function() {	
			$("#leadTbl,#agent2WTbl").hide();
			//$(".perPolicy,.agentAccessLate,.agentSlab,.inHouseTL,.tlSlab,.inHouseTlSlab,.plSlab,.leadTarget").hide();
			
			var tableIncentive = $('#tableIncentive').DataTable({
				  dom: 'Bfrtip',
				    buttons: [
				    {
				      extend: 'excel',
				      text: 'Export Agent',
				      className: 'exportExcel',
				      filename: 'Export excel',
				      exportOptions: {
				        modifier: {
				          page: 'all'
				        }
				      }
				    }
				    ],
				"autoWidth": false,
				"columnDefs": [
				               {"targets": [ 0 ],
				            	   "visible": false,
				            	   "searchable": false},
				            	   { targets: [2, 3, 4, 5, 6, 8, 7, 9,10,11], className: 'dt-body-right' }
				               ],
				               "ajax": {
				            	   "url": "/rpa/getIncentiveReportDetails",
				            	   data: { year: $("#incentiveYear").val(), month : $("#incentiveMonth").val(),type: $("#incecntiveType").val()  },
				            	   "type": "POST",
				            	   "success" :  function(data){
				            		  /* if(data.length>0){*/
				            		   $.each(data, function(ind, obj){
				            			   tableIncentive.clear().draw();
				            				tableIncentive.ajax.reload();
				            			   tableIncentive.row.add([
				            			                      ind+1,
				            			                      obj.name,
				            			                      obj.accessType,
				            			                      obj.totOdPremium,
				            			                      obj.slab,
				            			                      obj.motorOdIncentive,
				            			                      obj.noOfTwoWheelerPolicies,
				            			                      obj.twoWheelerIncentive
				            			                      ]).draw();
				            		   });
				            		  /* }else{
				            			   toastr.clear();
				            			   toastr.warning("No Record to show");
				            		   }*/
				            	   }
				               },
			});
			
			var tableLeadIncentive = $('#tableLeadIncentive').DataTable({
				  dom: 'Bfrtip',
				    buttons: [
				    {
				      extend: 'excel',
				      text: 'Export Lead',
				      className: 'exportExcel',
				      filename: 'Export excel',
				      exportOptions: {
				        modifier: {
				          page: 'all'
				        }
				      }
				    }
				    ],
				"autoWidth": false,
				"columnDefs": [
				               {"targets": [ 0 ],
				            	   "visible": false,
				            	   "searchable": false},
				               { targets: [2, 3, 4, 5, 6], className: 'dt-body-right' }
				               ],
				               "ajax": {
				            	   "url": "/rpa/getIncentiveReportDetails",
				            	   data: { year: $("#incentiveYear").val(), month : $("#incentiveMonth").val(),type: $("#incecntiveType").val()  },
				            	   "type": "POST",
				            	   "success" :  function(data){
				            		  /* if(data.length>0){*/
				            		   $.each(data, function(ind, obj){
				            			   tableLeadIncentive.clear().draw();
				            			   tableLeadIncentive.ajax.reload();
				            			   tableLeadIncentive.row.add([
				            			                      ind+1,
				            			                      obj.name,
				            			                      obj.accessType,
				            			                      obj.totOdPremium,
				            			                      obj.slab,
				            			                      obj.motorOdIncentive,
				            			                      obj.noOfTwoWheelerPolicies,
				            			                      obj.twoWheelerIncentive
				            			                      ]).draw();
				            		   });
				            		  /* }else{
				            			   toastr.clear();
				            			   toastr.warning("No Record to show");
				            		   }*/
				            	   }
				               },
			});
			
			var tableAgent2WIncentive = $('#tableAgent2WIncentive').DataTable({
				  dom: 'Bfrtip',
				    buttons: [
				    {
				      extend: 'excel',
				      text: 'Export 2W Agent',
				      className: 'exportExcel',
				      filename: 'Export excel',
				      exportOptions: {
				        modifier: {
				          page: 'all'
				        }
				      }
				    }
				    ],
				"autoWidth": false,
				"columnDefs": [
				               {"targets": [ 0 ],
				            	   "visible": false,
				            	   "searchable": false},
				            	   { targets: [2, 3, 4], className: 'dt-body-right' }
				               ],
				               "ajax": {
				            	   "url": "/rpa/getIncentiveReportDetails",
				            	   data: { year: $("#incentiveYear").val(), month : $("#incentiveMonth").val(),type: $("#incecntiveType").val()  },
				            	   "type": "POST",
				            	   "success" :  function(data){
				            		  /* if(data.length>0){*/
				            		   $.each(data, function(ind, obj){
				            			   tableAgent2WIncentive.clear().draw();
				            			   tableAgent2WIncentive.ajax.reload();
				            			   tableAgent2WIncentive.row.add([
				            			                      ind+1,
				            			                      obj.name,
				            			                      obj.accessType,
				            			                      obj.totOdPremium,
				            			                      obj.slab,
				            			                      obj.motorOdIncentive,
				            			                      obj.noOfTwoWheelerPolicies,
				            			                      obj.twoWheelerIncentive
				            			                      ]).draw();
				            		   });
				            		  /* }else{
				            			   toastr.clear();
				            			   toastr.warning("No Record to show");
				            		   }*/
				            	   }
				               },
			});
			
			$("#buttonIncentiveReport").click(function(){
				if($("#incecntiveType").val()=="AG"){
					$("#leadTbl,#agent2WTbl").hide();
					$("#agentTbl").show();
				$.ajax({
					type: "POST",
					url: "/rpa/getIncentiveReportDetails",
					timeout : 100000,
					data: { year: $("#incentiveYear").val(), month : $("#incentiveMonth").val(),type: $("#incecntiveType").val()},
						success: function(data){
							 tableIncentive.clear().draw();
	            				tableIncentive.ajax.reload();
							$.each(data, function(ind, obj){
		            			   tableIncentive.row.add([
		            			                      ind+1,
		            			                      obj.name,
		            			                   /*   obj.accessType,*/
		            			                      obj.searchSlab,
		            			                      obj.search,
		            			                      obj.searchInsentive,
		            			                      obj.wsiSlab,
		            			                      obj.wsi,
		            			                      obj.wsiIncentive,
		            			                      obj.totOdPremium,
		            			                      obj.totalIncentive,
		            			                      obj.lateLogin,
		            			                      obj.finalIncentive,
		            			                     /* obj.noOfTwoWheelerPolicies,
		            			                      obj.twoWheelerIncentive,*/
		            			                      obj.monthYear
		            			                      ]).draw();
		            		   });
							hideLoader();
						},
						error: function(e){
							hideLoader();
							toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
						}
				});
			}else if($("#incecntiveType").val()=="AG-2W"){
				
				$("#leadTbl,#agentTbl").hide();
				$("#agent2WTbl").show();
				$.ajax({
					type: "POST",
					url: "/rpa/getIncentiveReportDetails",
					timeout : 100000,
					data: { year: $("#incentiveYear").val(), month : $("#incentiveMonth").val(),type: $("#incecntiveType").val()},
						success: function(data){
							tableAgent2WIncentive.clear().draw();
							tableAgent2WIncentive.ajax.reload();
							$.each(data, function(ind, obj){
								tableAgent2WIncentive.row.add([
		            			                      ind+1,
		            			                      obj.name,
		            			                      obj.noOfTwoWheelerPolicies,
		            			                      obj.perPolicy,
		            			                      obj.twoWheelerIncentive,
		            			                      obj.monthYear
		            			                      ]).draw();
		            		   });
							hideLoader();
						},
						error: function(e){
							hideLoader();
							toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
						}
				});
				
			}else{
				$("#leadTbl").show();
				$("#agentTbl,#agent2WTbl").hide();
				$.ajax({
					type: "POST",
					url: "/rpa/getIncentiveReportDetails",
					timeout : 100000,
					data: { year: $("#incentiveYear").val(), month : $("#incentiveMonth").val(),type: $("#incecntiveType").val()},
						success: function(data){
							tableLeadIncentive.clear().draw();
							tableLeadIncentive.ajax.reload();
							$.each(data, function(ind, obj){
								tableLeadIncentive.row.add([
		            			                      ind+1,
		            			                      obj.name,
		            			                      obj.totOdPremium,
		            			                      obj.totalIncentive,
		            			                      obj.noOfTwoWheelerPolicies,
		            			                      obj.twoWheelerIncentive,
		            			                      obj.finalIncentive,
		            			                      obj.monthYear
		            			                      ]).draw();
		            		   });
							hideLoader();
						},
						error: function(e){
							hideLoader();
							toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
						}
				});
			}
		})
	});
		
		$('#AgentIncentiveCalculatorProcessTranModal').on('show.bs.modal', function(e) {

			var $modal = $(this),
			obj = e.relatedTarget.id;
			$.ajax({
				cache: false,
				type: 'POST',
				url: "/rpa/viewTransaction",
				data: { id: obj },
				success: function(data) {
					getAgentIncentiveStatus(data.id);
					
					$(".modal-body #agentIncentive_transactionId").val( data.id );
					$(".modal-body #agentIncentive_transactionStatus").val( data.transactionStatus );
					$(".modal-body #agentIncentive_processPhase").val( data.processPhase );
					$(".modal-body #agentIncentive_processStatus").val( data.processStatus );
					$(".modal-body #agentIncentive_processFaliureReason").val( data.processFailureReason );
					getTransactionExceptionLog(data.id,"agentIncentive_exceptionLog");
				},
				error: function(e){
					toastr.clear();toastr.error("Record not found.");
				}
			});
		});
		
		
		function getAgentIncentiveStatus(id){

			$.ajax({
				cache: false,
				type: 'POST',
				url: "/rpa/getAgentIncentiveStatus",
				data: { id: id },
				success: function(data) {
					
					//$(".modal-body #policyPdfMailTrigger_fileName").val( data.fileName );
					$(".modal-body #agentIncentive_xgenFilePath").val( data.xGenFilePath );
					$(".modal-body #agentIncentive_crmFilePath").val( data.d2cFilePath );
					if(data.isXgenFileAvailable=="Y"){
						$(".modal-body #agentIncentive_isXgenAvailable").val( "YES" );
					}else{
						$(".modal-body #agentIncentive_isXgenAvailable").val( "NO" );
					}
					if(data.isCrmFileAvailable=="Y"){
						$(".modal-body #agentIncentive_isCrmAvailable").val( "YES" );
					}else{
						$(".modal-body #agentIncentive_isCrmAvailable").val( "NO" );
					}
					
					if(data.isValidationSucceeded=="Y"){
						$(".modal-body #agentIncentive_isValidationSucceeded").val( "YES" );
					}else{
						$(".modal-body #agentIncentive_isValidationSucceeded").val( "NO" );
					}
					
					if(data.isDataUploadedInDb=="Y"){
						$(".modal-body #agentIncentive_isDataUploaded").val( "YES" );
					}else{
						$(".modal-body #agentIncentive_isDataUploaded").val( "NO" );
					}
					
					if(data.isIncentiveCalculated=="Y"){
						$(".modal-body #agentIncentive_isIncentiveCalculated").val( "YES" );
					}else{
						$(".modal-body #agentIncentive_isIncentiveCalculated").val( "NO" );
					}
					
					
					$(".modal-body #policyPdfMailTrigger_quoteCount").val( data.quoteCount );
				},
				error: function(e){
					toastr.clear();toastr.error("Record not found.");
				}
			});
			
		}
		
		 
		 
		 
		
