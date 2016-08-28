/**
 * Created by lex on 07-24-16.
 */

import automata.*
import com.mxgraph.model.mxCell
import com.mxgraph.view.mxGraph
import javafx.application.Application
import javafx.event.ActionEvent
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
import java.io.File


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
    var tabPane = TabPane()
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
        val newFile = Menu("New")
        val dfaMenu = MenuItem("DFA")
        val nfaMenu = MenuItem("NFA")
        val nfaeMenu = MenuItem("NFA-e")
        val regexMenu = MenuItem("Regex")
        dfaMenu.onAction= EventHandler<ActionEvent> {
            var newDfa = DFA()
            addNewTab(newDfa)
        }
        nfaMenu.onAction= EventHandler<ActionEvent> {
            var newNfa = NFA()
            addNewTab(newNfa)
        }
        nfaeMenu.onAction= EventHandler<ActionEvent> {
            var newNfae = NFAE()
            addNewTab(newNfae)
        }
        newFile.items.addAll(dfaMenu,nfaMenu,nfaeMenu,regexMenu)
        val openFile = MenuItem("Open File")
        val exitApp = MenuItem("Exit")
        exitApp.onAction= EventHandler<ActionEvent> {
            System.exit(0)
        }
        file.items.addAll(newFile,openFile,SeparatorMenuItem(), exitApp)

        //Create SubMenu Edit.
        val edit = Menu("Edit")
        val properties = MenuItem("Properties")
        edit.items.add(properties)

        //Create SubMenu Edit.
        val convert = Menu("Convert")
        val toDFA = MenuItem("To DFA")
        toDFA.onAction= EventHandler<ActionEvent> {
            if(tabPane.selectionModel.selectedItem!=null){
                var automata = (tabPane.selectionModel.selectedItem as TabContainer).automaton
                when(automata){
                    is DFA -> { //showMessageDialog(null, "Your automaton is already a DFA", "Error", JOptionPane.ERROR_MESSAGE)
                         }
                    else -> { addNewTab(automata.toDFA().renameStates()) }
                }
            }

        }
        val toRegex = MenuItem("To Regex")
        toRegex.onAction= EventHandler<ActionEvent> {
            if(tabPane.selectionModel.selectedItem!=null){
                var automata = (tabPane.selectionModel.selectedItem as TabContainer).automaton
                var regex= automata.toRegex()
                //showMessageDialog(null, regex, "Regex", JOptionPane.INFORMATION_MESSAGE)
            }
        }
        val toMinimizedDFA = MenuItem("To minimized DFA")
        toMinimizedDFA.onAction= EventHandler<ActionEvent> {
            if(tabPane.selectionModel.selectedItem!=null){
                var automata = (tabPane.selectionModel.selectedItem as TabContainer).automaton
                addNewTab(automata.toMinimizedDFA().renameStates())
            }
        }
        convert.items.addAll(toDFA,toRegex,toMinimizedDFA)



        //Create SubMenu Examples.
        val examples = Menu("Examples")
        val dfaExamples = Menu("DFA Examples")
        val dfaFirstExample = MenuItem("Binary numbers that are multiples of 3")
        val dfaToMinimize = MenuItem("Dfa to minimize")
        dfaExamples.items.addAll(dfaFirstExample,dfaToMinimize)
        dfaFirstExample.onAction = EventHandler {
            var dfa= DFA()
            dfa.addState(automata.State("q0"))
            dfa.addState(automata.State("q1"))
            dfa.addState(automata.State("q2"))

            dfa.addTransition('0',"q0","q0")
            dfa.addTransition('1',"q0","q1")
            dfa.addTransition('0',"q1","q2")
            dfa.addTransition('1',"q2","q2")
            dfa.addTransition('0',"q2","q1")
            dfa.addTransition('1',"q1","q0")

            dfa.setInitialState("q0")
            dfa.setFinalState("q0")
            addNewTab(dfa)
        }
        dfaToMinimize.onAction = EventHandler {
            var dfa= DFA()
            dfa.addState(automata.State("a"))
            dfa.addState(automata.State("b"))
            dfa.addState(automata.State("c"))
            dfa.addState(automata.State("d"))
            dfa.addState(automata.State("e"))
            dfa.addState(automata.State("f"))

            dfa.addTransition('0',"a","b")
            dfa.addTransition('1',"a","c")
            dfa.addTransition('0',"b","a")
            dfa.addTransition('1',"b","d")
            dfa.addTransition('0',"c","e")
            dfa.addTransition('1',"c","f")
            dfa.addTransition('0',"d","e")
            dfa.addTransition('1',"d","f")
            dfa.addTransition('0',"e","e")
            dfa.addTransition('1',"e","f")
            dfa.addTransition('0',"f","f")
            dfa.addTransition('1',"f","f")


            dfa.setInitialState("a")
            dfa.setFinalState("d")
            dfa.setFinalState("c")
            dfa.setFinalState("e")
            addNewTab(dfa)
        }

        val nfaExamples = Menu("NFA Example")
        val nfaFirstExample = MenuItem("F Example")
        val nfaSecondExample = MenuItem("Strings starting with a's")
        val nfaThirdExample = MenuItem("S Example")
        nfaExamples.items.addAll(nfaFirstExample,nfaSecondExample,nfaThirdExample)
        nfaFirstExample.onAction = EventHandler {
            var nfa= NFA()
            nfa.addState(automata.State("0"))
            nfa.addState(automata.State("1"))
            nfa.addState(automata.State("2"))
            nfa.addState(automata.State("3"))
            nfa.addState(automata.State("4"))

            nfa.addTransition('a',"0","1")
            nfa.addTransition('a',"0","2")
            nfa.addTransition('a',"0","3")
            nfa.addTransition('b',"0","2")
            nfa.addTransition('b',"0","3")

            nfa.addTransition('a',"1","1")
            nfa.addTransition('a',"1","2")
            nfa.addTransition('b',"1","2")
            nfa.addTransition('b',"1","3")

            nfa.addTransition('b',"2","2")
            nfa.addTransition('b',"2","3")
            nfa.addTransition('b',"2","4")

            nfa.addTransition('b',"3","3")
            nfa.addTransition('b',"3","2")
            nfa.addTransition('a',"3","4")
            nfa.addTransition('b',"3","4")

            nfa.setInitialState("0")
            nfa.setFinalState("1")
            addNewTab(nfa)
        }
        nfaSecondExample.onAction = EventHandler {
            var nfa= NFA()
            nfa.addState(automata.State("0"))
            nfa.addState(automata.State("1"))
            nfa.addState(automata.State("2"))
            nfa.addState(automata.State("3"))

            nfa.addTransition('a',"0","1")
            nfa.addTransition('a',"0","2")
            nfa.addTransition('a',"1","1")
            nfa.addTransition('a',"1","2")
            nfa.addTransition('b',"2","3")
            nfa.addTransition('b',"2","1")
            nfa.addTransition('a',"3","1")
            nfa.addTransition('a',"3","2")

            nfa.setInitialState("0")
            nfa.setFinalState("0")
            nfa.setFinalState("1")
            addNewTab(nfa)
        }
        nfaThirdExample.onAction = EventHandler {
            var nfa= NFA()
            nfa.addState(automata.State("0"))
            nfa.addState(automata.State("1"))
            nfa.addState(automata.State("2"))
            nfa.addState(automata.State("3"))
            nfa.addState(automata.State("4"))

            nfa.addTransition('a',"2","1")
            nfa.addTransition('a',"1","0")
            nfa.addTransition('b',"2","3")
            nfa.addTransition('b',"3","4")
            nfa.addTransition('a',"2","2")
            nfa.addTransition('b',"2","2")

            nfa.setInitialState("2")
            nfa.setFinalState("0")
            nfa.setFinalState("4")
            addNewTab(nfa)
        }

        val nfaeExamples = Menu("NFA-e Examples")
        val nfaeFirstExample = MenuItem("Strings terminated with abb")
        val nfaeSecondExample = MenuItem("S Example")
        nfaeExamples.items.addAll(nfaeFirstExample,nfaeSecondExample)
        nfaeFirstExample.onAction = EventHandler {
            var nfae = NFAE()
            nfae.addState(automata.State("0"))
            nfae.addState(automata.State("1"))
            nfae.addState(automata.State("2"))
            nfae.addState(automata.State("3"))
            nfae.addState(automata.State("4"))
            nfae.addState(automata.State("5"))
            nfae.addState(automata.State("6"))
            nfae.addState(automata.State("7"))
            nfae.addState(automata.State("8"))
            nfae.addState(automata.State("9"))
            nfae.addState(automata.State("10"))

            nfae.addTransition('E',"0","1")
            nfae.addTransition('E',"1","2")
            nfae.addTransition('E',"1","4")
            nfae.addTransition('a',"2","3")
            nfae.addTransition('b',"4","5")
            nfae.addTransition('E',"3","6")
            nfae.addTransition('E',"5","6")
            nfae.addTransition('E',"6","7")
            nfae.addTransition('a',"7","8")
            nfae.addTransition('b',"8","9")
            nfae.addTransition('b',"9","10")
            nfae.addTransition('E',"0","7")
            nfae.addTransition('E',"6","1")

            nfae.setInitialState("0")
            nfae.setFinalState("10")
            addNewTab(nfae)
        }
        nfaeSecondExample.onAction = EventHandler {
            var nfae = NFAE()
            nfae.addState(automata.State("p"))
            nfae.addState(automata.State("q"))
            nfae.addState(automata.State("r"))

            nfae.addTransition('a',"p","p")
            nfae.addTransition('b',"p","q")
            nfae.addTransition('c',"p","r")

            nfae.addTransition('a',"q","q")
            nfae.addTransition('b',"q","r")
            nfae.addTransition('E',"q","p")

            nfae.addTransition('a',"r","r")
            nfae.addTransition('E',"r","q")
            nfae.addTransition('c',"r","p")

            nfae.setInitialState("p")
            nfae.setFinalState("r")
            addNewTab(nfae)
        }

        val regexExample = MenuItem("Regex Example")


        examples.items.addAll(dfaExamples,nfaExamples,nfaeExamples,regexExample)
        // /Create SubMenu Help.
        val help = Menu("Help")
        val visitWebsite = MenuItem("Visit Website")
        help.items.add(visitWebsite)

        mainMenu.menus.addAll(file, edit,convert,examples, help)

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

        root.top = topContainer
        root.center = tabPane

        var A = DFA()
        A.addState(State("q0"))
        A.addState(State("q1"))
        A.addTransition('1',"q0","q0")
        A.addTransition('0',"q0","q1")
        A.addTransition('1',"q1","q0")
        A.addTransition('0',"q1","q1")
        A.setInitialState("q0")
        A.setFinalState("q1")

        var B = DFA()
        B.addState(State("q0"))
        B.addState(State("q1"))
        B.addTransition('1',"q0","q1")
        B.addTransition('1',"q1","q1")
        B.addTransition('0',"q1","q1")
        B.setInitialState("q0")
        B.setFinalState("q1")

        var newDfa = A.union(B)
        addNewTab(newDfa)

