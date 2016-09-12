
import automata.IAutomata
import automata.PDA
import automata.Production
import automata.State
import com.mxgraph.layout.mxFastOrganicLayout
import com.mxgraph.model.mxCell
import com.mxgraph.model.mxGeometry
import com.mxgraph.model.mxGraphModel
import com.mxgraph.swing.handler.mxKeyboardHandler
import com.mxgraph.swing.handler.mxRubberband
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.swing.util.mxMorphing
import com.mxgraph.util.*
import com.mxgraph.view.mxEdgeStyle
import com.mxgraph.view.mxGraph
import com.mxgraph.view.mxStylesheet
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.embed.swing.SwingNode
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.util.Callback
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
    val automaton: IAutomata
    private val defaultStyle: String = "shape=ellipse;fillColor=white;strokeColor=blue;defaultHotspot=1.0"
    var graphComponent: mxGraphComponent
    var modified = true
    protected var undoManager: mxUndoManager? = null
    protected var keyboardHandler: mxKeyboardHandler? = null
    private var rubberband: mxRubberband? = null
    var changes: PropertyChangeSupport = PropertyChangeSupport(this)
    val vertexMenu: ContextMenu = ContextMenu()
    private var bcontent: BorderPane
    var file: File? = null
    private val transactionsTable = javafx.scene.control.TableView<Production>()
    private val grammarTable = javafx.scene.control.TableView<Production>()
    private val grammarData = FXCollections.observableArrayList<Production>()
    private val grammarVBox = VBox()
    private val grammarHBox = HBox()
    private val transitionsVBox = VBox()
    private val transitionsHBox = HBox()

    constructor(iautomaton: IAutomata, text: String? = "new tab", file: File? = null) : super(text) {
        automaton = iautomaton
        this.file = file

        // Creates the embeddable graph swing component
        graphComponent = mxGraphComponent(graph)

        //Allows vertices to have edges from them to themselves
        graph.isAllowLoops = true

        //Prevents edges from pointing to nothing
        graph.isAllowDanglingEdges = false

        graph.isCellsCloneable = false
        //Prevent edge labels from being dragged somewhere absurd
        graph.isEdgeLabelsMovable = false

        graph.isDisconnectOnMove = false

        graph.isAutoSizeCells = true

        graph.isCellsResizable = false

        graph.isCellsEditable = false

        graphComponent.isGridVisible = true

        // Do not change the scale and translation after files have been loaded
        graph.isResetViewOnRootChange = false
        applyDefaultEdgeStyle()

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


        val setAsInitial: MenuItem = MenuItem("Set as Initial")
        val setAsFinal: MenuItem = MenuItem("Set as Final")
        val setAsInitialAndFinal: MenuItem = MenuItem("Set as Initial & Final")

        setAsInitial.onAction = EventHandler { actionEvent: ActionEvent ->
            println((actionEvent.source as MenuItem).parentPopup.x)
            println((actionEvent.source as MenuItem).parentPopup.y)
            //(actionEvent.source as mxCell).setVertexStyle(VertexType.INITIAL)
        }
        setAsFinal.onAction = EventHandler { actionEvent: ActionEvent ->
            setVertexStyle((actionEvent.source as mxCell), VertexType.FINAL)
        }
        setAsInitialAndFinal.onAction = EventHandler { actionEvent: ActionEvent ->
            setVertexStyle((actionEvent.source as mxCell), VertexType.INITIAL_FINAL)
        }
        vertexMenu.items.addAll(setAsInitial, setAsFinal, setAsInitialAndFinal)

        bcontent = BorderPane()

        bcontent.center = SwingNode().apply {
            //Sets the graph as the content of the swing node
            content = graphComponent
        }

        this.contextMenu = vertexMenu

        this.content = bcontent

        addTransactionsTable()
        addCFGTable()
    }

    private fun addCFGTable() {
        val noTerminal = TableColumn<Production, Char>("No Terminals")
        noTerminal.maxWidth = 40.0
        noTerminal.cellValueFactory = PropertyValueFactory("noTerminal")
        val production = TableColumn<Production, String>("Production")
        production.minWidth = 160.0
        production.cellValueFactory = PropertyValueFactory("production")

        //Delete Button
        val actions = TableColumn<Production, Boolean>("Action")
        actions.cellValueFactory = Callback<TableColumn.CellDataFeatures<Production, Boolean>?, ObservableValue<Boolean>?> {
            it ->
            SimpleBooleanProperty(it!!.value != null)
        }
        actions.cellFactory = Callback<javafx.scene.control.TableColumn<automata.Production, Boolean>, javafx.scene.control.TableCell<automata.Production, Boolean>> {
            val cell = object : TableCell<Production, Boolean>() {

                internal val btn = Button("Just Do It")
                init{
                    btn.graphic = ImageView(Image("icons"+File.separator+"folder-11.png"))
                    btn.setOnAction { event: ActionEvent ->
                        tableView.selectionModel.select(index)
                        val person = tableView.items[index]
                        println(person.noTerminal + "   " + person.production)
                        grammarData.remove(tableView.selectionModel.selectedItem)
                    }
                }
                public override fun updateItem(item: Boolean?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (!empty) {
                        contentDisplay = ContentDisplay.GRAPHIC_ONLY
                        graphic = btn
                    } else {
                        graphic = null
                    }
                }
            }
            cell
        }


        val noTerminaltf = TextField()
        noTerminaltf.promptText = "S"
        noTerminaltf.maxWidth = 40.0

        val productiontf = TextField()
        productiontf.promptText = "aSb"
        productiontf.minWidth = 160.0

        val addButton = Button("Add")
        addButton.setOnAction({ e: ActionEvent ->
            if (noTerminaltf.text.isNotEmpty() and productiontf.text.isNotEmpty()) {
                grammarData.add(Production(
                        noTerminaltf.text.first(),
                        productiontf.text
                ))
                noTerminaltf.clear()
                productiontf.clear()
            }
        })

        val convertToPDAButton = Button("To PDA")
        convertToPDAButton.setOnAction({ e: ActionEvent ->
            if(tabPane.selectionModel.selectedItem!=null){
                var tab = tabPane.selectionModel.selectedItem as TabContainer
                tab.redrawAutomata()
            }
        })

        grammarHBox.children.addAll(noTerminaltf, productiontf, addButton,convertToPDAButton)
        grammarHBox.spacing = 0.0

        grammarTable.items = grammarData
        grammarTable.columns.addAll(noTerminal, production, actions)

        grammarVBox.spacing = 5.0
        grammarVBox.padding = Insets(10.0, 0.0, 0.0, 10.0)
        grammarVBox.children.addAll(grammarHBox, grammarTable)


        if (automaton is PDA)
            bcontent.right = grammarVBox

        grammarData.add(Production('S', "aSb"))
    }

    private fun redrawAutomata() {
        graph.removeCells()
        (graph.model as mxGraphModel).clear()
        graphComponent.refresh()

        when(automaton){
            is PDA -> {
                automaton.states.removeAll(automaton.states)

                var grammar = grammarData.toList()

                var noTerminals = grammar.map { it.noTerminal }.distinct()
                var terminals = grammar.map { it.production.toList() }.flatten().subtract(noTerminals)
                (automaton as PDA).stackLanguage = noTerminals.union(terminals).union(listOf('Z')).toMutableList()
                println("$noTerminals")
                println("$terminals")
                println("${(automaton as PDA).stackLanguage}")

                var q0 = State("q0")
                var q1 = State("q1")
                var q2 = State("q2")

                automaton.addState(q0)
                automaton.addState(q1)
                automaton.addState(q2)

                automaton.setInitialState(q0.value)
                automaton.setFinalState(q2.value)

                automaton.addTransition('E',q0.value,q1.value,'Z', listOf(noTerminals.first(),'Z'))
                automaton.addTransition('E',q1.value,q2.value,'Z', listOf('Z'))

                for (entry in grammar){
                    automaton.addTransition('E',q1.value,q1.value,entry.noTerminal, entry.production.toList())
                }

                for(t in terminals){
                    automaton.addTransition(t,q1.value,q1.value,t, listOf('E'))
                }

                for (state in automaton.states){
                    for(transition in state.getTransitions()){
                        println("${transition.source.value} ${transition.symbol} ${transition.top} ${transition.target.value} ${transition.toPush}")
                    }
                }
            }
        }

        drawAutomata(automaton)
    }

    fun toogleCFGTable() {
        if (automaton is PDA)
            if (bcontent.right == null)
                bcontent.right = grammarVBox
            else
                bcontent.right = null
        else
            bcontent.right = null
    }

    private fun addTransactionsTable() {
        val noTerminal = TableColumn<Production, Production>("NT")
        noTerminal.maxWidth = 40.0
        noTerminal.cellValueFactory = PropertyValueFactory("noTerminal")

        val production = TableColumn<Production, Production>("Production")
        production.maxWidth = 300.0
        production.cellValueFactory = PropertyValueFactory("production")


        val noTerminaltf = TextField()
        noTerminaltf.promptText = "S"
        noTerminaltf.maxWidth = 40.0

        val productiontf = TextField()
        productiontf.promptText = "aSb"
        productiontf.maxWidth = 160.0

        val addButton = Button("Add")
        addButton.setOnAction({ e: ActionEvent ->
            if (noTerminaltf.text.isNotEmpty() and productiontf.text.isNotEmpty()) {
                grammarData.add(Production(
                        noTerminaltf.text.first(),
                        productiontf.text
                ))
                noTerminaltf.clear()
                productiontf.clear()
            }
        })

        val delButton = Button("Del")
        delButton.setOnAction({ e: ActionEvent ->
            var tab = tabPane.selectionModel.selectedItem
            if (tab != null) {
                val item = transactionsTable.selectionModel.selectedItem
                if (item != null) {
                    grammarData.remove(item)
                    transactionsTable.refresh()
                }
            }
        })

        transitionsHBox.children.addAll(noTerminaltf, productiontf, addButton, delButton)
        transitionsHBox.spacing = 3.0

        transactionsTable.items = grammarData
        transactionsTable.columns.addAll(noTerminal, production)

        // transitionsVBox.spacing = 5.0
        //transitionsVBox.padding = Insets(10.0, 0.0, 0.0, 10.0)
        transitionsVBox.children.addAll(transitionsHBox, transactionsTable)


        if (automaton is PDA)
            bcontent.right = transitionsVBox
    }


    fun ToogleTransactionsTable() {
        if (bcontent.left == null)
            bcontent.left = transitionsVBox
        else
            bcontent.left = null
    }

    private fun mxCell.toggleType() {
        val style = graph.getCellStyle(this)
        val ss = "${style["strokeColor"]}${style["shape"]}"
        when (ss) {
            "blueellipse" -> {
                setVertexStyle(this, VertexType.INITIAL)
            }
            "redellipse" -> {
                setVertexStyle(this, VertexType.FINAL)
            }
            "greendoubleEllipse" -> {
                setVertexStyle(this, VertexType.INITIAL_FINAL)
            }
            "reddoubleEllipse" -> {
                setVertexStyle(this, VertexType.NORMAL)
            }
            else -> {
                setVertexStyle(this, VertexType.NORMAL)
            }
        }
    }

    private fun mxCell.resize() {
        if (this.isVertex) {
            if (this.value.toString().length > 5) {
                //cell = graph.updateCellSize(cell) as mxCell

                val bounds = graph.view.getState(this).labelBounds
                val g = this.geometry.clone() as mxGeometry

                if (bounds.width > g.width)
                    g.width = bounds.width + 10
                g.height = bounds.width + 10

                graph.update {
                    graph.cellsResized(arrayOf(this), arrayOf(mxRectangle(g)))
                }

            }
        }
    }

    fun setVertexStyle(cell: mxCell, type: VertexType) {
        try {
            if (cell.isVertex) {
                graph.update {
                    when (type) {
                        VertexType.INITIAL -> {
                            cell.style = defaultStyle.replace("strokeColor=blue", "strokeColor=red")
                            automaton.removeFinalState(cell.value.toString())
                            automaton.setInitialState(cell.value.toString())
                        }
                        VertexType.FINAL -> {
                            cell.style = defaultStyle.replace("strokeColor=blue", "strokeColor=green").replace("shape=ellipse", "shape=doubleEllipse")
                            automaton.setFinalState(cell.value.toString())
                        }
                        VertexType.INITIAL_FINAL -> {
                            cell.style = defaultStyle.replace("strokeColor=blue", "strokeColor=red").replace("shape=ellipse", "shape=doubleEllipse")
                            automaton.setInitialState(cell.value.toString())
                            automaton.setFinalState(cell.value.toString())
                        }
                        VertexType.NORMAL -> {
                            cell.style = defaultStyle
                            automaton.setInitialState(null)
                            automaton.removeFinalState(cell.value.toString())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(null, e.message, "Error", JOptionPane.ERROR_MESSAGE)
        }

    }

    private fun mxGraph.ApplyLayout() {

        model.beginUpdate()
        try {
            val layout = mxFastOrganicLayout(graph)
            // set some properties
            layout.forceConstant = 200.0 // the higher, the more separated
            layout.isDisableEdgeStyle = false // true transforms the edges and makes them direct lines
            layout.isUseInputOrigin = true
            // layout graph
            layout.execute(graph.defaultParent)
        } finally {
            val morph = mxMorphing(graphComponent)
            morph.addListener(mxEvent.DONE, { source, evt ->
                graph.model.endUpdate()
                modified = false
            })

            morph.startAnimation()
        }


    }

    fun mxGraph.update(block: () -> Any) {
        model.beginUpdate()
        try {
            block()
        } finally {
            model.endUpdate()
        }
    }

    private fun applyDefaultEdgeStyle() {
        val edge = HashMap<String, Any>()
        edge.put(mxConstants.STYLE_ROUNDED, true)
        edge.put(mxConstants.STYLE_ORTHOGONAL, false)
        edge.put(mxConstants.STYLE_EDGE, mxEdgeStyle.TopToBottom)
        edge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR)
        edge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC)
        edge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE)
        edge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER)
        edge.put(mxConstants.STYLE_STROKECOLOR, "#6482B9")
        edge.put(mxConstants.STYLE_FONTCOLOR, "#ffffff")
        edge.put(mxConstants.STYLE_ARCSIZE, 80)
        edge.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "#6482B9")

        val edgeStyle = mxStylesheet()
        edgeStyle.defaultEdgeStyle = edge
        graph.stylesheet = edgeStyle
    }

    private fun drawAutomata(automata: IAutomata) {
        var x = 10.0
        var y = 10.0
        var maxGeometryHeight = 0.0
        val hEdgePadding = 100.0
        val VEdgePadding = 100.0
        val cells: HashMap<String, mxCell> = hashMapOf()

        for (state in automata.states) {
            val cell = graph.insertVertex(graph.defaultParent, null, state.value, x, y, 40.0, 40.0, defaultStyle) as mxCell
            val initial = automata.getInitialState()
            if (initial != null) {
                val isInitial = initial.value == state.value
                val isFinal = automata.isFinal(state.value)
                if (isInitial and isFinal) {
                    setVertexStyle(cell, VertexType.INITIAL_FINAL)
                } else if (isInitial) {
                    setVertexStyle(cell, VertexType.INITIAL)
                } else if (isFinal) {
                    setVertexStyle(cell, VertexType.FINAL)
                }
            }
            cell.resize()
            cells[state.value] = cell

            if (Screen.getPrimary().bounds.width - 10.0 >= (x + cell.geometry.width + hEdgePadding)) {
                x += cell.geometry.width + hEdgePadding
                if (cell.geometry.height > maxGeometryHeight)
                    maxGeometryHeight = cell.geometry.height

            } else {
                y += maxGeometryHeight + VEdgePadding
                x = 10.0
            }
        }

        graph.update {
            for (state in automata.states) {
                for (transition in state.getTransitions()) {
                    val source = cells[transition.source.value]
                    val target = cells[transition.target.value]
                    var symbol = transition.symbol
                    when(automata){
                        is PDA -> {
                            symbol = "${transition.symbol},${transition.top}/${transition.toPush.joinToString()}"
                        }
                    }
                    graph.insertEdge(graph.defaultParent, null, symbol , source, target)
                }
            }

//            Another way to do it
//            for (state in automata.states) {
//                for (group in state.getTransitions().groupBy { it.target }) {
//                    val source = cells[group.value.first().source.value]
//                    val target = cells[group.key.value]
//                    var symbol = group.value.map { it.symbol }.joinToString()
//                    when(automata){
//                        is PDA -> {
//                            symbol = group.value.map { "${it.symbol},${it.top}/${it.toPush.joinToString()}" }.joinToString("\n")
//                        }
//                    }
//                    graph.insertEdge(graph.defaultParent, null, symbol , source, target)
//                }
//            }
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
            if (cell.isEdge) {
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
                    automaton.addTransition(s.first().toChar(), cell.source.value.toString(), cell.target.value.toString())
                    cell.value = s
                    cell.style = "rounded=true;arcSize=30;edgeStyle=orthogonalEdgeStyle;portConstraint=north"
                } catch (e: NullPointerException) {
                    if (cell.isEdge) {
                        graph.model.remove(cell)
                    }
                } catch (e: Exception) {
                    JOptionPane.showMessageDialog(null, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                    graph.model.remove(cell)
                }
            } else {
                println("Got a cell: ${cell.value}")
            }

        }



        graphComponent.graphControl.addMouseListener(object : MouseAdapter() {

            override fun mouseReleased(e: java.awt.event.MouseEvent) {

                val cell = graphComponent.getCellAt(e.x, e.y)

                if (cell is mxCell) {
                    if (e.button == 3) { //Rigth Click
                        //vertexMenu.show(bcontent, e.xOnScreen.toDouble(),e.yOnScreen.toDouble())
                    } else if (e.clickCount == 2) {
                        println("vertex clicked")
                        cell.toggleType()
                        graphComponent.refresh()
                    }
                } else {
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
                        } while (s.isNullOrEmpty() || s.equals("new vertex"))
                        val cell = graph.insertVertex(graph.defaultParent, null, s?.toLowerCase(), e.x.toDouble(), e.y.toDouble(), 40.0, 40.0, defaultStyle)
                        try {
                            cell as mxCell
                            automaton.addState(State(cell.value.toString()))
                            cell.resize()
                        } catch (e: Exception) {
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
            this.text += "*"
        })

    }




}

