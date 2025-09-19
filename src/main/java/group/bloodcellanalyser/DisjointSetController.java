package group.bloodcellanalyser;

public class DisjointSetController<T> {


    //Find the root of node
    public DisjointSetNode<T> findA(DisjointSetNode<T> node){
        while (node.getParent()!=null){
            node = node.getParent();
        }
        return node;
    }

    public DisjointSetNode<T> find(DisjointSetNode<T> node){
        if (node.getParent() == null) {
            return node;
        }
        node.setParent(find(node.getParent()));
        return node.getParent();
    }


    //quickly union by just getting A's root and setting it's parent as b (makes really long(height) sets
    public void quickUnion(DisjointSetNode<T> nodeA, DisjointSetNode<T> nodeB){
        find(nodeA).setParent(nodeB);
    }


    public void unionByHeight(DisjointSetNode<T> nodeA, DisjointSetNode<T> nodeB){
        DisjointSetNode<T> rootA = find(nodeA);
        DisjointSetNode<T> rootB = find(nodeB);

        if (rootA == rootB) {
            return; // Already in the same set
        }



        //deeperRoot is| check statement for true/false | if true = rootA, false= rootB
        // syntax (Variable) = (expression) ? expressionTrue : expressionFalse
        DisjointSetNode<T> deeperRoot = rootA.getHeight()>=rootB.getHeight() ? rootA : rootB;
        //Check if deeper root is rootA ? if true shallow is rootB, if false shallow is rootA
        DisjointSetNode<T> shallowRoot = deeperRoot==rootA ? rootB : rootA;

        shallowRoot.setParent(deeperRoot);
        if(deeperRoot.getHeight()==shallowRoot.getHeight()){
            deeperRoot.setHeight(deeperRoot.getHeight()+1);
        }
    }

    public void unionBySize (DisjointSetNode<T> nodeA, DisjointSetNode<T> nodeB){
        DisjointSetNode<T> rootA = find(nodeA);
        DisjointSetNode<T> rootB = find(nodeB);

        if (rootA == rootB) {
            return; // Already in the same set
        }

        DisjointSetNode<T> biggerRoot=rootA.getSize()>=rootB.getSize() ? rootA : rootB;
        DisjointSetNode<T> smallerRoot= biggerRoot==rootA ? rootB : rootA;
        smallerRoot.setParent(biggerRoot);
        biggerRoot.setSize(biggerRoot.getSize()+smallerRoot.getSize());
    }


}
