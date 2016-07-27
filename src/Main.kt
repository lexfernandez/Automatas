///**
// * Created by lex on 07-24-16.
// */
//// If this imports do not work, then you didn't add the library correctly
//import automata.DFA
//import automata.State
//import com.mxgraph.model.mxCell
//import com.mxgraph.swing.mxGraphComponent
//import com.mxgraph.util.mxEvent
//import com.mxgraph.view.mxGraph
//
////Everything should come from JavaFX, otherwise you've done something wrong
//import javafx.application.Application
//import javafx.embed.swing.SwingNode
//import javafx.event.EventHandler
//import javafx.scene.Scene
//import javafx.scene.control.Button
//import javafx.scene.control.TextField
//import javafx.scene.input.KeyCode
//import javafx.scene.input.MouseEvent
//import javafx.scene.layout.FlowPane
//import javafx.stage.Stage
//import java.awt.event.MouseAdapter
//import javax.swing.JOptionPane
//
//
//
//
//// This is what makes this file the starting point of the program
//fun main(args: Array<String>) {
//    // The only thing it does is to launch our JavaFX application defined below
////    var dfa: DFA = DFA()
////    Automata multiplos de 3
////    dfa.addState(State(0,"s0"))
////    dfa.addState(State(1,"s1"))
////    dfa.addState(State(2,"s2"))
////
////    dfa.addTransition('0',0,0)
////    dfa.addTransition('1',0,1)
////    dfa.addTransition('1',1,0)
////    dfa.addTransition('0',1,2)
////    dfa.addTransition('0',2,1)
////    dfa.addTransition('1',2,2)
////
////    dfa.setInitialState(0)
////    dfa.setFinalState(0)
////    dfa.printStates()
//
////    println("evaluation: ${dfa.evaluate("0011")}")
//
//    Application.launch(MainApplication::class.java)
//}
//
//class MainApplication : Application() {
//    var dfa: DFA = DFA()
//    val graph = mxGraph()
//    var currentCellId : String = ""
//    val defaultStyle: String = "shape=ellipse;fillColor=white;strokeColor=blue"
//    override fun start(stage: Stage) {
//        //Create a simple Graph Object with two vertices and one edge
//
//        val parent = graph.defaultParent
//
//        // This is a call to an extension function defined below
//        // In other languages this would have looked like this: graph.update( () -> doSomething() )
//        // In JavaScript, it would have looked like this: graph.update( function(){ //code; //moreCode; } )
//        graph.update {
//            // Creates a vertex
//            // Gives it a parent, position, dimensions and style
//            val vertexOne = graph.insertVertex(parent, null, "q0", 20.0, 20.0, 50.0, 50.0, defaultStyle)
//            // Creates another vertex. But this one looks like an accepted state
//            val vertexTwo = graph.insertVertex(parent, null, "q1", 840.0, 450.0, 50.0, 50.0, defaultStyle)
//
//            // Creates an edge between the two vertices
//            graph.insertEdge(parent, null, "1", vertexOne, vertexTwo)
//
//            // Creates an edge from one vertex to itself to demonstrate loops
//            graph.insertEdge(parent, null, "0", vertexTwo, vertexTwo)
//
//            graph.removeCells()
//        }
//
//
//
//        // Creates the embeddable graph swing component
//        val graphComponent = mxGraphComponent(graph)
//
//
//        //Allows vertices to have edges from them to themselves
//        graph.isAllowLoops = true
//
//        //Prevents edges from pointing to nothing
//        graph.isAllowDanglingEdges = false
//
//        graph.isCellsCloneable=false
//        //Prevent edge labels from being dragged somewhere absurd
//        graph.isEdgeLabelsMovable = false
//
//        graph.isDisconnectOnMove = false
//
//        graphComponent.connectionHandler.addListener(mxEvent.CONNECT) { sender, evt ->
//            val cell = evt.getProperty("cell") as mxCell
//            var s: String
//
//            do {
//                s = JOptionPane.showInputDialog(
//                        null,
//                        "Enter vertex name:\n" + "e.g.\"1\" or \"a\"",
//                        "Vertex Name",
//                        JOptionPane.PLAIN_MESSAGE,
//                        null,
//                        null,
//                        "0") as String
//            } while (s.isNullOrEmpty() || s.equals("new vertex"))
//            try {
//                dfa.addTransition(s.first().toChar(),cell.source.id.toInt(),cell.target.id.toInt())
//                cell.value = s
//                cell.style = "rounded=true;arcSize=30"
//            }catch (e: Exception){
//                graph.model.remove(cell)
//            }
//        }
//
//        graphComponent.connectionHandler.addListener(mxEvent.LABEL_CHANGED) { sender, evt ->
//            println("label change to=" + evt.name)
//            println(evt.properties.toString())
//        }
//
//        graphComponent.graphControl.addMouseListener(object : MouseAdapter() {
//
//            override fun mouseReleased(e: java.awt.event.MouseEvent?) {
//                val cell = graphComponent.getCellAt(e!!.x, e.y)
//                if (cell is mxCell) {
//                    currentCellId=cell.id
//                } else {
//
//                    if (e.clickCount == 2) {
//                        var s: String?
//
//                        do {
//                            s = JOptionPane.showInputDialog(
//                                    null,
//                                    "Enter vertex name:\n" + "e.g.\"1\" or \"a\"",
//                                    "Vertex Name",
//                                    JOptionPane.PLAIN_MESSAGE,
//                                    null,
//                                    null,
//                                    "0") as String?
//                        } while (s.isNullOrEmpty() || s.equals("new vertex") )
//                        var cell = graph.insertVertex(graph.defaultParent, null, s?.toLowerCase(), e.x.toDouble(), e.y.toDouble(), 40.0, 40.0, defaultStyle)
//                        try {
//                            cell as mxCell
//                            dfa.addState(State(cell.id.toInt(), s ?: "q" + cell.id))
//
//                        }catch (e: Exception){
//                            graph.model.remove(cell)
//                        }
//                    }
//                }
//            }
//        })
//
//        //Creates a TextField
//        val aTextField = TextField()
//
//        // Creates a button with some text
//        val aButton = Button("Print Whatever is on the TextField")
//
//        // Creates an event handler for the button
//        aButton.onMouseClicked = EventHandler<MouseEvent> {
//            var alphabet = aTextField.getText()?:""
//            JOptionPane.showMessageDialog(null, "evaluation: ${dfa.evaluate(alphabet)}")
//        }
//
//        //Create the actual container for the GUI
//        val sceneRoot = FlowPane(SwingNode().apply {
//            //Sets the graph as the content of the swing node
//            content = graphComponent
//        },
//                aTextField,
//                aButton
//        )
//
//        //Adds the Scene to the Stage, a.k.a the actual Window
//        stage.scene = Scene(sceneRoot, 900.0, 700.0)
//
//        //add on key release event to scene
//        stage.scene.onKeyPressed = object : EventHandler<javafx.scene.input.KeyEvent> {
//
//            override fun handle(e: javafx.scene.input.KeyEvent) {
//                println("keyEvnet: ${e.code}")
//                if (e.isAltDown() && e.getCode() === KeyCode.I) {
//
//                    println("setting initial")
//                    graph.update {
//                        var cell = (graph.selectionCell as mxCell)
//                        if(cell is mxCell){
//                            if(cell.isVertex){
//                                val style = graph.getCellStyle(cell)
//                                if(style["strokeColor"]=="blue")
//                                    cell.style=cell.style.replace("strokeColor=blue","strokeColor=red")
//                                else
//                                    cell.style=cell.style.replace("strokeColor=green","strokeColor=red")
//                                dfa.setInitialState(cell.id.toInt())
//                            }
//                        }
//                    }
//                    graphComponent.refresh()
//                    //Stop letting it do anything else
//
//                    e.consume()
//
//                }else if (e.isAltDown() && e.getCode() === KeyCode.F) {
//
//                    println("setting final")
//                    graph.update {
//                        var cell = (graph.selectionCell as mxCell)
//                        if(cell is mxCell){
//                            if(cell.isVertex){
//                                val style = graph.getCellStyle(cell)
//                                if(style["strokeColor"]=="blue")
//                                    cell.style=cell.style.replace("strokeColor=blue","strokeColor=green")
//                                cell.style=cell.style.replace("shape=ellipse","shape=doubleEllipse")
//                                dfa.setFinalState(cell.id.toInt())
//                            }
//                        }
//                    }
//                    graphComponent.refresh()
//                    //Stop letting it do anything else
//
//                    e.consume()
//
//                }else if (e.isAltDown() && e.getCode() === KeyCode.N) {
//
//                    println("setting normal")
//                    graph.update {
//                        var cell = (graph.selectionCell as mxCell)
//                        if(cell is mxCell){
//                            if(cell.isVertex)
//                                cell.style=defaultStyle
//                        }
//                    }
//                    graphComponent.refresh()
//                    //Stop letting it do anything else
//
//                    e.consume()
//
//                }else if (e.getCode() === KeyCode.DELETE) {
//
//                    println("Deleting cell")
//                    graph.update {
//
//                        var cell = (graph.selectionCell as mxCell)
//                        if(cell.isVertex){
//
//                            for (edge in graph.getEdges(cell)){
//                                graph.model.remove(edge)
//                            }
//                        }
//                        graph.model.remove(cell)
//                    }
//                    graphComponent.refresh()
//                    //Stop letting it do anything else
//
//                    e.consume()
//
//                }
//
//            }
//
//        }
//
//        //stage.isFullScreen=true
//        //Shows the window
//        stage.show()
//    }
//
//
//    // This function is an extension to the mxGraph class
//    // I have no idea about how on this thing works
//    // UPDATE: now I now how it works. this "block" parameter is actually a callable function object
//    private fun  mxGraph.update(block: () -> Any) {
//        // Apparently the model variable is a child of mxGraph instance we are extending
//        model.beginUpdate()
//        try {
//            // I guess this prevents other threads from updating the model
//            // Or it makes the Earth orbit around the sun, I'm not sure
//            // UPDATE: turns out it has nothing to do with Earth's orbit
//            block()
//            // Thanks to Alex for pointing out that the "block" object come as a parameter,
//            // And since it's a function, it can be executed by appending parenthesis
//        }
//        finally {
//            model.endUpdate()
//        }
//    }
//}