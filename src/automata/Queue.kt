package automata

/**
 * Created by lex on 07-31-16.
 */

class Queue<T>(){
    var items: MutableList<T> = mutableListOf()

    fun Queue(list: MutableList<T>){
        items = list
    }

    fun isEmpty():Boolean = this.items.isEmpty()

    fun isNotEmpty():Boolean = !isEmpty()

    fun count():Int = this.items.count()

    override fun toString()= this.items.toString()

    fun enqueue(element: T){
        this.items.add(element)
    }

    fun dequeue(): T? {
        if(this.isEmpty()){
            return null
        }else{
            return this.items.removeAt(0)
        }
    }

    fun peek():T?{
        return this.items[0]
    }
}