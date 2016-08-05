import automata.IAutomata
import dk.brics.automaton.Automaton
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.Tab

/**
 * Created by lex on 08-05-16.
 */


class TabContainer(automaton: IAutomata,text: String? = "new tab") : Tab(text) {


    override fun getOnCloseRequest(): EventHandler<Event> {
        if(this.tabPane.tabs.count()>1) return super.getOnCloseRequest()
        return
    }
}