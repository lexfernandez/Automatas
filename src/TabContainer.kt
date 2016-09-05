
import automata.IAutomata
import automata.State
import com.mxgraph.layout.mxFastOrganicLayout
import com.mxgraph.model.mxCell
import com.mxgraph.model.mxGeometry
import com.mxgraph.swing.handler.mxKeyboardHandler
import com.mxgraph.swing.handler.mxRubberband
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.swing.util.mxMorphing
import com.mxgraph.util.*
import com.mxgraph.view.mxGraph
import javafx.application.Platform
import javafx.embed.swing.SwingNode
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.layout.BorderPane
import javafx.stage.Screen
import java.awt.event.MouseAdapter
import java.awt.event.MouseWheelEvent
import java.beans.PropertyChangeSupport
import java.io.File
import java.util.*
import javax.swing.JOptionPane

/**
* Created by lex on 08-05-16.
*/

open class TabContainer: Tab {
    val graph = mxGraph()
    var automaton: IAutomata
    val defaultStyle: String = "shape=ellipse;fillColor=white;strokeColor=blue;defaultHotspot=1.0"
    var graphComponent:mxGraphComponent
    var modified =true
    protected var undoManager: mxUndoManager? = null
    protected var keyboardHandler: mxKeyboardHandler? = null
    private var  rubberband: mxRubberband? = null
    var changes: PropertyChangeSupport = PropertyChangeSupport(this)
    val vertexMenu:ContextMenu = ContextMenu()
    var bcontent:BorderPane
    var file:File? = null


    constructor(iautomaton: IAutomata, text: String? = "new tab",file: File?=null):super(text){
        automaton=iautomaton
        this.file = file

        // Creates the embeddable graph swing component
        graphComponent = mxGraphComponent(graph)

        //Allows vertices to have edges from them to themselves
        graph.isAllowLoops = true

        //Prevents edges from pointing to nothing
        graph.isAllowDanglingEdges = false

        graph.isCellsCloneable=false
        //Prevent edge labels from being dragged somewhere absurd
        graph.isEdgeLabelsMovable = false

        graph.isDisconnectOnMove = false

        graph.isAutoSizeCells=true

        graph.isCellsResizable=false

        graph.isCellsEditable=false

        graphComponent.isGridVisible=true

        // Do not change the scale and translation after files have been loaded
        graph.isResetViewOnRootChange = false

        drawAutomata(automaton)
        graph.ApplyLayout()
        installHandlers()

        // Updates the modified flag if the graph model changes
        graph.model.addListener(mxEvent.CHANGE, changeTracker)

        // Adds the command history to the model and view
        graph.model.addListener(mxEvent.UNDO, undoHandler)
        graph.view.addListener(mxEvent.UNDO, undoHandler)


        // Keeps the selection in sync with the command history
        val undoHandler = mxEventSource.mxIEventListener { source, evt ->
            val changes = (evt.getProperty("edit") as mxUndoableEdit).changes
            graph.selectionCells = graph.getSelectionCellsForChanges(changes)
        }

        undoManager?.addListener(mxEvent.UNDO, undoHandler)
        undoManager?.addListener(mxEvent.REDO, undoHandler)



        val setAsInitial:MenuItem = MenuItem("Set as Initial")
        val setAsFinal:MenuItem = MenuItem("Set as Final")
        val setAsInitialAndFinal:MenuItem = MenuItem("Set as Initial & Final")

        setAsInitial.onAction= EventHandler { actionEvent: ActionEvent ->
            println((actionEvent.source as MenuItem).parentPopup.x)
            println((actionEvent.source as MenuItem).parentPopup.y)
            //(actionEvent.source as mxCell).setVertexStyle(VertexType.INITIAL)
        }
        setAsFinal.onAction= EventHandler { actionEvent: ActionEvent ->
            setVertexStyle((actionEvent.source as mxCell),VertexType.FINAL)
        }
        setAsInitialAndFinal.onAction= EventHandler { actionEvent: ActionEvent ->
            setVertexStyle((actionEvent.source as mxCell),VertexType.INITIAL_FINAL)
        }
        vertexMenu.items.addAll(setAsInitial,setAsFinal,setAsInitialAndFinal)

        bcontent =BorderPane(SwingNode().apply {
            //Sets the graph as the content of the swing node
            content = graphComponent
        })

        this.contextMenu=vertexMenu


        this.content= bcontent

    }

