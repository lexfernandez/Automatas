package automata

import java.io.Serializable

class NFA(): IAutomata, Serializable {
    override fun toMinimizedDFA(): DFA {
        return this.toDFA().toMinimizedDFA()
    }

    override fun toRegex(): String {
        return this.toDFA().toRegex()
    }

    override var language: MutableList<Char> = mutableListOf()
    override var states: MutableList<State> = mutableListOf()
    override var initial: State? = null
    override var finals: MutableList<State> = mutableListOf()

    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        if(symbol.equals('E'))
            throw Exception("Symbol E is not valid for a ${this.javaClass.simpleName}")
        val s = getState(source)
        val f = getState(target)
        val ts = s.getTransitions(symbol)
        for (t in ts){
            if(t.source.value==source && t.target.value==target){
                throw Exception("Transition from ${s.value} to ${f.value} with symbol $symbol already exist!")
            }
        }
        val transition: Transition = Transition(symbol.toString(), s, f)
        addLanguageSymbol(symbol)
        f.addTransitionPointingToMe(transition)
        return s.addTransition(transition)
    }

    override fun evaluate(alphabet: String): Boolean {
        println("NFA Evaluation")
        val init = getInitialState() ?: throw Exception("Initial state is not set")
        val result: List<State> = deltaExtended(init,alphabet)
        return !getFinalStates().intersect(result).isEmpty()
    }

    private fun deltaExtended(q: State, alphabet: String): List<State> {
        if(alphabet.isEmpty()){
            return listOf(q)
        }

        val a = alphabet.last()
        val x = alphabet.subSequence(0,alphabet.length-1).toString()

        //println("x: $x a:$a")

        val subset = deltaExtended(q,x)

        var result : MutableList<State> = mutableListOf()

        subset.forEach { q ->
            val delta = delta(q,a)
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
            val qs = q.getTransitions(symbol).map{ it.target }

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

    override fun toDFA(): DFA{
        val dfa = DFA()
        val queue: Queue<State> = Queue()
        val init = getInitialState() ?: throw Exception("Initial state is not set")
        val initial = State(init.value.toString())
        queue.enqueue(initial)
        dfa.addState(initial)
        dfa.setInitialState(initial.value)
        if (this.isFinal(initial.value)){
            dfa.setFinalState(initial.value)
        }
        while (queue.isNotEmpty()){
            val q = queue.dequeue()
            if(q!=null) {
                //println("State: ${q.value}| ")
                for (symbol in language){
                    val newStateName = getTargetsName(q.value,symbol)
                    if(newStateName!=null && newStateName.isNotEmpty()){
                        if(!dfa.hasState(newStateName)){
                            val newState = State(newStateName)
                            dfa.addState(newState)
                            queue.enqueue(newState)
                        }

                        if(!dfa.hasTransition(symbol,q.value,newStateName)){
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
                //print("\n")
            }
        }
        return dfa
    }

    private fun getTargetsName(states: String, symbol: Char): String?{
        val transitions: MutableSet<String> = mutableSetOf()
        for (name  in states.split(",")){
            if(hasState(name.trim())){
                val state = this.getState(name.trim())
                val mutableList = state.getTransitions(symbol).map { it.target.value }
                transitions.addAll(mutableList)
            }
        }
        if(transitions.count()==0) return null
        return transitions.sorted().toString().replace("[","").replace("]","").trim()
    }
}