(function(window, $) {
  window.fmpwizard = {
    views: {
      DynamicFields: function(){
        var self = this;
        self.addFields = function() {
          $('#btnAddHTML').click(function() {
          	var $ = Aloha.jQuery;
            var num     = $('.field-content').length; // how many "duplicatable" input fields we currently have
            var newNum  = new Number(num + 1);      // the numeric ID of the new input field being added
            // create the new element via clone(), and manipulate it's ID using newNum value
            // var newElem = $('#input' + num).clone().attr('id', 'input' + newNum);
            var newField = $('<br><div class="field-content WYSIWYG-fields" contenteditable="true"></div>');
            newField.next('.field-content').attr('id','Text' + newNum);
            $('#Text' + num).after(newField);
            
            Aloha.jQuery('.WYSIWYG-fields').mahalo();
        	Aloha.jQuery('.WYSIWYG-fields').aloha();
            // manipulate the name/id values of the input inside the new element
           // newElem.children('textarea').attr('class', 'emailContent').attr('id', 'reminderText' + newNum);
            //newElem.children('input').attr('class', 'schedule').attr('id', 'runReminderInDays' + newNum);
            // insert the new element after the last "duplicatable" input field
            
            // enable the "remove" button
            //$('#btnDel').removeAttr('disabled','');
          });
          
            $('#btnAddCode').click(function() {
 
            var num     = $('.field-content').length; // how many "duplicatable" input fields we currently have
            var newNum  = new Number(num + 1);      // the numeric ID of the new input field being added
            // create the new element via clone(), and manipulate it's ID using newNum value
            // var newElem = $('#input' + num).clone().attr('id', 'input' + newNum);
            var newField = $('<br><textarea class="field-content code-block-fields"></textarea>');
            newField.next('.field-content').attr('id','Text' + newNum);
            $('#Text' + num).after(newField);
            var newTextArea = document.getElementById('Text' + newNum);
            var newEditor = CodeMirror.fromTextArea(newTextArea, {
    		  lineNumbers: true,
			  value: "",
			  mode:  "text/x-scala",
			  lineWrapping: "wrap",
			  onCursorActivity: function() {
   			 	   		editor.matchHighlight("CodeMirror-matchhighlight");
   			 	   }
			});
			window.codeEditors.push(newEditor);
            // manipulate the name/id values of the input inside the new element
           // newElem.children('textarea').attr('class', 'emailContent').attr('id', 'reminderText' + newNum);
            //newElem.children('input').attr('class', 'schedule').attr('id', 'runReminderInDays' + newNum);
            // insert the new element after the last "duplicatable" input field
            
            // enable the "remove" button
            //$('#btnDel').removeAttr('disabled','');
          });
        };
        self.removeFields = function() {
          $('#btnDel').click(function() {
            var num = $('.clonedInput').length; // how many "duplicatable" input fields we currently have
            $('#input' + num).remove();     // remove the last element
            // enable the "add" button
            $('#btnAdd').removeAttr('disabled','');
            // if only one element remains, disable the "remove" button
            if (num-1 == 1)
              $('#btnDel').attr('disabled','disabled');
          });
        };
        self.collectFormData = function(fnName) {
          for(var i = 0; i < window.codeEditors.length; i++)
          {
          	window.codeEditors[i].save();
          }
          var formData = new Array();
          $(".field-content").each(function() {
            if($(this).is('.code-block-fields')){
            	console.log("code editor");
            	formData.push($(this).val());
            }else{
            	console.log("html editor");
            	formData.push($(this).html());	
            }
          });
          console.log(formData)
          fnName(formData);
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