    private fun mxCell.toggleType() {
        val style = graph.getCellStyle(this)
        val ss="${style["strokeColor"]}${style["shape"]}"
        when(ss){
            "blueellipse" -> {
                setVertexStyle(this,VertexType.INITIAL)
            }
            "redellipse" -> {
                setVertexStyle(this,VertexType.FINAL)
            }
            "greendoubleEllipse" -> {
                setVertexStyle(this,VertexType.INITIAL_FINAL)
            }
            "reddoubleEllipse" -> {
                setVertexStyle(this,VertexType.NORMAL)
            }
            else -> {
                setVertexStyle(this,VertexType.NORMAL)
            }
        }
    }

    private fun mxCell.resize(){
        if(this.isVertex){
            if(this.value.toString().length>5){
                //cell = graph.updateCellSize(cell) as mxCell

                val bounds = graph.view.getState(this).labelBounds
                val g = this.geometry.clone() as mxGeometry

                if(bounds.width>g.width)
                    g.width=bounds.width+10
                g.height=bounds.width+10

                graph.update {
                    graph.cellsResized(arrayOf(this), arrayOf( mxRectangle(g)))
                }

            }
        }
    }

    fun setVertexStyle(cell:mxCell,type: VertexType){
        try {
            if(cell.isVertex){
                graph.update {
                    when(type){
                        VertexType.INITIAL -> {
                            cell.style=defaultStyle.replace("strokeColor=blue","strokeColor=red")
                            automaton.removeFinalState(cell.value.toString())
                            automaton.setInitialState(cell.value.toString())
                        }
                        VertexType.FINAL -> {
                            cell.style=defaultStyle.replace("strokeColor=blue","strokeColor=green").replace("shape=ellipse","shape=doubleEllipse")
                            automaton.setFinalState(cell.value.toString())
                        }
                        VertexType.INITIAL_FINAL -> {
                            cell.style=defaultStyle.replace("strokeColor=blue","strokeColor=red").replace("shape=ellipse","shape=doubleEllipse")
                            automaton.setInitialState(cell.value.toString())
                            automaton.setFinalState(cell.value.toString())
                        }
                        VertexType.NORMAL -> {
                            cell.style=defaultStyle
                            automaton.setInitialState(null)
                            automaton.removeFinalState(cell.value.toString())
                        }
                    }
                }
            }
        }catch (e:Exception){
            JOptionPane.showMessageDialog(null, e.message, "Error", JOptionPane.ERROR_MESSAGE)
        }

    }

    private fun mxGraph.ApplyLayout(){

        model.beginUpdate()
        try {
            val layout = mxFastOrganicLayout(graph)
            // set some properties
            layout.forceConstant = 200.0 // the higher, the more separated
            layout.isDisableEdgeStyle = false // true transforms the edges and makes them direct lines
            layout.isUseInputOrigin=true
            // layout graph
            layout.execute(graph.defaultParent)
        }
        finally {
            val morph = mxMorphing(graphComponent)
            morph.addListener(mxEvent.DONE, { source, evt ->
                graph.model.endUpdate()
                modified=false
            })

            morph.startAnimation()
        }


    }

    fun  mxGraph.update(block: () -> Any) {
        model.beginUpdate()
        try {
            block()
        }
        finally {
            model.endUpdate()
        }
    }

    private fun drawAutomata(automata: IAutomata){
        var x=10.0
        var y=10.0
        var maxGeometryHeight=0.0
        val hEdgePadding = 100.0
        val VEdgePadding = 100.0
        val cells: HashMap<String, mxCell> = hashMapOf()

        for (state in automata.states){
            val cell = graph.insertVertex(graph.defaultParent, null,state.value, x, y, 40.0, 40.0, defaultStyle) as mxCell
            val initial=automata.getInitialState()
            if(initial!=null){
                val isInitial=initial.value==state.value
                val isFinal=automata.isFinal(state.value)
                if(isInitial and isFinal){
                    setVertexStyle(cell,VertexType.INITIAL_FINAL)
                }else if(isInitial){
                    setVertexStyle(cell,VertexType.INITIAL)
                }else if(isFinal){
                    setVertexStyle(cell,VertexType.FINAL)
                }
            }
            cell.resize()
            cells[state.value] = cell

            if(Screen.getPrimary().bounds.width-10.0>=(x+cell.geometry.width+hEdgePadding)){
                x+=cell.geometry.width+hEdgePadding
                if(cell.geometry.height>maxGeometryHeight)
                    maxGeometryHeight=cell.geometry.height

            }else{
                y+=maxGeometryHeight+VEdgePadding
                x=10.0
            }
        }

        graph.update {
            for(state in automata.states){
                for (transition in state.getTransitions()){
                    val source = cells[transition.source.value]
                    val target = cells[transition.target.value]
                    graph.insertEdge(graph.defaultParent, null,transition.symbol,source,target)
                }
            }
        }

        graphComponent.refresh()
    }

