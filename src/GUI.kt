/**
 * Created by lex on 07-24-16.
 */

import automata.DFA
import automata.State
import com.mxgraph.model.mxCell
import com.mxgraph.model.mxGeometry
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.util.mxEvent
import com.mxgraph.util.mxRectangle
import com.mxgraph.view.mxGraph
import javafx.application.Application
import javafx.embed.swing.SwingNode
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import java.awt.event.MouseAdapter
import javax.swing.JOptionPane


// This is what makes this file the starting point of the program
fun main(args: Array<String>) {
//    var dfa: DFA = DFA()
//    Automata multiplos de 3
//    dfa.addState(State(0,"s0"))
//    dfa.addState(State(1,"s1"))
//    dfa.addState(State(2,"s2"))
//
//    dfa.addTransition('0',0,0)
//    dfa.addTransition('1',0,1)
//    dfa.addTransition('1',1,0)
//    dfa.addTransition('0',1,2)
//    dfa.addTransition('0',2,1)
//    dfa.addTransition('1',2,2)
//
//    dfa.setInitialState(0)
//    dfa.setFinalState(0)
//    dfa.printStates()

//    println("evaluation: ${dfa.evaluate("0011")}")


    // The only thing it does is to launch our JavaFX application defined below
    Application.launch(GUI::class.java)
}

class GUI : Application() {
    val graph = mxGraph()
    var dfa: DFA = DFA()
    val defaultStyle: String = "shape=ellipse;fillColor=white;strokeColor=blue"

