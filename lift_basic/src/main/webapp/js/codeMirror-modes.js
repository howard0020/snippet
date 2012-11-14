window.codeMirrorModes = new Object()
//key:MIME types  => ["display text","file name"]

	//c like modes
	codeMirrorModes["text/x-csrc"] = ["C","clike"];
	codeMirrorModes["text/x-c++src"] = ["C++","clike"];
	codeMirrorModes["text/x-java"] = ["Java","clike"];
	codeMirrorModes["text/x-csharp"] = ["C#","clike"];
	codeMirrorModes["text/x-scala"] = ["Scala","clike"];
	
	codeMirrorModes["text/x-clojure"] = ["Clojure","clojure"];
	codeMirrorModes["text/x-coffeescript"] = ["Coffee Script","coffeescript"];
	codeMirrorModes["text/x-common-lisp"] = ["Common Lisp","commonlisp"];
	codeMirrorModes["text/x-ecl"] = ["ECL","ecl"];
	codeMirrorModes["text/x-erlang"] = ["Erlang","erlang"];
	codeMirrorModes["text/x-go"] = ["Go","go"];
	codeMirrorModes["text/x-groovy"] = ["Groovy","groovy"];
	codeMirrorModes["text/x-haskell"] = ["Haskell","haskell"];
	codeMirrorModes["text/x-haxe"] = ["Haxe","haxe"];
	
	//web like modes
	codeMirrorModes["application/x-aspx"] = ["ASP","htmlembedded"];
	codeMirrorModes["application/x-jsp"] = ["JSP","htmlembedded"];
	codeMirrorModes["text/html"] = ["HTML","htmlmixed"];
	codeMirrorModes["text/javascript"] = ["Javascript","javascript"];
	codeMirrorModes["text/x-perl"] = ["Perl","perl"];
	codeMirrorModes["application/x-httpd-php"] = ["PHP","php"];
	
	//sql like modes
	codeMirrorModes["text/x-mysql"] = ["MySQL","mysql"];
	codeMirrorModes["text/x-plsql"] = ["PL/SQL","plsql"];
	codeMirrorModes["application/x-sparql-query"] = ["SPARQL","sparql"];
	
	codeMirrorModes["text/x-less"] = ["LESS","less"];
	codeMirrorModes["text/x-lua"] = ["Lua","lua"];
	codeMirrorModes["text/x-markdown"] = ["Markdown","markdown"];

	codeMirrorModes["text/n-triples"] = ["NTriples","ntriples"];
	codeMirrorModes["text/x-ocaml"] = ["OCaml","ocaml"];
	codeMirrorModes["text/x-pascal"] = ["Pascal","pascal"];
	codeMirrorModes["text/x-pig"] = ["Pig","pig"];
	
	codeMirrorModes["text/x-python"] = ["Python","python"];
	codeMirrorModes["text/x-rsrc"] = ["R","r"];
	codeMirrorModes["text/x-ruby"] = ["Ruby","ruby"];
	codeMirrorModes["text/x-rustsrc"] = ["Rust","rust"];
	codeMirrorModes["text/x-scheme"] = ["Scheme","scheme"];
	codeMirrorModes["text/x-sh"] = ["Shell","sh"];
	codeMirrorModes["application/sieve"] = ["Sieve","sieve"];
	codeMirrorModes["text/x-stsrc"] = ["Smalltalk","smalltalk"];
	codeMirrorModes["text/x-smarty"] = ["Smarty","smarty"];
	
	//sTex LaTex
	codeMirrorModes["text/x-stex"] = ["sTex/LaTeX","stex"];
	
	//vb.net
	codeMirrorModes["text/x-vb"] = ["VB.NET","vb"];
	
	codeMirrorModes["text/vbscript"] = ["VBScript","vbscript"];
	codeMirrorModes["text/velocity"] = ["Velocity","velocity"];
	codeMirrorModes["application/xml"] = ["XML","xml"];
	codeMirrorModes["application/xquery"] = ["XQuery","xquery"];
	codeMirrorModes["text/x-yaml"] = ["YAML","yaml"];
        
    window.initModeOpts = function(selectId) {
    	$("#btnAddCode").attr("disabled",true);
    	
    	$("#"+selectId)
    			.append($("<option></option>")
    			.attr("value","empty")
    			.text("select a mode"));
    	$("#"+selectId).change(function() {
    		if($("#newModeSelect option:selected").val() == "empty"){
    			$("#btnAddCode").attr("disabled",true);
    		}else{
    			$("#btnAddCode").attr("disabled",false);
    		}
    	});
    	
    	for(var key in codeMirrorModes)
    	{
    		$("#"+selectId)
    			.append($("<option></option>")
    			.attr("value",key)
    			.text(codeMirrorModes[key][0]));
    	}
    };	
    
    window.deleteParentElement = function(element) {
		element.parentNode.parentNode.removeChild(element.parentNode);
	};