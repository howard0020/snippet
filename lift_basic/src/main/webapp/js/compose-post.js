(function(window, $) {
  window.fmpwizard = {
    views: {
      DynamicFields: function(){
        var self = this;
        self.addFields = function() {
          $('#btnAddHTML').click(function() {
          	var $ = Aloha.jQuery;
            var num     = editorCount++; // how many "duplicatable" input fields we currently have
            var newNum  = new Number(num + 1);      // the numeric ID of the new input field being added
            // create the new element via clone(), and manipulate it's ID using newNum value
            // var newElem = $('#input' + num).clone().attr('id', 'input' + newNum);
            var newField = $('<div class="sortable-item">' +
			            		'<span class="handle">Drag</span>' +
			            		'<button class="field-delete">Delete</button>' +
			            		'<div class="field-content WYSIWYG-fields" contenteditable="true">' +
			            		'</div>' +
			            	'</div>');
			newField.children(".field-delete").click(function(){
			            		deleteParentElement(this);
			            	});
            newField.attr('id','Text' + newNum);
            $('#Text1').parent().append(newField);
            
            Aloha.jQuery('.WYSIWYG-fields').mahalo();
        	Aloha.jQuery('.WYSIWYG-fields').aloha();
           
          });
          
            $('#btnAddCode').click(function() {
 
            var num     = editorCount++; // how many "duplicatable" input fields we currently have
            var newNum  = new Number(num + 1);      // the numeric ID of the new input field being added
           	//create the html to add
            var newField = $('<div class="field-content code-block-fields sortable-item">' +
			            		'<span class="handle">Drag</span>' +
			            		'<button class="field-delete">Delete</button>' +
			            		'<textarea></textarea>' +
		            		'</div>');
		    newField.children(".field-delete").click(function(){
			            		deleteParentElement(this);
			            	});
            var editorDiv = newField.attr('id','Text' + newNum);
            editorDiv.children("textarea:first").attr('id','textArea' + newNum);
            $('#Text1').parent().append(newField);
            var newTextArea = document.getElementById('textArea' + newNum);
            var newEditor = CodeMirror.fromTextArea(newTextArea, {
    		  lineNumbers: true,
			  value: "",
			  mode:  "text/x-scala",
			  lineWrapping: "wrap",
			  onCursorActivity: function() {
   			 	   		editor.matchHighlight("CodeMirror-matchhighlight");
   			 	   }
			});
			window.codeEditors['Text' + newNum] = newEditor;
            // manipulate the name/id values of the input inside the new element
           // newElem.children('textarea').attr('class', 'emailContent').attr('id', 'reminderText' + newNum);
            //newElem.children('input').attr('class', 'schedule').attr('id', 'runReminderInDays' + newNum);
            // insert the new element after the last "duplicatable" input field
            
            // enable the "remove" button
             $('#btnDel').removeAttr('disabled','');
          });
        };
        
        //remove the last editor
        self.removeFields = function() {
          $('#btnDel').click(function() {
            var num = $('.field-content').length; // how many "duplicatable" input fields we currently have
            $('#Text' + num).remove();     // remove the last element
            
            // if only one element remains, disable the "remove" button
            if (num-1 == 1)
           	 $('#btnDel').attr('disabled','disabled');
          });
        };
        //collect form data and submit to server
        self.collectFormData = function(fnName) {
          var check = $("[name='title']").validator();
          if(check.data("validator").checkValidity() == true){
          	console.log("title is true" );
	          for(var keys in window.codeEditors)
	          {
	          	window.codeEditors[keys].save();
	          }
	          var formData = new Array();
	          $(".field-content").each(function() {
	          	if($(this).is('.title-field')){
	          		formData.push(["titleField",$(this).val()])
	          	}else if($(this).is('.code-block-fields')){
	            	var editorObj = window.codeEditors[$(this).attr('id')];
	            	formData.push(["codeBlock",editorObj.getOption("mode"),$(this).children("textarea:first").val()]);
	            }else{
	            	formData.push(["htmlBlock",$(this).html()]);	
	            }
	          });
	          console.log(formData);
	          fnName(formData);
        	}
        };
      },
      SomeOtherNameSpace: function(){
        var self = this;
        self.funcNamehere = function() {
          $("#id").val();
        };
      }
    }
  };
})(this, jQuery);

/* You call functions from your html file like this:

<script type="text/javascript">
  $(document).ready(function() {
    window.dyTable = new window.fmpwizard.views.DynamicFields();
    window.dyTable.addFields();
    window.dyTable.removeFields();
  });
</script>
 */