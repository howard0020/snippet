$(document).ready(function() {
	
	// Expand Panel
	$("#slide_open").click(function(){
		$("#user_login_wrapper").slideDown("slow");
	
	});	
	
	// Collapse Panel
	$("#slide_close").click(function(){
		$("#user_login_wrapper").slideUp("slow");	
	});	
		
});