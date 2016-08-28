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

    var index=0
    var S:MutableList<State> = mutableListOf()

    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        if(symbol.equals('E'))
            throw Exception("Symbol E is not valid for a ${this.javaClass.simpleName}")
        var s = getState(source)
        var t = getState(target)
        var transition: Transition = Transition(symbol.toString(), s, t)
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
        var regex=""
        for(final in getFinalStates()){
            var clone = this.clone()
            for(cfinal in clone.getFinalStates()){
                if(final.value!=cfinal.value)
                    clone.removeFinalState(cfinal.value)
            }

            //First, if there are multiple edges from one node to other node, then these are unified into a single edge that contains the union of inputs.
            for (state in clone.states){
                var edgesToUnified = state.getTransitions().groupBy { it.target }.filter { it.value.count()>1 }

                for (group in edgesToUnified){
                    var target = group.key

                    var edge = group.value.first()
                    var toRemove = group.value.minus(edge)
                    for (e in toRemove){
                        edge.symbol+="+"+e.symbol
                        state.removeTransition(e)
                    }

                }
            }
            //Second, Remove circles and sub circles
//            for (state in clone.states){
//                if(state.index==-1){
//                    var sequencesToRemove = tarjan(state).filter { it.count()>1 }
//                    for (secuence in sequencesToRemove){
//                        println("SCC:"+secuence.map { it.value })
//                        for (stateToRemove in secuence){
//                            var transitionsIGointTo = stateToRemove.getTransitions()
//                            var transitionsPointingToMe = stateToRemove.getTransitionsPointingToMe()
//                            var kleeStart = ""
//                            var selfTransition = transitionsIGointTo.first{ it.target==stateToRemove }
//                            if(!=null){
//                                kleeStart=tr
//                            }
//
//                        }
//                    }
//                }
//            }
        }

        return regex
    }

    private fun tarjan(state: State): MutableList<MutableList<State>> {
        state.index=index
        state.lowlink = index
        index+=1

        S.add(state)

        var otherLists: MutableList<MutableList<State>> = mutableListOf()
        for (transition in state.getTransitions()){
            var target = transition.target
            if(target.index==-1){
                otherLists = tarjan(target)
                state.lowlink=Math.min(state.lowlink,target.lowlink)
            }else if(target in S){
                state.lowlink=Math.min(state.lowlink,target.index)
            }
        }

        var toRemove : MutableList<State> = mutableListOf()
        if(state.lowlink==state.index){
            do {
                var target = S.last()
                S.remove(target)
                toRemove.add(target)
            }while (S.count()>0  && state!=target)
            otherLists.add(toRemove)
        }


        return otherLists
    }

    override fun clone(): DFA {
        var clone = DFA()
        for(state in this.states){
            clone.addState(State(state.value))
        }

        for(state in this.states){
            for(transition in state.getTransitions()){
                clone.addTransition(transition.symbol.first(),transition.source.value,transition.target.value)
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
                    if(result!=null){
                        markedPairs.add(pair)
                        unmarkedPairs.remove(pair)
                    }
                }catch (e:Exception){
                    markedPairs.add(pair)
                    unmarkedPairs.remove(pair)
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

    private fun unify(A:DFA,B:DFA,op: AutomataOperation): DFA {
        A.renameStates('A')
        B.renameStates('B')
        var states: MutableList<MutableList<State>> = mutableListOf()
        var initial = mutableListOf(A.getInitialState(),B.getInitialState())
        states.add(initial)

        var automata = DFA()
        var initialStateName = initial.map { it.value }.sorted().toString().replace("[","").replace("]","").trim()
        automata.addState(State(initialStateName))
        automata.setInitialState(initialStateName)

        var acceptedLanguage = A.language.union(B.language)

        var iterate = states.listIterator()
        while (iterate.hasNext()){
            var group = iterate.next()
            for (symbol in acceptedLanguage){
                var targetGroup:MutableList<State> = mutableListOf()
                for (state in group){
                    try {
                        targetGroup.add(state.getTransition(symbol)!!.target)
                    }catch (e:Exception){

                    }
                }

                if(targetGroup.count()>0){
                    states.remove(group)

                    var sourceName = group.map { it.value }.sorted().toString().replace("[","").replace("]","").trim()
                    var targetName = targetGroup.map { it.value }.sorted().toString().replace("[","").replace("]","").trim()
                    if(!automata.hasState(targetName)){
                        automata.addState(State(targetName))
                        states.add(targetGroup)
                    }
                    when(op){
                        AutomataOperation.Union -> {
                            if((A.finals.intersect(targetGroup).count()>0) or (B.finals.intersect(targetGroup).count()>0)){
                                automata.setFinalState(targetName)
                            }
                        }
                        AutomataOperation.Intersect -> {
                            if((A.finals.intersect(targetGroup).count()>0) and (B.finals.intersect(targetGroup).count()>0)){
                                automata.setFinalState(targetName)
                            }
                        }
                        AutomataOperation.Subtract -> {
                            if((A.finals.intersect(targetGroup).count()>0)){
                                automata.setFinalState(targetName)
                            }
                        }

                    }

                    if(!automata.hasTransition(symbol,sourceName,targetName))
                        automata.addTransition(symbol,sourceName,targetName)
                    iterate = states.listIterator()
                }

            }
        }

        return automata
    }

    fun union(B:DFA): DFA{
        return unify(this,B, AutomataOperation.Union)
    }

    fun intersect(B:DFA): DFA{
        return unify(this,B, AutomataOperation.Intersect)
    }

    fun subtract(B:DFA): DFA{
        return unify(this,B, AutomataOperation.Subtract)
    }

    fun complement(): DFA{
        var complement = this.clone()

        for (state in complement.states){
            if(this.isFinal(state.value))
                complement.removeFinalState(state.value)
            else
                complement.setFinalState(state.value)
        }

        var sumidero: State? = null

        for (state in this.states){
            for (symbol in complement.language){
                if(state.getTransition(symbol)==null){
                    if(sumidero==null){
                        sumidero= State("drain")
                        complement.addState(sumidero)
                        complement.setFinalState(sumidero.value)
                        for (a in complement.language){
                            complement.addTransition(a,sumidero.value,sumidero.value)
                        }
                        complement.addTransition(symbol,state.value,sumidero.value)
                    }else{
                        complement.addTransition(symbol,state.value,sumidero.value)
                    }
                }
            }
        }

        return complement
    }
}

enum class AutomataOperation {
    Union,

    Subtract,

    Intersect

}

