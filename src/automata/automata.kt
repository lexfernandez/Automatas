package automata

import java.util.*

/**
 * Created by lex on 07-26-16.
 */

interface Automata {
    var states: HashMap<String, State>
    var initial: String
    var finals: MutableList<String>

    fun addState(state: State): State? {
        if(states[state.value]!=null)
            throw Exception("El estado ya existe")
        return states.put(state.value,state)
    }

    fun getState(value: String): State? {
        return states[value]?: null
    }
}