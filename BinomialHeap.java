/**
 * BinomialHeap
 *
 * An implementation of binomial heap over non-negative integers.
 *
 */
public class BinomialHeap
{
	public int size;
	public int numTrees;
	public HeapNode last;
	public HeapNode min;

	/**
	 * 
	 * pre: key > 0
	 *
	 * Insert (key,info) into the heap and return the newly generated HeapItem.
	 *
	 * Time-Complexity: O(log(n = size of heap)) - only constant number of operations
	 * which are O(1) EXCEPT this.meld which costs O(log(n+1)) = O(log(n)).
	 *
	 */
	public HeapItem insert(int key, String info) {
		//Initialize temporary-heap with the desired element
		HeapNode node = new HeapNode();
		HeapItem item = new HeapItem(node,key,info);
		node.setItem(item);
		BinomialHeap temp = new BinomialHeap();
		temp.size = 1;
		temp.numTrees = 1;
		temp.min = node;
		temp.last = node;
		temp.last.setNext(node);

		//meld temporary-heap to the original
		this.meld(temp);

		return item;
	}

	/**
	 * 
	 * Delete the minimal item
	 *
	 * Time-Complexity: O(log(n = size of heap)) - each step prior to melding
	 * costs <= O(log(n)) due to the properties of binomial heap as discussed in class,
	 * the melding operation costs O(log(n)) as well hence total cost is O(log(n)).
	 *
	 */
	public void deleteMin() {
		if (this.size() <= 1){ //if heap is empty or of size 1 - reset
			this.reset(); //implementation below in 'private methods'
			return;
		}

		//physically detach min node from all its children
		HeapNode child = this.min.getChild();
		HeapNode p = child;
		this.min.setChild(null);
		if(p != null){
			do{
				p.setParent(null);
				p = p.getNext();
			} while(p != child);
		}

		//update current heap's min,last,size,numTrees
		//and physically detach min node from the tree list
		int minRank = this.min.getRank();
		this.size -= (int)Math.pow(2,minRank); //update size
		this.numTrees -= 1; //update numTrees
		if(this.last.getNext() == this.min){ //handle case which min is first
			this.last.setNext(this.last.getNext().getNext());
			this.min.setNext(null);
		}
		//update min and detach old min from tree list
		if(numTrees == 0){ //if there was only one tree - heap resets before melding with children
			this.reset();
		}
		else{
			p = this.last.getNext();
			HeapNode oldMin = this.min;
			this.min = this.last.getNext();
			do {
				if (p.getNext() == oldMin) {
					p.setNext(oldMin.getNext());
					oldMin.setNext(null);
					if(oldMin == this.last){ //if min was also last then we shall make its prev the new last
						this.last = p;
					}
				} if (p.getNext().getItem().getKey() < this.min.getItem().getKey()) {
					this.min = p.getNext();
				}
				p = p.getNext();
			}while(p != this.last);
		}

		if(child == null) //if there was nothing under the min - we are done
			return;
		//make new heap and set min's children as its trees
		BinomialHeap heap = new BinomialHeap();
		heap.last = child;
		heap.size = (int)Math.pow(2,minRank) - 1;
		heap.numTrees = minRank;
		//determine new heap's min
		p = heap.last.getNext();
		heap.min = heap.last.getNext();
		do {
			if(p.getNext().getItem().getKey() < heap.min.getItem().getKey()){
				heap.min = p.getNext();
			}
			p = p.getNext();
		}while(p != heap.last);

		//finally - meld new heap with current heap
		this.meld(heap);
	}

	/**
	 * 
	 * Return the minimal HeapItem
	 *
	 * Time-Complexity: O(1) - constant number of O(1) operations.
	 */
	public HeapItem findMin() { return this.min.getItem(); }

