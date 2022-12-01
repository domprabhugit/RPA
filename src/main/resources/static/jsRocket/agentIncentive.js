var rowId = 0;
var rowIdLeadTarget = 0;
$(document).ready(function() {	
	//$(".perPolicy").show();
	$(".agentAccessLate,.agentSlab,.inHouseTL,.tlSlab,.inHouseTlSlab,.plSlab,.leadTarget").hide();
			
	var tblPerPolicy = $('#tblPerPolicy').DataTable({
		"autoWidth": false,
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getPerPolicyDetails",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){
		            			   tblPerPolicy.row.add([
		            			                      ind+1,
		            			                      obj.key,
		            			                      obj.value,
		            			                      "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.key+"@@"+obj.value+"' data-target='#perPolicyModal'>Edit</a>",
		            			                      ]).draw();
		            		   });
		            	   }
		               },
	});	
	
	$('#perPolicyModal').on('show.bs.modal', function(e) {
		values = e.relatedTarget.id;
		$(".modal-body #key_edit").val( values.split("@@")[0] );
		$(".modal-body #value_edit").val( values.split("@@")[1] );
	});
	
	
	var tableInHouseLeads = $('#tableInHouseLeads').DataTable({
		"autoWidth": false,
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getInHouseLeads",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){
		            			   tableInHouseLeads.row.add([
		            			                      ind+1,
		            			                      obj.leadName
		            			                      ]).draw();
		            		   });
		            	   }
		               },
	});	
	
	var tableAgentSlab = $('#tableAgentSlab').DataTable({
		"autoWidth": false,
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getAgentSlab",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){
		            			   tableAgentSlab.row.add([
		            			                      ind+1,
		            			                      obj.odMinSlab,
		            			                      obj.odMaxSlab,
		            			                      obj.pgqPercentage,
		            			                      obj.wpiPercentage,
		            			                      obj.wsiPercentage,
		            			                      obj.inbPercentage,
		            			                      "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.odMinSlab+"@@"+obj.odMaxSlab+"@@"+obj.pgqPercentage+"@@"+obj.wpiPercentage+"@@"+obj.wsiPercentage+"@@"+obj.inbPercentage+"' data-target='#agentSlabModal'>Edit</a>",
		            			                      ]).draw();
		            		   });
		            	   }
		               },
	});	
	
	$('#agentSlabModal').on('show.bs.modal', function(e) {
		values = e.relatedTarget.id;
		$(".modal-body #agentSlabOdMin_edit").val( values.split("@@")[0] );
		$(".modal-body #agentSlabOdMax_edit").val( values.split("@@")[1] );
		$(".modal-body #agentSlabPQG_edit").val( values.split("@@")[2] );
		$(".modal-body #agentSlabWPI_edit").val( values.split("@@")[3] );
		$(".modal-body #agentSlabWSI_edit").val( values.split("@@")[4] );
		$(".modal-body #agentSlabINB_edit").val( values.split("@@")[5] );
	});
	
	var tableTlSlab = $('#tableTlSlab').DataTable({
		"autoWidth": false,
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getTlSlab",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){
		            			   tableTlSlab.row.add([
		            			                      ind+1,
		            			                      obj.percentage_70_To_85,
		            			                      obj.percenatge_85_To_90,
		            			                      obj.percenatge_90_To_95,
		            			                      obj.percenatge_95_To_100,
		            			                      obj.percenatge_100_Above,
		            			                      "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.percentage_70_To_85+"@@"+obj.percenatge_85_To_90+"@@"+obj.percenatge_90_To_95+"@@"+obj.percenatge_95_To_100+"@@"+obj.percenatge_100_Above+"@@"+obj.id+"' data-target='#tlSlabModal'>Edit</a>",
		            			                      ]).draw();
		            		   });
		            	   }
		               },
	});	
	
	$('#tlSlabModal').on('show.bs.modal', function(e) {
		values = e.relatedTarget.id;
		$(".modal-body #70to85_edit").val( values.split("@@")[0] );
		$(".modal-body #85to90_edit").val( values.split("@@")[1] );
		$(".modal-body #90to95_edit").val( values.split("@@")[2] );
		$(".modal-body #95to100_edit").val( values.split("@@")[3] );
		$(".modal-body #100AndAbove_edit").val( values.split("@@")[4] );
		$(".modal-body #tlSlabId").val( values.split("@@")[5] );
	});
	
	var tablePlSlab = $('#tablePlSlab').DataTable({
		"autoWidth": false,
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getPlSlab",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){
		            			   tablePlSlab.row.add([
		            			                      ind+1,
		            			                      obj.percentage_70_To_85,
		            			                      obj.percenatge_85_To_90,
		            			                      obj.percenatge_90_To_95,
		            			                      obj.percenatge_95_To_100,
		            			                      obj.percenatge_100_Above,
		            			                      "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.percentage_70_To_85+"@@"+obj.percenatge_85_To_90+"@@"+obj.percenatge_90_To_95+"@@"+obj.percenatge_95_To_100+"@@"+obj.percenatge_100_Above+"@@"+obj.id+"' data-target='#tlSlabModal'>Edit</a>",
		            			                      ]).draw();
		            		   });
		            	   }
		               },
	});	
	
	
	var tableTlInHouseSlab = $('#tableTlInHouseSlab').DataTable({
		"autoWidth": false,
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getTlInHouseSlab",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){
		            			   tableTlInHouseSlab.row.add([
		            			                      ind+1,
		            			                      obj.odMinSlab,
		            			                      obj.odMaxSlab,
		            			                      obj.target,
		            			                      obj.incentivePercentage,
		            			                      "<a href='#myModal' class='btn btn-info' data-toggle='modal' id='"+obj.odMinSlab+"@@"+obj.odMaxSlab+"@@"+obj.target+"@@"+obj.incentivePercentage+"@@"+obj.id+"' data-target='#inHouseTlSlabModal'>Edit</a>",
		            			                      ]).draw();
		            		   });
		            	   }
		               },
	});	
	
	$('#inHouseTlSlabModal').on('show.bs.modal', function(e) {
		values = e.relatedTarget.id;
		$(".modal-body #inHouseTlodMin_edit").val( values.split("@@")[0] );
		$(".modal-body #inHouseTlodMax_edit").val( values.split("@@")[1] );
		$(".modal-body #inHouseTarget_edit").val( values.split("@@")[2] );
		$(".modal-body #inHouseTlIncentivePerc_edit").val( values.split("@@")[3] );
		$(".modal-body #inHouseTlSlabId").val( values.split("@@")[4] );
	});
	
	var tableAgentAccessLate = $('#tableAgentAccessLate').DataTable({
		"autoWidth": false,
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getAgentAccessLate",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){
		            			   tableAgentAccessLate.row.add([
		            			                      ind+1,
		            			                      obj.agentName,
		            			                      obj.leadAccess,
		            			                      obj.lateLogin,
		            			                      obj.monthYear
		            			                      ]).draw();
		            		   });
		            	   }
		               },
	});	
	
	var tableLeadTarget = $('#tableLeadTarget').DataTable({
		"autoWidth": false,
		"columnDefs": [
		               {"targets": [ 0 ],
		            	   "visible": false,
		            	   "searchable": false}
		               ],
		               "ajax": {
		            	   "url": "/rpa/getLeadTarget",
		            	   "type": "POST",
		            	   "success" :  function(data){
		            		   $.each(data, function(ind, obj){
		            			   tableLeadTarget.row.add([
		            			                      ind+1,
		            			                      obj.name,
		            			                      obj.leadType,
		            			                      obj.target,
		            			                      obj.month,
		            			                      obj.year
		            			                      ]).draw();
		            		   });
		            	   }
		               },
	});	
	
	$("#masterType").change(function(){
		 var mType = $("#masterType").val();
		if(mType=="1"){
			$(".agentAccessLate,.agentSlab,.inHouseTL,.tlSlab,.inHouseTlSlab,.plSlab,.leadTarget").hide();
			$(".perPolicy,#buttonMasterInsert").show();
		}else if(mType=="2"){
			$(".perPolicy,.agentSlab,.inHouseTL,.tlSlab,.inHouseTlSlab,.plSlab,.leadTarget,#buttonMasterInsert").hide();
			$(".agentAccessLate").show();
		}else if(mType=="3"){
			$(".perPolicy,.agentAccessLate,.inHouseTL,.tlSlab,.inHouseTlSlab,.plSlab,.leadTarget").hide();
			$(".agentSlab,#buttonMasterInsert").show();
		}else if(mType=="4"){
			$(".perPolicy,.agentAccessLate,.agentSlab,.tlSlab,.inHouseTlSlab,.plSlab,.leadTarget,#buttonMasterInsert").hide();
			$(".inHouseTL").show();
		}else if(mType=="5"){
			$(".perPolicy,.agentAccessLate,.agentSlab,.inHouseTL,.inHouseTlSlab,.plSlab,.leadTarget,#buttonMasterInsert").hide();
			$(".tlSlab").show();
		}else if(mType=="6"){
			$(".perPolicy,.agentAccessLate,.agentSlab,.tlSlab,.inHouseTL,.plSlab,.leadTarget").hide();
			$(".inHouseTlSlab,#buttonMasterInsert").show();
		}else if(mType=="7"){
			$(".perPolicy,.agentAccessLate,.agentSlab,.tlSlab,.inHouseTL,.inHouseTlSlab,.leadTarget,#buttonMasterInsert").hide();
			$(".plSlab").show();
		}else if(mType=="8"){
			$(".perPolicy,.agentAccessLate,.agentSlab,.tlSlab,.inHouseTL,.plSlab,.inHouseTlSlab,#buttonMasterInsert").hide();
			$(".leadTarget").show();
		}
		
	 });
	
	$(".add-row").click(function(){
		rowId++;
		var txtRow = '<div class="form-group row agentAccessLate" id="'+rowId+'"><div class="col-md-3"><input name="2_agentName-'+rowId+'" type="text" class="form-control agentAccessLate-'+rowId+'" placeholder="" /></div><div class="col-md-2"><input name="2_leadAccess-'+rowId+'" type="text" class="form-control agentAccessLate-'+rowId+'" placeholder="" /></div><div class="col-md-2"><input name="2_lateLogin-'+rowId+'" type="text" class="form-control agentAccessLate-'+rowId+'" placeholder="" /></div><div class="col-md-2"><input name="2_monthYear-'+rowId+'" type="text" class="form-control agentAccessLate-'+rowId+'" placeholder="" /></div><div class="col-md-1"><input type="button" value="--" onclick="removeRow('+rowId+')" /></div></div>';
		$("#agentAccessLaterForm").append(txtRow);
	});
	
	$(".add-row-leadTarget").click(function(){
		rowId++;
		var txtRow = '<div class="form-group row agentAccessLate" id="'+rowIdLeadTarget+'"><div class="col-md-3"><input name="8_name-'+rowIdLeadTarget+'" type="text" class="form-control agentAccessLate-'+rowIdLeadTarget+'" placeholder="" /></div><div class="col-md-2"><input name="2_target-'+rowIdLeadTarget+'" type="text" class="form-control agentAccessLate-'+rowIdLeadTarget+'" placeholder="" /></div><div class="col-md-2"><input name="2_monthYear-'+rowIdLeadTarget+'" type="text" class="form-control agentAccessLate-'+rowIdLeadTarget+'" placeholder="" /></div><div class="col-md-2"><input name="2_leadType-'+rowIdLeadTarget+'" type="text" class="form-control agentAccessLate-'+rowIdLeadTarget+'" placeholder="" /></div><div class="col-md-1"><input type="button" value="--" onclick="removeRow('+rowIdLeadTarget+')" /></div></div>';
		$("#leadTargetForm").append(txtRow);
	});
	
	$("#buttonMasterInsert").click(function(){
		var mType = $("#masterType").val();
		if(mType=="1"){
			$.ajax({
				type: "POST",
				url: "/rpa/insertPerPolicy",
				timeout : 100000,
				data: { key :$("#1_key").val(), value:$("#1_value").val()},
	
					success: function(data){
						tblPerPolicy.clear().draw();
						tblPerPolicy.ajax.reload();
						if(data.indexOf("Already")!=-1){
							toastr.clear();toastr.warning(data);
						}
						else{
							toastr.clear();toastr.info(data);
							$("#1_key,#1_value").val('');
						}
						
						hideLoader();
					},
					error: function(e){
						hideLoader();
						toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
					}
			});
	}else if(mType=="3"){
		$.ajax({
			type: "POST",
			url: "/rpa/insertAgentSlab",
			timeout : 100000,
			data: { odMinSlab  :$("#3_odMinSlab").val(), odMaxSlab:$("#3_odMaxSlab").val(), pgqPercentage : $("#3_pgqPercentage").val()
				, wpiPercentage : $("#3_wpiPercentage").val(), wsiPercentage : $("#3_wsiPercentage").val(), inbPercentage : $("#3_inbPercentage").val()},
				success: function(data){
					tableAgentSlab.clear().draw();
					tableAgentSlab.ajax.reload();
					if(data.indexOf("Already")!=-1){
						toastr.clear();toastr.warning(data);
					}
					else{
						toastr.clear();toastr.info(data);
						$("#3_odMinSlab,#3_odMaxSlab,#3_pgqPercentage,#3_wpiPercentage,#3_wsiPercentage,#3_inbPercentage").val('');
					}
					hideLoader();
				},
				error: function(e){
					hideLoader();
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
		});
	}else if(mType=="5"){
		$.ajax({
			type: "POST",
			url: "/rpa/insertTlSlab",
			timeout : 100000,
			data: { perc_70To85  :$("#5_70To85").val(), perc_85To90:$("#5_85To90").val(), perc_90To95 : $("#5_90To95").val()
				, perc_95To100 : $("#5_95To100").val(), perc_100AndAbove : $("#5_100andAbove").val()},
				success: function(data){
					tableAgentSlab.clear().draw();
					tableAgentSlab.ajax.reload();
					if(data.indexOf("Already")!=-1){
						toastr.clear();toastr.warning(data);
					}
					else{
						toastr.clear();toastr.info(data);
						$("#5_70To85,#5_85To90,#5_90To95,#5_95To100,#5_100andAbove").val('');
					}
					hideLoader();
				},
				error: function(e){
					hideLoader();
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
		});
}else if(mType=="6"){
	$.ajax({
		type: "POST",
		url: "/rpa/insertInHouseTlSlab",
		timeout : 100000,
		data: { odMinSlab  :$("#6_odMinSlab").val(), odMaxSlab:$("#6_odMaxSlab").val(), target : $("#6_target").val()
			, incentivePercentage : $("#6_incentivePercentage").val()},
			success: function(data){
				tableTlInHouseSlab.clear().draw();
				tableTlInHouseSlab.ajax.reload();
				if(data.indexOf("Already")!=-1){
					toastr.clear();toastr.warning(data);
				}
				else{
					toastr.clear();toastr.info(data);
					$("#6_odMinSlab,#6_odMaxSlab,#6_target,#6_incentivePercentage").val('');
				}
				hideLoader();
			},
			error: function(e){
				hideLoader();
				toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
			}
	});
}
	
			});
	
	$("#agentSlabUpdate").click(function(){
		$.ajax({
			type: "POST",
			url: "/rpa/updateAgentSlab",
			timeout : 100000,
			data: { agentSlabOdMin_edit :$("#agentSlabOdMin_edit").val(), agentSlabOdMax_edit :$("#agentSlabOdMax_edit").val(),agentSlabPQG_edit :$("#agentSlabPQG_edit").val()
				, agentSlabWPI_edit :$("#agentSlabWPI_edit").val(),agentSlabWSI_edit :$("#agentSlabWSI_edit").val(), agentSlabINB_edit :$("#agentSlabINB_edit").val()},

				success: function(data){
					tableAgentSlab.clear().draw();
					tableAgentSlab.ajax.reload();
					toastr.clear();toastr.info(data);
					hideLoader();
				},
				error: function(e){
					hideLoader();
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
		});
	});
	
	$("#tlSlabUpdate").click(function(){
		
		if($("#masterType").val()=="5"){
		$.ajax({
			type: "POST",
			url: "/rpa/updateTlSlab",
			timeout : 100000,
			data: { perc_70To85 :$("#70to85_edit").val(), perc_85To90 :$("#85to90_edit").val(),perc_90To95 :$("#90to95_edit").val()
				, perc_95To100 :$("#95to100_edit").val(),perc_100AndAbove :$("#100AndAbove_edit").val(), tlSlabId : $("#tlSlabId").val()},

				success: function(data){
					tableTlSlab.clear().draw();
					tableTlSlab.ajax.reload();
					toastr.clear();toastr.info(data);
					hideLoader();
				},
				error: function(e){
					hideLoader();
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
		});
	}else{
		$.ajax({
			type: "POST",
			url: "/rpa/updatePlSlab",
			timeout : 100000,
			data: { perc_70To85 :$("#70to85_edit").val(), perc_85To90 :$("#85to90_edit").val(),perc_90To95 :$("#90to95_edit").val()
				, perc_95To100 :$("#95to100_edit").val(),perc_100AndAbove :$("#100AndAbove_edit").val(), tlSlabId : $("#tlSlabId").val()},

				success: function(data){
					tablePlSlab.clear().draw();
					tablePlSlab.ajax.reload();
					toastr.clear();toastr.info(data);
					hideLoader();
				},
				error: function(e){
					hideLoader();
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
		});
	}
	
	});
	
	$("#inHouseTlSlabUpdate").click(function(){
		$.ajax({
			type: "POST",
			url: "/rpa/updateInHouseTlSlab",
			timeout : 100000,
			data: { odMinSlab :$("#inHouseTlodMin_edit").val(), odMaxSlab :$("#inHouseTlodMax_edit").val(),target :$("#inHouseTarget_edit").val()
				, incentivePercentage :$("#inHouseTlIncentivePerc_edit").val(),inHouseTlSlabId: $("#inHouseTlSlabId").val()},

				success: function(data){
					tableTlInHouseSlab.clear().draw();
					tableTlInHouseSlab.ajax.reload();
					toastr.clear();toastr.info(data);
					hideLoader();
				},
				error: function(e){
					hideLoader();
					toastr.clear();toastr.error("Internal Server Error. Please contact the Admin.");
				}
		});
	});
});

function removeRow(id){
	$("#"+id).remove();
}
			
