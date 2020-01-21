package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*

private val PARAMETER_MAP = mapOf(
    "ADD_LABEL" to "yfiles.graph.ILabelOwner",
    "ADJUST_GROUP_NODE_SIZE" to INODE,
    "BEGIN_EDGE_CREATION" to "yfiles.input.IPortCandidate",
    "CLOSE" to JS_VOID,
    "COLLAPSE_GROUP" to INODE,
    "COPY" to JS_VOID,
    "CUT" to JS_VOID,
    "DECREASE_ZOOM" to JS_DOUBLE,
    "DELETE" to JS_VOID,
    "DESELECT_ALL" to JS_VOID,
    "DESELECT_ITEM" to IMODEL_ITEM,
    "DUPLICATE" to JS_VOID,
    "EDIT_LABEL" to ILABEL,

    "ENTER_GROUP" to INODE,
    "EXIT_GROUP" to JS_VOID,
    "EXPAND_GROUP" to INODE,

    "EXTEND_SELECTION_DOWN" to JS_VOID,
    "EXTEND_SELECTION_LEFT" to JS_VOID,
    "EXTEND_SELECTION_RIGHT" to JS_VOID,
    "EXTEND_SELECTION_UP" to JS_VOID,

    "FIT_CONTENT" to JS_VOID,
    "FIT_GRAPH_BOUNDS" to "yfiles.geometry.Insets",
    "GROUP_SELECTION" to JS_VOID,
    "HELP" to JS_VOID,
    "INCREASE_ZOOM" to JS_DOUBLE,
    "LOWER" to IMODEL_ITEM,

    "MOVE_DOWN" to JS_VOID,
    "MOVE_FOCUS_BACK" to JS_VOID,
    "MOVE_FOCUS_DOWN" to JS_VOID,
    "MOVE_FOCUS_FORWARD" to JS_VOID,
    "MOVE_FOCUS_PAGE_DOWN" to JS_VOID,
    "MOVE_FOCUS_PAGE_UP" to JS_VOID,
    "MOVE_FOCUS_UP" to JS_VOID,
    "MOVE_LEFT" to JS_VOID,
    "MOVE_RIGHT" to JS_VOID,
    "MOVE_TO_PAGE_DOWN" to JS_VOID,
    "MOVE_TO_PAGE_UP" to JS_VOID,
    "MOVE_UP" to JS_VOID,

    "NEW" to JS_VOID,
    "OPEN" to JS_VOID,
    "PASTE" to "yfiles.geometry.IPoint",
    "PRINT" to JS_VOID,
    "PRINT_PREVIEW" to JS_VOID,
    "PROPERTIES" to JS_VOID,
    "RAISE" to IMODEL_ITEM,
    "REDO" to JS_VOID,
    "REVERSE_EDGE" to IEDGE,
    "SAVE" to JS_VOID,

    "SCROLL_PAGE_DOWN" to JS_DOUBLE,
    "SCROLL_PAGE_LEFT" to JS_DOUBLE,
    "SCROLL_PAGE_RIGHT" to JS_DOUBLE,
    "SCROLL_PAGE_UP" to JS_DOUBLE,

    "SELECT_ALL" to JS_VOID,
    "SELECT_ITEM" to IMODEL_ITEM,

    "SELECT_TO_PAGE_DOWN" to JS_VOID,
    "SELECT_TO_PAGE_UP" to JS_VOID,

    "SET_CURRENT_ITEM" to IMODEL_ITEM,

    "TOGGLE_EXPANSION_STATE" to INODE,
    "TOGGLE_ITEM_SELECTION" to IMODEL_ITEM,

    "TO_BACK" to IMODEL_ITEM,
    "TO_FRONT" to IMODEL_ITEM,

    "UNDO" to JS_VOID,
    "UNGROUP_SELECTION" to JS_VOID,
    "UPDATE_CONTENT_RECT" to "yfiles.geometry.Rect",
    "ZOOM" to "yfiles.geometry.Rect",
    "ZOOM_TO_CURRENT_ITEM" to JS_VOID
)

internal fun applyCommandHacks(source: Source) {
    source.type("ICommand")
}
