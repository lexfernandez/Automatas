package automata

class NFA(): DFA() {
    override fun addTransition(symbol: Char, source: String, target: String): Transition? {
        var s = getState(source) ?: throw IllegalArgumentException("source not exist")
        getState(target) ?: throw IllegalArgumentException("target not exist")
        var transition: Transition = Transition(symbol, source, target)
        if(s.getTransition(symbol)!=null)
            throw Exception("transition alredy exist")
        else
            return s.addTransition(transition)
    }

    override fun evaluate(alphabet: String): Boolean {
        val result: State = deltaExtended(states[initial]!!,alphabet)
        return finals.contains(result.value)
    }

    private fun deltaExtended(q: State, alphabet: String): State {
        if(alphabet.isEmpty()){
            return q
        }

        var a = alphabet.last()
        var x = alphabet.subSequence(0,alphabet.length-1).toString()

        println("x: $x a:$a")
        return delta(deltaExtended(q,x),a)
    }

    private fun delta(q: State, symbol: Char): State {
        if (q.getTransition(symbol)!=null)
            return getState(q.getTransition(symbol)!!.target)!!
        else
            throw Exception("Transition dont exits")
    }
}