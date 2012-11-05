function initCodeBlock(){
	$('.code-block-fields').each(function() {
		CodeMirror.modeURL = "../codemirror/mode/%N/%N.js";
	    var $this = $(this),
	        $code = $this.html(),
	        $unescaped = $('<div/>').html($code).text();
	   
	    var thisEditor = CodeMirror.fromTextArea(this, {
	        mode: $this.attr('mode'),
	        lineNumbers: true,
	        readOnly: true,
	        lineWrapping: "wrap",
	        onCursorActivity: function() {
		 	   		thisEditor.matchHighlight("CodeMirror-matchhighlight");
		 	   }
	    });
	    CodeMirror.autoLoadMode(thisEditor, window.codeMirrorModes[$this.attr('mode')][1]);
	    $('pre.CodeMirror-cursor').remove();
	});
};