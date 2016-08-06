/**
 * Created by lex on 07-24-16.
 */

import automata.*
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
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import java.awt.event.MouseAdapter
import java.io.File
import java.util.*
import javax.swing.JOptionPane.*


// This is what makes this file the starting point of the program
fun main(args: Array<String>) {
    //IAutomata multiplos de 3


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

    var stage: Stage = Stage()

    override fun start(primaryStage: Stage) {
        stage = primaryStage
        //Setup the VBox Container and BorderPane
        val root = BorderPane()
        val topContainer = VBox()

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
        openFileBtn.graphic = ImageView(Image("icons"+File.separator+"folder-11.png"))
        printBtn.graphic = ImageView(Image("icons"+File.separator+"print.png"))
        snapshotBtn.graphic = ImageView(Image("icons"+File.separator+"photo-camera-1.png"))


        toolBar.items.addAll(openFileBtn, printBtn, snapshotBtn,Separator(),alphabetTextField,evaluateBtn)

        //Add the ToolBar and Main Meu to the VBox
        topContainer.children.add(mainMenu)
        topContainer.children.add(toolBar)


        //Apply the VBox to the Top Border
        var dfa:NFAE=NFAE()
        dfa.addState(State("0"))
        dfa.addState(State("1"))
        dfa.addState(State("2"))
        dfa.addState(State("3"))
        dfa.addState(State("4"))
        dfa.addState(State("5"))
        dfa.addState(State("6"))
        dfa.addState(State("7"))
        dfa.addState(State("8"))
        dfa.addState(State("9"))
        dfa.addState(State("10"))

        dfa.addTransition('E',"0","1")
        dfa.addTransition('E',"1","2")
        dfa.addTransition('E',"1","4")
        dfa.addTransition('a',"2","3")
        dfa.addTransition('b',"4","5")
        dfa.addTransition('E',"3","6")
        dfa.addTransition('E',"5","6")
        dfa.addTransition('E',"6","7")
        dfa.addTransition('a',"7","8")
        dfa.addTransition('b',"8","9")
        dfa.addTransition('b',"9","10")
        dfa.addTransition('E',"0","7")
        dfa.addTransition('E',"6","1")

        dfa.setInitialState("0")
        dfa.setFinalState("10")

        var tab = TabContainer(dfa)
        var tabPane = TabPane()
        tabPane.tabs.add(tab)
        tabPane.tabs.add(TabContainer(DFA()))
        root.top = topContainer
        root.center = tabPane




//        graphComponent.connectionHandler.addListener(mxEvent.LABEL_CHANGED) { sender, evt ->
//            println("label change to=" + evt.name)
//            println(evt.properties.toString())
//        }


        // Creates an event handler for the button
//        evaluateBtn.onMouseClicked = EventHandler<MouseEvent> {
//            val alphabet = alphabetTextField.text ?:""
//            try{
//                showMessageDialog(null, "evaluation: ${dfa.evaluate(alphabet)}")
//            }catch (e: Exception){
//                showMessageDialog(null, e.message, "Error",ERROR_MESSAGE)
//            }
//
//        }

        val scene = Scene(root, Screen.getPrimary().bounds.width-200, Screen.getPrimary().bounds.height-200)

//        //add on key release event to scene
//        scene.onKeyPressed = EventHandler<KeyEvent> { e ->
//            try{
//                println("keyEvnet: ${e.code}")
//                val cell: mxCell = (graph.selectionCell as mxCell)
//                if(cell.isVertex){
//                    if (e.isAltDown && e.code === KeyCode.I) {
//                        println("setting initial")
//                        cell.setVertexStyle(VertexType.INITIAL)
//                        graphComponent.refresh()
//                    }else if (e.isAltDown && e.code === KeyCode.F) {
//                        println("setting final")
//                        cell.setVertexStyle(VertexType.FINAL)
//                        graphComponent.refresh()
//                    }else if (e.isAltDown && e.code === KeyCode.B) {
//                        println("setting Both Initial and Final")
//                        cell.setVertexStyle(VertexType.INITIAL_FINAL)
//                        graphComponent.refresh()
//                    }else if (e.isAltDown && e.code === KeyCode.N) {
//                        println("setting normal")
//                        cell.setVertexStyle(VertexType.NORMAL)
//                        graphComponent.refresh()
//                    }else if (e.code === KeyCode.DELETE) {
//                        println("Deleting cell")
//                        if(dfa.removeState(cell.value.toString())){
//                            graph.update {
//                                for (edge in graph.getEdges(cell)){
//                                    graph.model.remove(edge)
//                                }
//                                graph.model.remove(cell)
//                            }
//                            graphComponent.refresh()
//                        }
//
//                    }
//                    e.consume()
//                }else if(cell.isEdge){
//                    if (e.code === KeyCode.DELETE) {
//                        println("Deleting edge")
//                        graph.update {
//                            graph.model.remove(cell)
//                        }
//                        graphComponent.refresh()
//                    }
//                    e.consume()
//                }
//                e.consume()
//            }catch (e:Exception){
//                showMessageDialog(null, e.message, "Error",ERROR_MESSAGE)
//            }
//
//        }



        //Setup the Stage.
        primaryStage.title = "Automatas"
        primaryStage.scene = scene
        primaryStage.show()


        //(dfa as NFAE).printClosure()

    }


}

