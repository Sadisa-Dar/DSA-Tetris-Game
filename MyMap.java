public class MyMap<K, V extends Comparable<V>> {
    
    private Node<K, V> head;
    private Node<K, V> tail;
    private int size;

    public MyMap() { 
        head = tail = null; 
        size = 0; 
    }

    public void putSortedDescending(K key, V value) {
        Node<K, V> newNode = new Node<>(key, value);

        if (head == null) {
            head = tail = newNode;
            size++;
            return;
        }

        Node<K, V> current = head;
        Node<K, V> prev = null;

        while (current != null) {
            if (value.compareTo(current.value) > 0) { 
                if (prev == null) { 
                    head = newNode;
                    newNode.next = current; 
                }
                else { 
                    prev.next = newNode;
                    newNode.next = current; 
                }
                size++;
                return;
            }
            prev = current;
            current = current.next;
        }

        tail.next = newNode;
        tail = newNode;
        size++;
    }

    public void deleteLast(){
        if(head == null) {
            return;
        }
        if(head == tail){
            head = null;
            tail = null;
            size--;
            return;
        }

        Node<K,V> current = head;
        while(current.next != tail){
            current = current.next;
        }
        current.next = null;
        tail = current;
        size--;
    }

    public int size() { 
        return size; 
    }

    public Node<K, V> getHead() {
        return head; 
    }
    
    public void putLast(K key, V value) {
        Node<K, V> current = head;
        while (current != null) {
            if(current.key != null && current.key.equals(key)){
                current.value = value; 
                return;
            }
            current = current.next;
        }
        addLast(key, value);
    }
    public void putFirst(K key, V value) {
        Node<K, V> current = head;
        while (current != null) {
            if(current.key != null && current.key.equals(key)){
                current.value = value; 
            }
            current = current.next;
        }
        addFirst(key, value);
    }

    public void addLast(K key, V value) {
        Node<K,V> newNode = new Node<>(key, value);

        if(head == null){
            head = newNode;
            tail = newNode;
        }
        else{
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public void addFirst(K key, V value) {
        Node<K,V> newNode = new Node<>(key,value);

        if(head == null){
            head = newNode;
            tail = newNode;
        }
        else{
            newNode.next = head;
            head = newNode;
        }
        size++;
    }

    public void deleteFirst(){
        if(head == null) return;

        if(head == tail){
            head = null;
            tail = null;
        }
        else{
            head = head.next;
        }
        size--;
    }

    public V get(K key){
        Node<K, V> current = head;
        while (current != null) {
            if (current.key != null && current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    public V getFirstValue(){
        if (head == null){
            return null;
        }
        return head.value;
    }

    public V getLast(){
        if (tail == null){
            return null;
        }
        return tail.value;
    }
    public boolean containsKey(K key){
        Node<K, V> current = head;
        while (current != null){
            if (current.key != null && current.key.equals(key)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
    public void printSize(){
        System.out.println("Size = " + size);
    }


    public void print() {
        Node<K, V> current = head;
        while (current != null) {
            System.out.println(current.key + " -> " + current.value);
            current = current.next;
        }
    }
}
