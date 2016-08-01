package automata

import java.io.Serializable
import java.util.*

class NFA(): IAutomata, Serializable {
    override var language: MutableList<Char> = mutableListOf()
    override var states: MutableList<State> = mutableListOf()
    override var initial: State? = null
    override var finals: MutableList<State> = mutableListOf()

    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        var s = getState(source)
        var f = getState(target)
        var ts = s.getTransitions(symbol)
        for (t in ts){
            if(t!=null){
                if(t.source.value==source && t.target.value==target){
                    throw Exception("Transition from ${s.value} to ${f.value} with symbol $symbol already exist!")
                }
            }
        }

        var transition: Transition = Transition(symbol, s, f)
        if(!language.contains(symbol))
            language.add(symbol)
        return s.addTransition(transition)
    }

    override fun evaluate(alphabet: String): Boolean {
        var init = getInitialState()
        val result: List<State> = deltaExtended(init,alphabet)
        return !getFinalStates().intersect(result).isEmpty()
    }

    private fun deltaExtended(q: State, alphabet: String): List<State> {
        if(alphabet.isEmpty()){
            return listOf(q)
        }

        var a = alphabet.last()
        var x = alphabet.subSequence(0,alphabet.length-1).toString()

        //println("x: $x a:$a")

        var subset = deltaExtended(q,x)

        var result : MutableList<State> = mutableListOf()

        for (q in subset){
            var delta = delta(q,a)
            result = result.union(delta).toMutableList()
        }
        //println("DeltaExtended ${q.value},$x:")
//        for (st in result){
//            println("${st.value}")
//        }
        return result
    }

    private fun delta(q: State, symbol: Char): List<State> {
        if (q.getTransition(symbol)!=null){
            //println("Delta ${q.value},$symbol:")
            var qs = q.getTransitions(symbol).map{ it.target }

            return qs
        }
        else{
            //println("Delta $symbol: {}")
            return listOf()
        }

    }

//    fun printTable(){
//        var table: List<Pair<String,String>>
//        for (state in states){
//            print("${state.value}|")
//            for (symbol in language){
//                var transitions = state.getTransitions(symbol).map{ it.target.value }.sorted()
//                if(transitions.count()==0) continue
//                var conjunto = transitions.toString().replace("[","").replace("]","").trim()
//                print("$conjunto|")
//            }
//            print("\n")
//        }
//    }

    fun toDFA(): DFA{
        var dfa = DFA()
        var queue: Queue<State> = Queue()
        var initial = State(this.getInitialState().value.toString())
        queue.enqueue(initial)
        dfa.addState(initial)
        dfa.setInitialState(initial.value)
        if (this.isFinal(initial.value)){
            dfa.setFinalState(initial.value)
        }
        while (queue.isNotEmpty()){
            var q = queue.dequeue()
            if(q!=null) {
                println("State: ${q.value}| ")
                for (symbol in language){
                    var newStateName = getTargetsName(q.value,symbol)
                    if(newStateName!=null)
                        println("$symbol : $newStateName| ")
                    if(newStateName!=null && newStateName.isNotEmpty()){
                        if(!dfa.hasState(newStateName)){
                            var newState = State(newStateName)
                            dfa.addState(newState)
                            queue.enqueue(newState)
                        }

                        if(!hasTransition(symbol,q.value,newStateName)){
                            dfa.addTransition(symbol,q.value,newStateName)
                        }

                        for (name  in newStateName.split(",")){
                            if (this.isFinal(name.trim())){
                                dfa.setFinalState(newStateName)
                                break
                            }
                        }

                    }
                }
                print("\n")
            }
        }
        return dfa
    }

    private fun getTargetsName(states: String, symbol: Char): String?{
        var transitions: MutableSet<String> = mutableSetOf()
        for (name  in states.split(",")){
            if(hasState(name.trim())){
                var state = this.getState(name.trim())
                val mutableList = state.getTransitions(symbol).map { it.target.value }
                transitions.addAll(mutableList)
            }
        }
        if(transitions.count()==0) return null
        return transitions.sorted().toString().replace("[","").replace("]","").trim()
    }
}