    protected var undoHandler: mxEventSource.mxIEventListener = mxEventSource.mxIEventListener { source, evt ->
        undoManager?.undoableEditHappened(evt.getProperty("edit") as mxUndoableEdit)
    }

    protected var changeTracker: mxEventSource.mxIEventListener = mxEventSource.mxIEventListener({ source, evt ->
        setModifiedProperty(true)
        println("Was Modified")
    })

    protected fun createUndoManager(): mxUndoManager {
        return mxUndoManager()
    }

    protected fun mouseWheelMoved(e: MouseWheelEvent) {
        if (e.wheelRotation < 0) {
            graphComponent.zoomIn()
        } else {
            graphComponent.zoomOut()
        }



//        status(mxResources.get("scale") + ": "
//                + (100 * graphComponent.graph.view.scale) as Int
//                + "%")
    }



    protected fun installHandlers() {
        rubberband = mxRubberband(graphComponent)
        //keyboardHandler = EditorKeyboardHandler(graphComponent)

        graphComponent.connectionHandler.addListener(mxEvent.CONNECT) { sender, evt ->
            val cell = evt.getProperty("cell") as mxCell
            if(cell.isEdge){
                var s: String

                do {
                    s = JOptionPane.showInputDialog(
                            null,
                            "Enter edge name:\n" + "e.g.\"1\" or \"a\"",
                            "Edge Name",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            "0") as String
                } while (s.isNullOrEmpty())
                try {
                    automaton.addTransition(s.first().toChar(),cell.source.value.toString(),cell.target.value.toString())
                    cell.value = s
                    cell.style = "rounded=true;arcSize=30;edgeStyle=orthogonalEdgeStyle;portConstraint=north"
                }catch (e:NullPointerException){
                    if(cell.isEdge){
                        graph.model.remove(cell)
                    }
                }
                catch (e: Exception){
                    JOptionPane.showMessageDialog(null, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                    graph.model.remove(cell)
                }
            }else{
                println("Got a cell: ${cell.value}")
            }

        }



        graphComponent.graphControl.addMouseListener(object : MouseAdapter() {

            override fun mouseReleased(e: java.awt.event.MouseEvent) {

                val cell = graphComponent.getCellAt(e.x, e.y)

                if (cell is mxCell) {
                    if(e.button==3){ //Rigth Click
                        //vertexMenu.show(bcontent, e.xOnScreen.toDouble(),e.yOnScreen.toDouble())
                    }else if (e.clickCount == 2) {
                        println("vertex clicked")
                        cell.toggleType()
                        graphComponent.refresh()
                    }
                }else{
                    if (e.clickCount == 2) {
                        var s: String?

                        do {
                            s = JOptionPane.showInputDialog(
                                    null,
                                    "Enter vertex name:\n" + "e.g.\"1\" or \"a\"",
                                    "Vertex Name",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    null,
                                    null) as String?
                        } while (s.isNullOrEmpty() || s.equals("new vertex") )
                        val cell = graph.insertVertex(graph.defaultParent, null, s?.toLowerCase(), e.x.toDouble(), e.y.toDouble(), 40.0, 40.0, defaultStyle)
                        try {
                            cell as mxCell
                            automaton.addState(State(cell.value.toString()))
                            cell.resize()
                        }catch (e: Exception){
                            JOptionPane.showMessageDialog(null, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                            graph.model.remove(cell)
                        }
                    }
                }
            }

            override fun mousePressed(e: java.awt.event.MouseEvent) {
                val cell = graphComponent.getCellAt(e.x, e.y)
                println("Mouse click in graph component")
                if (cell != null) {
                    println("cell=" + graph.getLabel(cell))
                }
            }
        })

    }

//    protected fun mouseLocationChanged(e: MouseEvent) {
//        status(e.x + ", " + e.y)
//    }

    fun setModifiedProperty(modified: Boolean) {
        val oldValue = this.modified
        this.modified = modified

        //changes.firePropertyChange("modified", oldValue, modified)

        if (oldValue != modified) {
            updateTitle()
        }
    }


    fun isModified(): Boolean {
        return modified
    }

//    fun status(msg: String) {
//        statusBar.setText(msg)
//    }

    fun updateTitle() {
        Platform.runLater({
            this.text+="*"
        })

    }


}