	/**
	 * 
	 * pre: 0<diff<item.key
	 * 
	 * Decrease the key of item by diff and fix the heap. 
	 * 
	 * Time-Complexity: O(log(n = size of heap)) - we travel along 
	 * the entire height of a single binomial tree, and as discussed
	 * in class, such procedure costs O(log(n)).
	 */
	public void decreaseKey(HeapItem item, int diff) {
		//physically set new key
		int newKey = item.getKey() - diff;
		item.setKey(newKey);

		//perform 'Heapify-up'
		String info = item.getInfo();
		HeapNode node = item.getNode();
		HeapNode parent = node.getParent();
		while(parent != null){
			int parentKey = parent.getItem().getKey();
			String parentInfo = parent.getItem().getInfo();
			if(parentKey > newKey){
				//switch parent and current node
				parent.getItem().setKey(newKey);
				node.getItem().setKey(parentKey);
				parent.getItem().setInfo(info);
				node.getItem().setInfo(parentInfo);
			}
			else{
				break;
			}
			node = parent;
			parent = parent.getParent();
		}

		//update minimum
		if(this.min.getItem().getKey() > newKey)
			this.min = node;
	}

	/**
	 * 
	 * Delete the item from the heap.
	 *
	 * Time-Complexity: O(log(n)) - both decreaseKey and deleteMin work in O(log(n))
	 */
	public void delete(HeapItem item) {
		//make item the new min
		int diff = item.getKey() + 1;
		this.decreaseKey(item, diff);
		//delete min
		this.deleteMin();
	}

	/**
	 * 
	 * Meld the heap with heap2
	 *
	 * Time-Complexity: O(log(n = size of heaps)) - STEP 1 (combining the lists) costs O(k) where k
	 * is the number of trees in both heaps [similar analysis to combining linked lists], STEP 2 costs the
	 * same since the analysis is similar to adding binary numbers.
	 * and by properties of Binomial Heaps we know that O(k) = O(log(n)).
	 *
	 */
	public void meld(BinomialHeap heap2) {
		//get pointers to each list of trees
		HeapNode p1 = this.last;
		HeapNode p2 = heap2.last;

		//if our heap is empty - just copy heap2
		if(p1 == null){
			this.last = heap2.last;
			this.min = heap2.min;
			this.size = heap2.size();
			this.numTrees = heap2.numTrees();
			return;
		}
		//if heap2 is empty then we shall not do anything
		if(p2 == null){
			return;
		}

		//update minimum, size and numTrees
		if(heap2.min.getItem().getKey() < this.min.getItem().getKey()){
			this.min = heap2.min;
		}
		this.size += heap2.size();
		this.numTrees += heap2.numTrees(); //may change later upon linking

		//STEP 1 - combine lists of trees in ascending order
		p1 = this.last.getNext();
		p2 = heap2.last.getNext();
		this.last.setNext(null);
		heap2.last.setNext(null);
		HeapNode first = mergeTwoLists(p1,p2); //implementation below in 'private methods'
		HeapNode last = first;
		while(last.getNext() != null){
			last = last.getNext();
		}

		//STEP 2 - link same-degree trees
		HeapNode prev = null;
		HeapNode curr = first;
		HeapNode next = curr.getNext();
		while(next != null){
			//prioritize linking the later couple in order to maintain order of list
			//notice we cannot skip any linking since there can only be at most 3 trees with the same rank in the list
			if((next.getNext() != null && next.getNext().getRank() == curr.getRank()) || curr.getRank() != next.getRank()){
				prev = curr;
				curr = next;
			}
			else{ //now we know we can link curr and next and there are no links with higher priority
				curr = link(curr,next,next.getNext()); //implementation of 'link' below in 'private methods'
				this.numTrees -= 1; //adjust numTrees since linking 'removes' one tree
				if(prev == null){
					first = curr; //adjust first in case the first tree got linked
				}
				else{
					prev.setNext(curr); //link previous tree with new root
				}
				if(next == last){ //adjust last in case the last tree got linked
					last = curr;
				}
			}
			next = curr.getNext();
		}

		//maintain circularity and update heap
		last.setNext(first);
		this.last = last;
	}

