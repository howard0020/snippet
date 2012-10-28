	function btnWithDefaultText(buttonInfo) {
		var btnID = buttonInfo.buttonID;
		var defaultText = buttonInfo.defaultText;
		var enterLink = buttonInfo.enterLink;
		
		var textColor = buttonInfo.color;
		var enterKeyHandler = buttonInfo.enterKeyHandler;
		
		var btn = $("#" + btnID);
		btn.val(defaultText);
		var origColor = btn.css("color");
		
		if (textColor !== undefined)
			btn.css("color", textColor)
	
		btn.focus(function(e) {
			if ($(this).val() == defaultText) {
				btn.css("color", origColor);
				btn.val("");
			}
		});
		
		btn.blur(function(e) {
			if ($(this).val() == "") {
				btn.css("color", textColor);
				btn.val(defaultText)
			}
		});
		
		btn.keydown(function(e) {
			var searchString = $(this).val();
			if (e.which == 13 && searchString.length != 0) { // key code 13 is enter key
				if (enterKeyHandler !== undefined) {
					enterKeyHandler(this);
				} else {
					window.location.href = enterLink + "/" + searchString;
				}
			}
		});
}