//        tabPane.setOnContextMenuRequested({ e ->
////            val cell = (tabPane.selectionModel.selectedItem as TabContainer).graphComponent.getCellAt(e.x.toInt(), e.y.toInt())
////
////            if (cell is mxCell) {}
//            println(e.sceneX)
//            println(e.sceneY)
//                tabPane.selectionModel.selectedItem.contextMenu.show(tabPane, e.sceneX, e.sceneY)
//
//        })
//


//        graphComponent.connectionHandler.addListener(mxEvent.LABEL_CHANGED) { sender, evt ->
//            println("label change to=" + evt.name)
//            println(evt.properties.toString())
//        }


        // Creates an event handler for the button
        evaluateBtn.onMouseClicked = EventHandler<MouseEvent> {
            val alphabet = alphabetTextField.text ?:""
            val automataTab = tabPane.selectionModel.selectedItem

            if(automataTab!=null){
                try{
                    var result=(automataTab as TabContainer).automaton.evaluate(alphabet)
                    val alert = Alert(Alert.AlertType.INFORMATION)
                    alert.title = "Evaluation"
                    alert.headerText = "Evaluating input $alphabet"
                    alert.contentText = "Accepted: $result"

                    alert.showAndWait()
                    //showMessageDialog(null, "evaluation: $result")
                }catch (e: Exception){
                    //showMessageDialog(null, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                }
            }
        }

        val scene = Scene(root, Screen.getPrimary().bounds.width-200, Screen.getPrimary().bounds.height-200)

        //add on key release event to scene
        scene.onKeyPressed = EventHandler<KeyEvent> { e ->
            var tab = (tabPane.selectionModel.selectedItem as TabContainer)
            if(tab==null) {
            }else{
                try{
                    println("keyEvnet: ${e.code}")

                    val cell: mxCell = (tab.graph.selectionCell as mxCell)
                    if(cell.isVertex){
                        if (e.isAltDown && e.code === KeyCode.I) {
                            println("setting initial")
                            tab.setVertexStyle(cell,VertexType.INITIAL)

                        }else if (e.isAltDown && e.code === KeyCode.F) {
                            println("setting final")
                            tab.setVertexStyle(cell,VertexType.FINAL)
                        }else if (e.isAltDown && e.code === KeyCode.B) {
                            println("setting Both Initial and Final")
                            tab.setVertexStyle(cell,VertexType.INITIAL_FINAL)
                        }else if (e.isAltDown && e.code === KeyCode.N) {
                            println("setting normal")
                            tab.setVertexStyle(cell,VertexType.NORMAL)
                        }else if (e.code === KeyCode.DELETE) {
                            println("Deleting cell")
                            if(tab.automaton.removeState(cell.value.toString())){
                                tab.graph.update {
                                    for (edge in tab.graph.getEdges(cell)){
                                        tab.graph.model.remove(edge)
                                    }
                                    tab.graph.model.remove(cell)
                                }
                            }

                        }
                    }else if(cell.isEdge){
                        if (e.code === KeyCode.DELETE) {
                            println("Deleting edge")
                            tab.graph.update {
                                tab.graph.model.remove(cell)
                            }
                        }
                    }
                }catch (e:Exception){
                    //showMessageDialog(null, e.message, "Error",ERROR_MESSAGE)
                }
            }
            tab.graphComponent.refresh()
            e.consume()


        }



        //Setup the Stage.
        primaryStage.title = "Automatas"
        primaryStage.scene = scene
        primaryStage.show()


        //(dfa as NFAE).printClosure()

    }


    private fun mxGraph.update(block: () -> Any) {
        model.beginUpdate()
        try {
            block()
        }
        finally {
            model.endUpdate()
        }
    }
    private fun  addNewTab(automaton: IAutomata) {
        val tab = TabContainer(automaton,"new "+automaton.getClassName())
        tabPane.tabs.add(tab)
    }


}

