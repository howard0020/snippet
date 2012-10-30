$(document)
		.ready(
				function() {
					$("#tblofcontent")
							.dynatree(
									{
										/*
										 * initAjax : { url :
										 * "http://localhost:8080/ws/tblofcontent" },
										 *
										 * children : get_toc(),
										*/
										onLazyRead : function(node) {
											// Mockup a slow reqeuest ...
											node
													.appendAjax({
														url : "http://localhost:8080/ws/tblofcontent/posts",
														debugLazyDelay : 750
													// don't do thi in
													// production code
													});
										},
										onActivate : function(node) {
											//alert(
											//		node.data.title + "("
											//			+ node.data.key
											//			+ ")");
										},
										onDeactivate : function(node) {
											//alert("deactivate - ");
										},
										onLazyRead : function(node) {
											node
													.appendAjax({
														url : "http://localhost:8080/ws/tblofcontent/posts",
													});
										},
										dnd : {
											autoExpandMS : 1000,
											preventVoidMoves : true, // Prevent
											// dropping
											// nodes 'before
											// self', etc.
											onDragStart : function(node) {
												return isToCEditable();
											},
											onDragEnter : function(node,
													sourceNode) {
												/**
												 * sourceNode may be null for
												 * non-dynatree droppables.
												 * Return false to disallow
												 * dropping on node. In this
												 * case onDragOver and
												 * onDragLeave are not called.
												 * Return 'over', 'before, or
												 * 'after' to force a hitMode.
												 * Any other return value will
												 * calc the hitMode from the
												 * cursor position.
												 */
												logMsg(
														"tree.onDragEnter(%o, %o)",
														node, sourceNode);
												// if(node.data.isFolder)
												// return false;
												return true;
												// return "over";
											},
											onDragOver : function(node,
													sourceNode, hitMode) {
												/**
												 * Return false to disallow
												 * dropping this node.
												 * 
												 */
												// if(node.data.isFolder){
												// var dd =
												// $.ui.ddmanager.current;
												// dd.cancel();
												// alert("folder");
												// }
												logMsg(
														"tree.onDragOver(%o, %o, %o)",
														node, sourceNode,
														hitMode);
												if (node
														.isDescendantOf(sourceNode))
													return false;

											},
											onDrop : function(node, sourceNode,
													hitMode, ui, draggable) {
												/**
												 * This function MUST be defined
												 * to enable dropping of items
												 * on the tree. sourceNode may
												 * be null, if it is a
												 * non-Dynatree droppable.
												 */
												logMsg("tree.onDrop(%o, %o)",
														node, sourceNode);
												var copynode;
												if (sourceNode) {
													sourceNode.move(node,
															hitMode);
												} else {
													var span = ui.helper
															.find("span");
													var post_title = span
															.html();
													var post_id = span
															.attr("data");
													copynode = {
														title : post_title,
														key : post_id
													};

													if (hitMode == "over") {
														// Append as child node
														node.addChild(copynode);
														// expand the drop
														// target
														node.expand(true);
													} else if (hitMode == "before") {
														// Add before this, i.e.
														// as
														// child of current
														// parent
														node.parent.addChild(
																copynode, node);
													} else if (hitMode == "after") {
														// Add after this, i.e.
														// as child
														// of current parent
														node.parent
																.addChild(
																		copynode,
																		node
																				.getNextSibling());
													}
												}
												console.log("table of content has been updated on the client");
												clientToCUpdateListner();
											},
											onDragLeave : function(node,
													sourceNode) {
												/**
												 * Always called if onDragEnter
												 * was called.
												 */
												logMsg(
														"tree.onDragLeave(%o, %o)",
														node, sourceNode);
											}
										}
									});
				})