/**
 * Created by lex on 08-05-16.
 */


/**
 * Copyright (c) 2008, Gaudenz Alder
 */
package com.mxgraph.examples.swing.editor;

import com.mxgraph.swing.handler.mxKeyboardHandler
import com.mxgraph.swing.mxGraphComponent
import javax.swing.ActionMap
import javax.swing.InputMap
import javax.swing.JComponent
import javax.swing.KeyStroke

/**
 * @author Administrator
 *
 */ class EditorKeyboardHandler : mxKeyboardHandler
{

    /**
     *
     * @param graphComponent
     */
    constructor(graphComponent: mxGraphComponent):super(graphComponent)
    {

    }

    /**
     * Return JTree's input map.
     */
    protected override fun getInputMap(condition:Int): InputMap
    {
        var map: InputMap = super.getInputMap(condition)

        if (condition == JComponent.WHEN_FOCUSED && map != null)
        {
            map.put(KeyStroke.getKeyStroke("control S"), "save");
            map.put(KeyStroke.getKeyStroke("control shift S"), "saveAs");
            map.put(KeyStroke.getKeyStroke("control N"), "new");
            map.put(KeyStroke.getKeyStroke("control O"), "open");

            map.put(KeyStroke.getKeyStroke("control Z"), "undo");
            map.put(KeyStroke.getKeyStroke("control Y"), "redo");
            map
                    .put(KeyStroke.getKeyStroke("control shift V"),
                            "selectVertices");
            map.put(KeyStroke.getKeyStroke("control shift E"), "selectEdges");
        }

        return map;
    }

    /**
     * Return the mapping between JTree's input map and JGraph's actions.
     */
    protected override fun createActionMap():ActionMap
    {
        var map:ActionMap = super.createActionMap();

//        map.put("save", EditorActions.SaveAction(false));
//        map.put("saveAs", EditorActions.SaveAction(true));
//        map.put("new", EditorActions.NewAction());
//        map.put("open", EditorActions.OpenAction());
//        map.put("undo", EditorActions.HistoryAction(true));
//        map.put("redo", EditorActions.HistoryAction(false));
//        map.put("selectVertices", mxGraphActions.getSelectVerticesAction());
//        map.put("selectEdges", mxGraphActions.getSelectEdgesAction());

        return map;
    }

}
