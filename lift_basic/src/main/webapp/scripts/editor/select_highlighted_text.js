function selectedText(){
var input = document.getElementById("editor");
var startPos = input.selectionStart;
alert(startPos);
var endPos = input.selectionEnd;
alert(endPos);
var doc = document.selection;

if(doc && doc.createRange().text.length != 0){
alert(doc.createRange().text);
}else if (!doc && input.value.substring(startPos,endPos).length != 0){
alert(input.value.substring(startPos,endPos))	
}
}