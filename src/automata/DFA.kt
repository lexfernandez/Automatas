package automata

import java.io.Serializable

/**
 * Created by Alex Fernandez on 07/25/2016.
 */

open class DFA(): IAutomata, Serializable,Cloneable {


    override var language: MutableList<Char> = mutableListOf()
    override var states: MutableList<State> = mutableListOf()
    override var initial: State? = null
    override var finals: MutableList<State> = mutableListOf()


    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        if(symbol.equals('E'))
            throw Exception("Symbol E is not valid for a ${this.javaClass.simpleName}")
        var s = getState(source)
        var t = getState(target)
        var transition: Transition = Transition(symbol, s, t)
        if(s.getTransition(symbol)!=null)
            throw Exception("Transition from ${s.value} with symbol $symbol already exist!")
        else{
            addLanguageSymbol(symbol)
            t.addTransitionPointingToMe(transition)
            return s.addTransition(transition)
        }
    }

    override fun evaluate(alphabet: String): Boolean {
        if(this.states.count()==0) return false
        println("DFA Evaluation")
        var init = getInitialState()
        val result: State = deltaExtended(init,alphabet)
        return getFinalStates().contains(result)
    }

    private fun deltaExtended(q: State, alphabet: String): State {
        if(alphabet.isEmpty()){
            return q
        }

        var a = alphabet.last()
        var x = alphabet.subSequence(0,alphabet.length-1).toString()

        //println("x: $x a:$a")
        return delta(deltaExtended(q,x),a)
    }

    private fun delta(q: State, symbol: Char): State {
        if (q.getTransition(symbol)!=null)
            return q.getTransition(symbol)!!.target
        else
            throw Exception("Transition dont exits")
    }



    override fun toDFA(): DFA {
        return this
    }

    override fun toRegex(): String {
        println(this)
        println(this.finals.map { it.value })
        var regex=""
        for(final in getFinalStates()){
            var clone = this.clone()
            println(clone)
            for(cfinal in clone.getFinalStates()){
                if(final.value!=cfinal.value)
                    clone.removeFinalState(cfinal.value)
            }

            for (state in clone.states){
                //si el estado no es inicial y no final
//                if(!clone.isFinal(state.value) && clone.getInitialState().value!=state.value ){
//                    //removerlo
//                    var transitionsPointingToMe = this.getState(state.value).getTransitionsPointingToMe()
//                    var transitionsIPointTo = this.getState(state.value).getTransitions()
//                    transitionsIPointTo.filter { it.target.value= }
//                    if(clone.removeState(state.value)){
//                        for(fromT in transitionsPointingToMe){
//                            for (toT in transitionsIPointTo){
//                                //fromT.
//                            }
//                        }
//                    }
//                }

            }
            println(clone.initial?.value)
            println(clone.finals.map { it.value })
        }

        return regex
    }

    override fun clone(): DFA {
        var clone = DFA()
        for(state in this.states){
            clone.addState(State(state.value))
        }

        for(state in this.states){
            for(transition in state.getTransitions()){
                clone.addTransition(transition.symbol,transition.source.value,transition.target.value)
            }
        }

        clone.setInitialState(this.getInitialState().value)

        for(state in this.getFinalStates()){
            clone.setFinalState(state.value)
        }

        return clone
    }

    override fun toMinimizedDFA(): DFA {
        var pairsTable:MutableList<Pair<State,State>> = mutableListOf()
        var markedPairs:MutableList<Pair<State,State>> = mutableListOf()
        var unmarkedPairs:MutableList<Pair<State,State>> = mutableListOf()


        //Step 1
        var visitedStates:MutableList<State> = mutableListOf()
        for (qi in this.states){
            for (qj in this.states.filter { !visitedStates.contains(it) }){
                if(qi!=qj){
                    pairsTable.add(Pair(qi,qj))
                }
            }
            visitedStates.add(qi)
        }


        //Step 2
        for (pair in pairsTable){

            if(this.isFinal(pair.first.value) and !this.isFinal(pair.second.value)){
                markedPairs.add(pair)
            }else if(this.isFinal(pair.second.value) and !this.isFinal(pair.first.value)){
                markedPairs.add(pair)
            }else{
                unmarkedPairs.add(pair)
            }
        }

        //Step 3
        var up:MutableList<Pair<State,State>> = mutableListOf()
        up=up.union(unmarkedPairs).toMutableList()
        var iterate = up.listIterator()
        while (iterate.hasNext()){
            var pair=iterate.next()
            for (a in this.language){
                try {
                    var A=delta(pair.first,a)
                    var B=delta(pair.second,a)
                    val list = markedPairs.filter { (it.first.value == A.value && it.second.value == B.value) }
                    var result=list.firstOrNull()
                    println("(${pair.first.value},${pair.second.value}):$a ==>(${A.value}.${B.value}):${result!=null}")
                    if(result!=null){
                        markedPairs.add(pair)
                        unmarkedPairs.remove(pair)
                    }
                }catch (e:Exception){

                }

            }
            iterate.remove()
        }


        //Step 4
        var newStates: MutableList<MutableList<State>> = mutableListOf()

        for (pair in unmarkedPairs){
            newStates.add(pair.toList().toMutableList())
        }

        var toAddLater:MutableList<MutableList<State>> = mutableListOf()
        for (state in this.states){
            var toCombine = newStates.filter { it.contains(state) }
            if(toCombine.count()>1){
                //Combine
                newStates.removeAll(toCombine)
                var states: MutableList<State> = toCombine.flatten().toMutableList()
                newStates.add(states)
            }else if(toCombine.count()==0){
                toAddLater.add(mutableListOf(state))
            }
        }

        newStates.addAll(toAddLater)
        //Create minimized DFA
        var minimizedDfa:DFA = DFA()
        for (states in newStates){
            minimizedDfa.addState(State(states.map { it.value }.sorted().toString().replace("[","").replace("]","").trim()))
        }

        for (states in newStates){
            for (source in states){
                if(states.contains(this.getInitialState())){
                    minimizedDfa.setInitialState(states.map { it.value }.sorted().toString().replace("[", "").replace("]", "").trim())
                }
                if(this.isFinal(source.value)){
                    minimizedDfa.setFinalState(states.map { it.value }.sorted().toString().replace("[", "").replace("]", "").trim())
                }

                for (symbol in this.language){
                    try {
                        var destiny = delta(source, symbol)
                        var msource = states.map { it.value }.sorted().toString().replace("[", "").replace("]", "").trim()
                        var targets = newStates.filter { it.contains(destiny) }

                        for (target in targets){
                            var mdestiny = target.map { it.value }.sorted().toString().replace("[", "").replace("]", "").trim()
                            minimizedDfa.addTransition(symbol,msource,mdestiny)
                        }
                    } catch(e: Exception) {

                    }
                }
            }
        }




        return minimizedDfa
    }


    fun renameStates(prefix:Char='q'): DFA {
        var i=0
        for (state in this.states){
            state.value= "$prefix${i.toString()}"
            i++
        }

        return this
    }
}

