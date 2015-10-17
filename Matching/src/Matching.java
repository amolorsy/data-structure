import java.io.*;

public class Matching {
    public static void main(String args[]) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        HashTable table = new HashTable();

        while (true) {
            try {
                String input = br.readLine();
                if (input.compareTo("QUIT") == 0)
                    break;

                command(input, table);
            } catch (IOException e) {
                System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
            }
        }
    }

    private static String inputData;

    private static void command(String input, HashTable table) throws IOException {
        String firstCommand = input.substring(0, input.indexOf(' '));
        String secondCommand = input.substring(input.indexOf(' ') + 1);
        // Input을 첫번째로 나타나는 공백을 기준으로 구분하였다.

        if (firstCommand.equals("<")) {
            inputData = secondCommand;
            table.hashing(inputData);
        }
        if (firstCommand.equals("@")) {
            int slotNum = Integer.parseInt(secondCommand);
            table.getAVLTree(slotNum).searchAVLTree();
        }
        if (firstCommand.equals("?")) {
            boolean isExisted = true;

            if (secondCommand.length() > 6) {
                // 검색할 패턴의 길이가 6보다 큰 경우 처리한다.
                // 이 경우에서는 우선 패턴 중 앞의 길이 6인 패턴에 대하여 AVL Tree에 Node로 있는지 확인한다.
                // Node를 가지고 있다면, Input으로 받은 패턴 중 앞에서 검색한 길이 6의 패턴을 제외한 나머지 패턴에 대하여 일치하는지 검사한다.
                String pattern = secondCommand.substring(0, 6);
                String plusPattern = secondCommand.substring(6);
                int slotNum = table.funcHash(pattern);
                AVLTreeNode existedPatternNode = table.getAVLTree(slotNum).findSameStrNode(pattern);

                LinkedList patternList;

                if (existedPatternNode != null) {
                    // 앞의 길이 6인 패턴이 AVL Tree에 Node로 있는 경우
                    patternList = existedPatternNode.getList();

                    if (patternList.getHead() == null)
                        isExisted = false;
                    else {
                        if (patternList.isSamePlusPattern(inputData, plusPattern) == false)
                            isExisted = false;
                        // 나머지 패턴이 일치하지 않으면 isExisted = false, 일치하면 isExisted는 true로 남게 된다.
                    }
                } else
                    // 앞의 길이 6인 패턴이 AVL Tree에 Node로 없는 경우
                    isExisted = false;
            } else {
                // 검색할 패턴의 길이가 6인 경우 처리한다.
                String pattern = secondCommand;
                int slotNum = table.funcHash(pattern);
                AVLTreeNode existedPatternNode = table.getAVLTree(slotNum).findSameStrNode(pattern);

                LinkedList patternList;

                if (existedPatternNode != null) {
                    patternList = existedPatternNode.getList();

                    if (patternList.getHead() != null)
                        patternList.print();
                    else
                        isExisted = false;
                } else
                    isExisted = false;
            }

            if (isExisted == false)
                System.out.println("(0, 0)");
        }
    }
}

class HashTable {
    private AVLTree[] avlTree = new AVLTree[100];

    public int funcHash(String hashStr) {
        int sum = 0;

        for (int i = 0; i < hashStr.length(); i++)
            sum += hashStr.charAt(i);

        return sum % 100;
    }

    public void hashing(String fileName) throws IOException {
        for (int i = 0; i < 100; i++)
            avlTree[i] = new AVLTree();
        BufferedReader temp = new BufferedReader(new FileReader(fileName));

        String line;
        int i, j, count = 1;

        while ((line = temp.readLine()) != null) {
            String hashStr;
            i = 0;
            j = i + 6;

            while (j != line.length() + 1) {
                hashStr = line.substring(i, j);

                AVLTreeNode theNode = avlTree[funcHash(hashStr)].findSameStrNode(hashStr);

                if (theNode == null) {
                    // 각 AVL Tree에 root 노드가 비어있는 경우
                    avlTree[funcHash(hashStr)].insert(hashStr, count, i + 1);
                    theNode = avlTree[funcHash(hashStr)].findSameStrNode(hashStr);
                    LListNode head = new LListNode(theNode.getContent(), theNode.getLine(), theNode.getIndex());
                    theNode.getList().setHead(head);
                } else {
                    // 각 AVL Tree에 적어도 하나의 insert된 노드가 있는 경우
                    LListNode newNode = new LListNode(hashStr, count, i + 1);
                    theNode.getList().insertNode(newNode);
                }

                i++;
                j++;
            }
            count++;
        }
        temp.close();
    }