	/**
	 * 
	 * Return the number of elements in the heap
	 *
	 * Time-Complexity: O(1) - constant number of O(1) operations.
	 */
	public int size() { return this.size; }

	/**
	 * 
	 * The method returns true if and only if the heap
	 * is empty.
	 *
	 * Time-Complexity: O(1) - constant number of O(1) operations.
	 */
	public boolean empty() { return (this.size == 0); }

	/**
	 * 
	 * Return the number of trees in the heap.
	 *
	 * Time-Complexity: O(1) - constant number of O(1) operations.
	 */
	public int numTrees() { return numTrees; }


	/**
	 * Class implementing a node in a Binomial Heap.
	 *  
	 */
	public class HeapNode{
		public HeapItem item;
		public HeapNode child;
		public HeapNode next;
		public HeapNode parent;
		public int rank;

		//constructors
		public HeapNode(){
			this.item = null;
			this.child = null;
			this.next = null;
			this.parent = null;
			this.rank = 0;
		}

		public HeapNode(HeapItem item){
			this();
			this.item = item;
		}

		//setters
		private void setItem(HeapItem item){this.item = item;}

		private void setChild(HeapNode child){this.child = child;}

		private void setNext(HeapNode next){this.next = next;}

		private void setParent(HeapNode parent){this.parent = parent;}

		private void setRank(int rank){this.rank = rank;}

		//getters
		private HeapItem getItem(){return this.item;}

		private HeapNode getChild(){return this.child;}

		private HeapNode getNext(){return this.next;}

		private HeapNode getParent(){return this.parent;}

		private int getRank(){return this.rank;}
	}

	/**
	 * Class implementing an item in a Binomial Heap.
	 *  
	 */
	public class HeapItem{
		public HeapNode node;
		public int key;
		public String info;

		//constructors
		public HeapItem(){
			this.node = null;
			this.key = 0;
			this.info = null;
		}

		public HeapItem(HeapNode node, int key, String info){
			this.node = node;
			this.key = key;
			this.info = info;
		}

		//setters
		private void setNode(HeapNode node){this.node = node;}

		private void setKey(int key){this.key = key;}

		private void setInfo(String info){this.info = info;}

		//getters
		private HeapNode getNode(){return this.node;}

		private int getKey(){return this.key;}

		private String getInfo(){return this.info;}

	}


	//Private methods:
	/**
	 * Links 2 binomial trees of the same rank
	 * to form a binomial tree of +1 rank.
	 *
	 * Time-Complexity: O(1) - constant number of O(1) operations.
	 */
	private HeapNode link(HeapNode node1, HeapNode node2, HeapNode nextTree){
		//determine which should be the root
		HeapNode root = node1;
		HeapNode child = node2;
		if(node1.getItem().getKey() > node2.getItem().getKey()){
			root = node2;
			child = node1;
		}
		//link both nodes
		HeapNode prevChild = root.getChild();
		HeapNode first = (prevChild != null) ? prevChild.getNext() : child;
		root.setChild(child);
		child.setNext(first);
		if (prevChild != null)
			prevChild.setNext(child);
		child.setParent(root);
		root.setRank(root.getRank() + 1);
		root.setNext(nextTree);
		return root;
	}

	/**
	 * Resets the heap
	 *
	 * Time-complexity: O(1) - constant number of
	 * O(1) operations.
	 */
	private void reset(){
		this.size = 0;
		this.last = null;
		this.min = null;
		this.numTrees = 0;
	}

	/**
	 * Merges two sorted linked lists
	 *
	 * Time-Complexity: O(n = size of both lists) - traversing each list once.
	 */
	private static HeapNode mergeTwoLists(HeapNode list1, HeapNode list2){
		if(list1 != null && list2 != null){
			if(list1.getRank() < list2.getRank()){
				list1.setNext(mergeTwoLists(list1.getNext(),list2));
				return list1;
			}
			else{
				list2.setNext(mergeTwoLists(list1,list2.getNext()));
				return list2;
			}
		}
		return (list1 == null) ? list2 : list1;
	}
}
