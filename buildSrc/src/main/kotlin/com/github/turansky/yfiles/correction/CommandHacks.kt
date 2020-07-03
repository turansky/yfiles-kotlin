package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*

private const val JS_NOTHING = "Nothing"

private val COMMAND_ALIASES = setOf(
    "yfiles.input.CanExecuteCommandHandler",
    "yfiles.input.ExecuteCommandHandler"
)

private const val ICOMMAND = "yfiles.input.ICommand"

private val PARAMETER_MAP = mapOf(
    "ADD_LABEL" to "yfiles.graph.ILabelOwner",
    "ADJUST_GROUP_NODE_SIZE" to INODE,
    "BEGIN_EDGE_CREATION" to "yfiles.input.IPortCandidate",
    "CLOSE" to JS_NOTHING,
    "COLLAPSE_GROUP" to INODE,
    "COPY" to JS_NOTHING,
    "CUT" to JS_NOTHING,
    "DECREASE_ZOOM" to JS_DOUBLE,
    "DELETE" to JS_NOTHING,
    "DESELECT_ALL" to JS_NOTHING,
    "DESELECT_ITEM" to IMODEL_ITEM,
    "DUPLICATE" to JS_NOTHING,
    "EDIT_LABEL" to ILABEL,

    "ENTER_GROUP" to INODE,
    "EXIT_GROUP" to JS_NOTHING,
    "EXPAND_GROUP" to INODE,

    "EXTEND_SELECTION_DOWN" to JS_NOTHING,
    "EXTEND_SELECTION_LEFT" to JS_NOTHING,
    "EXTEND_SELECTION_RIGHT" to JS_NOTHING,
    "EXTEND_SELECTION_UP" to JS_NOTHING,

    "FIT_CONTENT" to JS_NOTHING,
    "FIT_GRAPH_BOUNDS" to "yfiles.geometry.Insets",
    "GROUP_SELECTION" to JS_NOTHING,
    "HELP" to JS_NOTHING,
    "INCREASE_ZOOM" to JS_DOUBLE,
    "LOWER" to IMODEL_ITEM,

    "MOVE_DOWN" to JS_NOTHING,
    "MOVE_FOCUS_BACK" to JS_NOTHING,
    "MOVE_FOCUS_DOWN" to JS_NOTHING,
    "MOVE_FOCUS_FORWARD" to JS_NOTHING,
    "MOVE_FOCUS_PAGE_DOWN" to JS_NOTHING,
    "MOVE_FOCUS_PAGE_UP" to JS_NOTHING,
    "MOVE_FOCUS_UP" to JS_NOTHING,
    "MOVE_LEFT" to JS_NOTHING,
    "MOVE_RIGHT" to JS_NOTHING,
    "MOVE_TO_PAGE_DOWN" to JS_NOTHING,
    "MOVE_TO_PAGE_UP" to JS_NOTHING,
    "MOVE_UP" to JS_NOTHING,

    "NEW" to JS_NOTHING,
    "OPEN" to JS_NOTHING,
    "PASTE" to "yfiles.geometry.IPoint",
    "PRINT" to JS_NOTHING,
    "PRINT_PREVIEW" to JS_NOTHING,
    "PROPERTIES" to JS_NOTHING,
    "RAISE" to IMODEL_ITEM,
    "REDO" to JS_NOTHING,
    "REVERSE_EDGE" to IEDGE,
    "SAVE" to JS_NOTHING,

    "SCROLL_PAGE_DOWN" to JS_DOUBLE,
    "SCROLL_PAGE_LEFT" to JS_DOUBLE,
    "SCROLL_PAGE_RIGHT" to JS_DOUBLE,
    "SCROLL_PAGE_UP" to JS_DOUBLE,

    "SELECT_ALL" to JS_NOTHING,
    "SELECT_ITEM" to IMODEL_ITEM,

    "SELECT_TO_PAGE_DOWN" to JS_NOTHING,
    "SELECT_TO_PAGE_UP" to JS_NOTHING,

    "SET_CURRENT_ITEM" to IMODEL_ITEM,

    "TOGGLE_EXPANSION_STATE" to INODE,
    "TOGGLE_ITEM_SELECTION" to IMODEL_ITEM,

    "TO_BACK" to IMODEL_ITEM,
    "TO_FRONT" to IMODEL_ITEM,

    "UNDO" to JS_NOTHING,
    "UNGROUP_SELECTION" to JS_NOTHING,
    "UPDATE_CONTENT_RECT" to "yfiles.geometry.Rect",
    "ZOOM" to JS_ANY,
    "ZOOM_TO_CURRENT_ITEM" to JS_NOTHING
)

internal fun applyCommandHacks(source: Source) {
    source.type("ICommand").apply {
        setSingleTypeParameter(name = "in T", bound = JS_ANY)

        flatMap(METHODS)
            .optFlatMap(PARAMETERS)
            .filter { it[NAME] == "parameter" }
            .forEach { it[TYPE] = "T" }

        flatMap(CONSTANTS)
            .forEach { it.addGeneric(PARAMETER_MAP.getValue(it[NAME])) }

        method("createCommand")[RETURNS].addGeneric("*")
    }

    COMMAND_ALIASES
        .map { source.functionSignature(it) }
        .forEach {
            it.setSingleTypeParameter(bound = JS_OBJECT)

            it.parameter("command").addGeneric("T")
            it.parameter("parameter").also {
                it[TYPE] = "T"
                it[MODIFIERS] = arrayOf(CANBENULL)
            }
        }

    source.type("KeyboardInputMode")
        .flatMap(METHODS)
        .filter { it[NAME].startsWith("add") }
        .onEach { it.setSingleTypeParameter(bound = JS_ANY) }
        .flatMap(PARAMETERS)
        .forEach {
            when {
                it[TYPE] == ICOMMAND -> it.addGeneric("T")
                it.opt(SIGNATURE) in COMMAND_ALIASES -> it[SIGNATURE] = it[SIGNATURE] + "<T>"
                it[NAME] in "commandParameter" -> it[TYPE] = "T"
            }
        }

    source.type("KeyboardInputMode")
        .method("removeCommand")
        .parameter("command")
        .addGeneric("*")

    source.type("KeyboardInputModeBinding")
        .property("command")
        .addGeneric("*")

    source.types(
        "GraphInputMode",
        "NavigationInputMode",
        "OverviewInputMode"
    ).map { it.property("availableCommands") }
        .forEach { it.replaceInType(">", "<*>>") }

    source.types(
        "GraphInputMode",
        "OverviewInputMode",
        "TableEditorInputMode"
    ).forEach {
        it.method("shouldInstallCommand")
            .firstParameter
            .addGeneric("*")
    }
}