    public AVLTree getAVLTree(int slotNum) {
        return avlTree[slotNum];
    }
}

class AVLTree {
    private AVLTreeNode root = null;
    private AVLTreeNode newNode;
    private String resultNodes = "";

    public void insert(String hashedStr, int line, int index) {
        root = insert(root, hashedStr, line, index);
    }

    public AVLTreeNode insert(AVLTreeNode parent, String hashedStr, int line, int index) {
        newNode = new AVLTreeNode(hashedStr, line, index);

        if (parent == null) {
            parent = newNode;
        } else if (hashedStr.compareTo(parent.getContent()) < 0) {
            parent.setLeftChild(insert(parent.getLeftChild(), hashedStr, line, index));

            // 회전 연산과 관련된 부분이다.
            if (isRotateNeed(parent) == 2) {
                if (hashedStr.compareTo(parent.getLeftChild().getContent()) < 0)
                    parent = avlRotateRight(parent);
                else {
                    parent.setLeftChild(avlRotateLeft(parent.getLeftChild()));
                    parent = avlRotateRight(parent);
                }
            }
        } else {
            parent.setRightChild(insert(parent.getRightChild(), hashedStr, line, index));

            // 회전 연산과 관련된 부분이다.
            if (isRotateNeed(parent) == -2) {
                if (hashedStr.compareTo(parent.getRightChild().getContent()) > 0)
                    parent = avlRotateLeft(parent);
                else {
                    parent.setRightChild(avlRotateRight(parent.getRightChild()));
                    parent = avlRotateLeft(parent);
                }
            }
        }

        return parent;
    }

    public int isRotateNeed(AVLTreeNode node) {
        // balance factor를 계산해주는 함수이다.
        // balance factor는 왼쪽 Sub Tree의 높이에서 오른쪽 Sub Tree의 높이를 빼서 나오는 결과값이다.
        // balance factor의 절대값이 2 이상인 경우, AVL Tree에서는 회전 연산을 적용하게 된다.
        int leftHeight, rightHeight, balance;

        if (node == null)
            return 0;
        else {
            leftHeight = getHeight(node.getLeftChild());
            rightHeight = getHeight(node.getRightChild());
            balance = leftHeight - rightHeight;
        }
        return balance;
    }

    public AVLTreeNode avlRotateLeft(AVLTreeNode node) {
        // 왼쪽으로 회전 연산을 적용하는 함수이다.
        AVLTreeNode rightChild = node.getRightChild();
        node.setRightChild(rightChild.getLeftChild());
        rightChild.setLeftChild(node);

        return rightChild;
    }

    public AVLTreeNode avlRotateRight(AVLTreeNode node) {
        // 오른쪽으로 회전 연산을 적용하는 함수이다.
        AVLTreeNode leftChild = node.getLeftChild();
        node.setLeftChild(leftChild.getRightChild());
        leftChild.setRightChild(node);

        return leftChild;
    }

    public int getHeight(AVLTreeNode node) {
        // 매개변수로 받은 node의 높이를 계산하는 함수이다.
        if (node == null)
            return 0;
        else {
            int leftHeight = getHeight(node.getLeftChild());
            int rightHeight = getHeight(node.getRightChild());

            return (leftHeight > rightHeight ? leftHeight + 1 : rightHeight + 1);
        }
    }

    public void searchAVLTree() {
        int count = 0;
        resultNodes = "";

        if (preorderTraversal(root, count) == 0)
            System.out.println("EMPTY");
        else
            print(resultNodes);
    }

    public int preorderTraversal(AVLTreeNode node, int count) {
        // Preorder Search()
        if (node != null) {
            resultNodes += node.printNode();
            count++;
            count = preorderTraversal(node.getLeftChild(), count);
            count = preorderTraversal(node.getRightChild(), count);
        }

        return count;
    }

    public void print(String resultString) {
        int lastSpace = resultString.lastIndexOf(' ');
        System.out.println(resultNodes.substring(0, lastSpace));
    }

