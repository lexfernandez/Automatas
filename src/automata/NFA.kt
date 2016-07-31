package automata

class NFA(): DFA() {
    override fun addTransition(symbol: Char, source: String, target: String): Boolean {
        var s = getState(source)
        var f = getState(target)
        var t = s.getTransition(symbol)
        if(t!=null){
            if(t.source==s && t.target==f){
                throw Exception("Transition from ${s.value} to ${f.value} with symbol $symbol already exist!")
            }
        }
        var transition: Transition = Transition(symbol, s, f)
        return s.addTransition(transition)
    }

    override fun evaluate(alphabet: String): Boolean {
        val result: List<State> = deltaExtended(initial!!,alphabet)
        return !finals.intersect(result).isEmpty()
    }

    private fun deltaExtended(q: State, alphabet: String): List<State> {
        if(alphabet.isEmpty()){
            return listOf(q)
        }

        var a = alphabet.last()
        var x = alphabet.subSequence(0,alphabet.length-1).toString()

        println("x: $x a:$a")

        var subset = deltaExtended(q,x)

        var result : MutableList<State> = mutableListOf()

        for (q in subset){
            var delta = delta(q,a)
            result = result.union(delta).toMutableList()
        }
        for (st in result){
            println("${st.value}")
        }
        return result
    }

    private fun delta(q: State, symbol: Char): List<State> {
        if (q.getTransition(symbol)!=null){
            var qs = q.getTransitions(symbol).map{ it.target }
            println("Delta $symbol: $qs")
            return qs
        }
        else{
            println("Delta $symbol: {}")
            return listOf()
        }

    }
}