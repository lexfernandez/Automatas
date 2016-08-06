import automata.IAutomata
import automata.NFAE
import automata.State
import com.mxgraph.model.mxCell
import com.mxgraph.model.mxGeometry
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.util.mxEvent
import com.mxgraph.util.mxRectangle
import com.mxgraph.view.mxGraph
import javafx.embed.swing.SwingNode
import javafx.scene.control.Tab
import javafx.scene.layout.BorderPane
import javafx.stage.Screen
import java.awt.event.MouseAdapter
import java.util.*
import javax.swing.JOptionPane

/**
 * Created by lex on 08-05-16.
 */


class TabContainer: Tab {
    val graph = mxGraph()
    var dfa: IAutomata
    val defaultStyle: String = "shape=ellipse;fillColor=white;strokeColor=blue;defaultHotspot=1.0"
    var graphComponent:mxGraphComponent
    constructor(automaton: IAutomata,text: String? = "new tab"):super(text){
        dfa=automaton

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

        this.content= BorderPane(SwingNode().apply {
            //Sets the graph as the content of the swing node
            content = graphComponent
        })

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
                    dfa.addTransition(s.first().toChar(),cell.source.value.toString(),cell.target.value.toString())
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
                    if (e.clickCount == 2) {
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
                            dfa.addState(State(cell.value.toString()))
                            cell.resize()
                        }catch (e: Exception){
                            JOptionPane.showMessageDialog(null, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                            graph.model.remove(cell)
                        }
                    }
                }
            }

            override fun mousePressed(e: java.awt.event.MouseEvent) {
                var cell = graphComponent.getCellAt(e.x, e.y);
                println("Mouse click in graph component");
                if (cell != null) {
                    println("cell=" + graph.getLabel(cell));
                }
            }
        })

        drawAutomata(dfa)
    }

    private fun mxCell.toggleType() {
        val style = graph.getCellStyle(this)
        val ss="${style["strokeColor"]}${style["shape"]}"
        when(ss){
            "blueellipse" -> {
                this.setVertexStyle(VertexType.INITIAL)
            }
            "redellipse" -> {
                this.setVertexStyle(VertexType.FINAL)
            }
            "greendoubleEllipse" -> {
                this.setVertexStyle(VertexType.INITIAL_FINAL)
            }
            "reddoubleEllipse" -> {
                this.setVertexStyle(VertexType.NORMAL)
            }
            else -> {
                this.setVertexStyle(VertexType.NORMAL)
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

    private fun mxCell.setVertexStyle(type: VertexType){
        try {
            if(this.isVertex){
                graph.update {
                    when(type){
                        VertexType.INITIAL -> {
                            this.style=defaultStyle.replace("strokeColor=blue","strokeColor=red")
                            dfa.unsetFinalState(this.value.toString())
                            dfa.setInitialState(this.value.toString())
                        }
                        VertexType.FINAL -> {
                            this.style=defaultStyle.replace("strokeColor=blue","strokeColor=green").replace("shape=ellipse","shape=doubleEllipse")
                            dfa.setFinalState(this.value.toString())
                        }
                        VertexType.INITIAL_FINAL -> {
                            this.style=defaultStyle.replace("strokeColor=blue","strokeColor=red").replace("shape=ellipse","shape=doubleEllipse")
                            dfa.setInitialState(this.value.toString())
                            dfa.setFinalState(this.value.toString())
                        }
                        VertexType.NORMAL -> {
                            this.style=defaultStyle
                            dfa.setInitialState(null)
                            dfa.unsetFinalState(this.value.toString())
                        }
                    }
                }
            }
        }catch (e:Exception){
            JOptionPane.showMessageDialog(null, e.message, "Error", JOptionPane.ERROR_MESSAGE)
        }

    }

    private fun  mxGraph.update(block: () -> Any) {
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
        var hEdgePadding = 100.0
        var VEdgePadding = 100.0
        var cells: HashMap<String, mxCell> = hashMapOf()

        for (state in automata.states){
            val cell = graph.insertVertex(graph.defaultParent, null,state.value, x, y, 40.0, 40.0, defaultStyle) as mxCell
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
                    var source = cells[transition.source.value]
                    var target = cells[transition.target.value]
                    graph.insertEdge(graph.defaultParent, null,transition.symbol,source,target)
                }
            }
        }
    }



}