    public AVLTreeNode preorderSearch(AVLTreeNode node, String hashStr, AVLTreeNode theNode) {
        // hashStr과 같은 string을 가진 Node를 preorder로 검색하는 함수이다.
        if (node != null) {
            if ((node.getContent()).equals(hashStr))
                theNode = node;
            else {
                theNode = preorderSearch(node.getLeftChild(), hashStr, theNode);
                theNode = preorderSearch(node.getRightChild(), hashStr, theNode);
            }
        }

        return theNode;
    }

    public AVLTreeNode findSameStrNode(String hashStr) {
        AVLTreeNode theNode = null;
        theNode = preorderSearch(root, hashStr, theNode);

        if (theNode != null) {
            return theNode;
        } else {
            return null;
        }
    }
}

class LinkedList {
    private LListNode head;
    private LListNode tail;

    LinkedList() {
        head = null;
    }

    public void setHead(LListNode node) {
        head = node;
    }

    public LListNode getHead() {
        return head;
    }

    public LListNode findTailNode() {
        LListNode next;
        tail = head;

        while ((next = tail.getNext()) != null)
            tail = next;

        return tail;
    }

    public void insertNode(LListNode node) {
        tail = findTailNode();
        tail.setNext(node);
    }

    public void print() {
        LListNode temp = head;
        String nodesInfo = "";

        do {
            nodesInfo += temp.printNode();
        } while ((temp = temp.getNext()) != null);

        System.out.println(nodesInfo.trim());
    }

    public boolean isSamePlusPattern(String inputData, String plusPattern) throws IOException {
        // 명령이 ? abcdefgh인 경우, abcdef를 제외한 gh에 대하여 비교를 진행하기 위하여 txt 파일을 다시 읽어들였다.
        // 하지만, txt 파일 전체를 String에 저장하는 것이 아니라 필요로 하는 부분의 String만 읽어와 비교를 하게 된다.
        BufferedReader input = new BufferedReader(new FileReader(inputData));
        LListNode temp = head;
        String nodesInfo = "";

        int lineNum = 1;
        int plusPatternLength = plusPattern.length();
        String line = "";
        boolean isSame = false;

        do {
            // lineNum은 새로 불러온 txt 파일에서 비교하고자 하는 String의 줄 번호로 사용된다.
            // lineNum을 비교에 쓰일 LinkedList의 Node가 저장하고 있는 line 번호까지 올린다.
            while (lineNum <= temp.getLine()) {
                line = input.readLine();
                lineNum++;
            }

            if (line.length() >= temp.getIndex() + 5 + plusPatternLength) {
                if (plusPattern.equals(line.substring(temp.getIndex() + 5, temp.getIndex() + 5 + plusPatternLength))) {
                    isSame = true;
                    nodesInfo += temp.printNode();
                }
            }
        } while ((temp = temp.getNext()) != null);
        // LinkedList(길이 6, 같은 패턴의 문자열이 저장된 LinkedList)의 끝까지 읽는다.

        input.close();

        if (isSame == true)
            System.out.println(nodesInfo.trim());

        return isSame;
    }
}

class AVLTreeNode {
    private int line;
    private int index;
    private String content;

    private AVLTreeNode leftChild;
    private AVLTreeNode rightChild;

    private LinkedList samePattern;

    AVLTreeNode(String hashedStr, int line, int index) {
        content = hashedStr;
        this.line = line;
        this.index = index;
        samePattern = new LinkedList();
    }

    public String getContent() {
        return content;
    }

    public int getLine() {
        return line;
    }

    public int getIndex() {
        return index;
    }

    public AVLTreeNode getLeftChild() {
        return leftChild;
    }

    public AVLTreeNode getRightChild() {
        return rightChild;
    }

    public void setLeftChild(AVLTreeNode newLeftChild) {
        leftChild = newLeftChild;
    }

    public void setRightChild(AVLTreeNode newRightChild) {
        rightChild = newRightChild;
    }

    public LinkedList getList() {
        return samePattern;
    }

    public String printNode() {
        return getContent() + " ";
    }
}

class LListNode {
    private int line;
    private int index;
    private String content;

    private LListNode next;

    LListNode(String hashedStr, int line, int index) {
        content = hashedStr;
        this.line = line;
        this.index = index;
    }

    public void setNext(LListNode newNextNode) {
        next = newNextNode;
    }

    public LListNode getNext() {
        return next;
    }

    public String getContent() {
        return content;
    }

    public int getLine() {
        return line;
    }

    public int getIndex() {
        return index;
    }

    public String printNode() {
        return "(" + line + ", " + index + ") ";
    }
}