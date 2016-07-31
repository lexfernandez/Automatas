/**
 * Created by lex on 07-24-16.
 */

import automata.DFA
import automata.State
import VertexType
import automata.NFA
import com.mxgraph.model.mxCell
import com.mxgraph.model.mxGeometry
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.util.mxEvent
import com.mxgraph.util.mxRectangle
import com.mxgraph.view.mxGraph
import com.mxgraph.view.mxStylesheet
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
import java.io.*
import javax.swing.JOptionPane


// This is what makes this file the starting point of the program
fun main(args: Array<String>) {
//    var dfa: DFA = DFA()
//    IAutomata multiplos de 3
//    dfa.addState(State("0"))
//    dfa.addState(State("1"))
//    dfa.addState(State("2"))
//
//    dfa.addTransition('0',"0","0")
//    dfa.addTransition('1',"0","1")
//    dfa.addTransition('1',"1","0")
//    dfa.addTransition('0',"1","2")
//    dfa.addTransition('0',"2","1")
//    dfa.addTransition('1',"2","2")
//
//    dfa.setInitialState("0")
//    dfa.setFinalState("0")
//    dfa.printStates()
//
//    println("evaluation: ${dfa.evaluate("0011")}")
//    try {
//        val fileOut = FileOutputStream("./numerosPrimos.dfa")
//        val out = ObjectOutputStream(fileOut)
//        out.writeObject(dfa)
//        out.flush()
//        out.close()
//        fileOut.close()
//        System.out.printf("Serialized data is saved in ./numerosPrimos.dfa")
//    } catch (i: IOException) {
//        i.printStackTrace()
//    }


//    var e: DFA? = null
//    try {
//        val fileIn = FileInputStream("./numerosPrimos.dfa")
//        val ois: ObjectInputStream = ObjectInputStream(fileIn)
//        e = ois.readObject() as DFA
//        ois.close()
//        fileIn.close()
//    } catch (i: IOException) {
//        i.printStackTrace()
//        return
//    } catch (c: ClassNotFoundException) {
//        println("Employee class not found")
//        c.printStackTrace()
//        return
//    }
//
//    e.printStates()
//
//    println("evaluation: ${e.evaluate("0111")}")
//    println("evaluation: ${e.evaluate("01111")}")


    // The only thing it does is to launch our JavaFX application defined below
    Application.launch(GUI::class.java)
}

class GUI : Application() {
    val graph = mxGraph()
    var dfa: DFA = NFA()
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

        graph.isCellsEditable=false

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


        //Add some button graphicsdelta(q,a)delta(q,a)
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
            }catch (e: Exception){
                graph.model.remove(cell)
            }
        }

//        graphComponent.connectionHandler.addListener(mxEvent.LABEL_CHANGED) { sender, evt ->
//            println("label change to=" + evt.name)
//            println(evt.properties.toString())
//        }

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
                                    "0") as String?
                        } while (s.isNullOrEmpty() || s.equals("new vertex") )
                        val cell = graph.insertVertex(graph.defaultParent, null, s?.toLowerCase(), e.x.toDouble(), e.y.toDouble(), 40.0, 40.0, defaultStyle)
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
            val cell: mxCell = (graph.selectionCell as mxCell)
            if(cell.isVertex){
                if (e.isAltDown && e.code === KeyCode.I) {
                    println("setting initial")
                    cell.setVertexStyle(VertexType.INITIAL)
                    graphComponent.refresh()
                }else if (e.isAltDown && e.code === KeyCode.F) {
                    println("setting final")
                    cell.setVertexStyle(VertexType.FINAL)
                    graphComponent.refresh()
                }else if (e.isAltDown && e.code === KeyCode.B) {
                    println("setting Both Initial and Final")
                    cell.setVertexStyle(VertexType.INITIAL_FINAL)
                    graphComponent.refresh()
                }else if (e.isAltDown && e.code === KeyCode.N) {
                    println("setting normal")
                    cell.setVertexStyle(VertexType.NORMAL)
                    graphComponent.refresh()
                }else if (e.code === KeyCode.DELETE) {
                    println("Deleting cell")
                    if(dfa.removeState(cell.value.toString())){
                        graph.update {
                            for (edge in graph.getEdges(cell)){
                                graph.model.remove(edge)
                            }
                            graph.model.remove(cell)
                        }
                        graphComponent.refresh()
                    }

                }
                e.consume()
            }else if(cell.isEdge){
                if (e.code === KeyCode.DELETE) {
                    println("Deleting edge")
                    graph.update {
                        graph.model.remove(cell)
                    }
                    graphComponent.refresh()
                }
                e.consume()
            }
            e.consume()

        }

        //Setup the Stage.
        primaryStage.title = "Automatas"
        primaryStage.scene = scene
        primaryStage.show()
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