    override fun start(primaryStage: Stage) {

        //Setup the VBox Container and BorderPane
        val root = BorderPane()
        val topContainer = VBox()

        // Creates the embeddable graph swing component
        val graphComponent = mxGraphComponent(graph)

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


        graphComponent.isGridVisible=true


        //Setup the Main Menu bar and the ToolBar
        val mainMenu = MenuBar()
        val toolBar = ToolBar()

        //Create SubMenu File.
        val file = Menu("File")
        val openFile = MenuItem("Open File")
        val exitApp = MenuItem("Exit")
        file.items.addAll(openFile, exitApp)

        //Create SubMenu Edit.
        val edit = Menu("Edit")
        val properties = MenuItem("Properties")
        edit.items.add(properties)

        //Create SubMenu Help.
        val help = Menu("Help")
        val visitWebsite = MenuItem("Visit Website")
        help.items.add(visitWebsite)

        mainMenu.menus.addAll(file, edit, help)

        //Create some toolbar buttons
        val openFileBtn = Button()
        val printBtn = Button()
        val snapshotBtn = Button()
        val alphabetTextField = TextField()
        val evaluateBtn = Button("Evaluar")


        //Add some button graphics
        openFileBtn.graphic = ImageView("file:///../icons/folder-11.png")
        printBtn.graphic = ImageView("file:///../icons/print.png")
        snapshotBtn.graphic = ImageView("file:///../icons/photo-camera-1.png")


        toolBar.items.addAll(openFileBtn, printBtn, snapshotBtn,Separator(),alphabetTextField,evaluateBtn)

        //Add the ToolBar and Main Meu to the VBox
        topContainer.children.add(mainMenu)
        topContainer.children.add(toolBar)


        //Apply the VBox to the Top Border
        root.top = topContainer
        root.center = BorderPane(SwingNode().apply {
            //Sets the graph as the content of the swing node
            content = graphComponent
        })

        graphComponent.connectionHandler.addListener(mxEvent.CONNECT) { sender, evt ->
            val cell = evt.getProperty("cell") as mxCell
            var s: String

            do {
                s = JOptionPane.showInputDialog(
                        null,
                        "Enter vertex name:\n" + "e.g.\"1\" or \"a\"",
                        "Vertex Name",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "0") as String
            } while (s.isNullOrEmpty() || s.equals("new vertex"))
            try {
                dfa.addTransition(s.first().toChar(),cell.source.value.toString(),cell.target.value.toString())
                cell.value = s
                cell.style = "rounded=true;arcSize=30"
            }catch (e: Exception){
                graph.model.remove(cell)
            }
        }

        graphComponent.connectionHandler.addListener(mxEvent.LABEL_CHANGED) { sender, evt ->
            println("label change to=" + evt.name)
            println(evt.properties.toString())
        }

        graphComponent.graphControl.addMouseListener(object : MouseAdapter() {

            override fun mouseReleased(e: java.awt.event.MouseEvent?) {
                val cell = graphComponent.getCellAt(e!!.x, e.y)
                if (cell !is mxCell) {

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
                                    "0") as String?
                        } while (s.isNullOrEmpty() || s.equals("new vertex") )
                        var cell = graph.insertVertex(graph.defaultParent, null, s?.toLowerCase(), e.x.toDouble(), e.y.toDouble(), 40.0, 40.0, defaultStyle)
                        try {
                            cell as mxCell
                            dfa.addState(State(cell.value.toString()))
                            cell.resize()
                        }catch (e: Exception){
                            graph.model.remove(cell)
                        }
                    }
                }
            }
        })

        // Creates an event handler for the button
        evaluateBtn.onMouseClicked = EventHandler<MouseEvent> {
            val alphabet = alphabetTextField.text ?:""
            JOptionPane.showMessageDialog(null, "evaluation: ${dfa.evaluate(alphabet)}")
        }

        val scene = Scene(root, Screen.getPrimary().bounds.width-200, Screen.getPrimary().bounds.height-200)

        //add on key release event to scene
        scene.onKeyPressed = EventHandler<KeyEvent> { e ->
            println("keyEvnet: ${e.code}")
            if (e.isAltDown && e.code === KeyCode.I) {

                println("setting initial")
                graph.update {
                    var cell: mxCell = (graph.selectionCell as mxCell)
                    if(cell is mxCell){
                        if(cell.isVertex){
                            val style = graph.getCellStyle(cell)
                            if(style["strokeColor"]=="blue")
                                cell.style=cell.style.replace("strokeColor=blue","strokeColor=red")
                            else
                                cell.style=cell.style.replace("strokeColor=green","strokeColor=red")
                            dfa.setInitialState(cell.value.toString())
                        }
                    }
                }
                graphComponent.refresh()
                //Stop letting it do anything else

                e.consume()

            }else if (e.isAltDown && e.code === KeyCode.F) {

                println("setting final")
                graph.update {
                    var cell = (graph.selectionCell as mxCell)
                    if(cell is mxCell){
                        if(cell.isVertex){
                            val style = graph.getCellStyle(cell)
                            if(style["strokeColor"]=="blue")
                                cell.style=cell.style.replace("strokeColor=blue","strokeColor=green")
                            cell.style=cell.style.replace("shape=ellipse","shape=doubleEllipse")
                            dfa.setFinalState(cell.value.toString())
                        }
                    }
                }
                graphComponent.refresh()
                //Stop letting it do anything else

                e.consume()

            }else if (e.isAltDown && e.code === KeyCode.N) {

                println("setting normal")
                graph.update {
                    var cell = (graph.selectionCell as mxCell)
                    if(cell is mxCell){
                        if(cell.isVertex)
                            cell.style=defaultStyle
                    }
                }
                graphComponent.refresh()
                //Stop letting it do anything else

                e.consume()

            }else if (e.getCode() === KeyCode.DELETE) {

                println("Deleting cell")
                graph.update {

                    var cell = (graph.selectionCell as mxCell)
                    if(cell.isVertex){

                        for (edge in graph.getEdges(cell)){
                            graph.model.remove(edge)
                        }
                    }
                    graph.model.remove(cell)
                }
                graphComponent.refresh()
                //Stop letting it do anything else

                e.consume()

            }
        }

        //Setup the Stage.
        primaryStage.title = "Automatas"
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun mxCell.resize(){
        if(this.isVertex){
            if(this.value.toString().length>5){
                //cell = graph.updateCellSize(cell) as mxCell

                var bounds = graph.view.getState(this).labelBounds
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

    private fun mxCell.setVertexType(type: VertexType){
        when(type){
            VertexType.NORMAL -> {
                var st =  graph.stylesheet
                st.
            }
            VertexType.INITIAL -> {}
            VertexType.FINAL -> {}
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

}

