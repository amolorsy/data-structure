import java.io.*;

public class MovieDatabase {
    public static void main(String args[]) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        LinkedList list = new LinkedList(); // 영화를 담는 Node들의 리스트를 생성한다.

        while (true) {
            try {
                String input = br.readLine().toUpperCase();

                if (input.compareTo("QUIT") == 0)
                    break;

                command(input, list); // command 명령어에 LiskedList 매개변수를 추가했다.
            } catch (Exception e) {
                System.out.println("오류를 출력합니다. " + e.toString());
            }
        }
    }

    private static void command(String input, LinkedList list) {
        String[] tokens = input.split("%");

        String genre;
        String title;
        String searchWord;

        // Input에 담긴 명령어에 따라서 적절한 명령을 수행한다.
        if (input.contains("INSERT")) {
            genre = new String(tokens[1]);
            title = new String(tokens[3]);
            list.insert(genre, title);
        } else if (input.contains("DELETE")) {
            genre = new String(tokens[1]);
            title = new String(tokens[3]);
            list.delete(genre, title);
        } else if (input.contains("SEARCH")) {
            searchWord = new String(tokens[1]);
            list.search(searchWord);
        } else if (input.contains("PRINT")) {
            list.print();
        }
    }
}

class Node {
    private String genre;
    private String title;
    private Node nextNode;

    Node() {
    }

    Node(String genre, String title) {
        this.genre = genre;
        this.title = title;
        nextNode = null;
    }

    public void setNextNode(Node nextNode) {
        this.nextNode = nextNode;
    }

    public Node getNextNode() {
        return this.nextNode;
    }

    public boolean isTheNode(String genre, String title) {
        boolean temp = false;

        if ((this.genre).equals(genre) && (this.title).equals(title))
            temp = true;

        return temp;
    }

    public boolean isInclude(String searchWord) {
        boolean temp = (this.title).contains(searchWord);

        return temp;
    }

    public String getGenre() {
        return this.genre;
    }

    public String getTitle() {
        return this.title;
    }

    public void modifyGenreAndTitle(String genre, String title) {
        this.genre = genre;
        this.title = title;
    }

    public void printNode() {
        System.out.println("(" + genre + ", " + title + ")");
    }
}

class LinkedList implements ListCommand {
    private Node head = new Node();
    private Node node;
    private Node tail;

    // allListSize는 전체의 Node 개수를 담는 변수이다.
    private int allListSize = 0;
    private LinkedList sortedList;
    // 정렬에 사용될 Node를 담는 리스트이다.

    public void insert(String genre, String title) {
        node = new Node(genre, title);

        if (head.getNextNode() == null) {
            head.setNextNode(node);
            allListSize++;
        } else {
            // findTheNode 함수는 검색하려는 genre, title을 가진 Node가 있을 경우 그 Node를 리턴하고, 없을 경우 head를
            // 리턴한다.
            if (findTheNode(genre, title) == head) {
                tail = findTailNode();
                tail.setNextNode(node);
                allListSize++;
            }
        }
    }

    // tail Node를 찾는 함수이다.
    private Node findTailNode() {
        Node tailNode = head;

        while (tailNode.getNextNode() != null)
            tailNode = tailNode.getNextNode();

        return tailNode;
    }

    public void delete(String genre, String title) {
        Node temp = head;
        Node willBeDeleted = findTheNode(genre, title);
        // 삭제하고 싶은 Node를 findTheNode(genre, title)을 통해서 찾는다.

        // findTheNode(genre, title)을 실행했을 때, 원하는 Node가 리스트에 있을 때 실행한다.
        if (willBeDeleted != head) {
            while ((temp.getNextNode()) != null) {
                if (temp.getNextNode() == willBeDeleted) {
                    temp.setNextNode(willBeDeleted.getNextNode());
                    break;
                }
                temp = temp.getNextNode();
            }
            allListSize--;
        }
    }

    private Node findTheNode(String genre, String title) {
        Node theNode = head;
        boolean isExist = false;
        // flag 변수이다. isExist가 true로 바뀌는 순간 원하는 Node가 찾아진 것이다.

        while (theNode.getNextNode() != null) {
            theNode = theNode.getNextNode();
            if (theNode.isTheNode(genre, title) == true) {
                isExist = true; // 검색하기 원하는 Node가 발견될 시, isExist를 true로 변경.
                break;
            }
        }

        if (isExist == true)
            return theNode;
        else
            return head;
    }

    public void search(String searchWord) {
        Node temp = head;
        sortedList = new LinkedList();
        int isIncludeCount = 0;
        // isIncludeCount는 search 함수 실행 시 원하는 Node가 몇 개 있는지 count하는 변수이다.

        while ((temp.getNextNode()) != null) {
            temp = temp.getNextNode();
            if ((temp.isInclude(searchWord)) == true) {
                // temp가 정렬하기 원하는 Node일 시, sortedList에 추가한다.
                sortedList.insert(temp.getGenre(), temp.getTitle());
                isIncludeCount++;
            }
        }

        sort(isIncludeCount, sortedList);
    }

    public void print() {
        int originalSize = allListSize;
        Node temp = head;
        sortedList = new LinkedList();

        while ((temp.getNextNode()) != null) {
            temp = temp.getNextNode();
            sortedList.insert(temp.getGenre(), temp.getTitle());
            // temp가 정렬하기 원하는 Node일 시, sortedList에 추가한다.
        }

        sort(originalSize, sortedList);
    }

    public void sort(int flag, LinkedList sortedList) {
        if (flag == 0) // flag로 받은 isIncludeCount가 0이면 EMPTY를 출력.
            System.out.println("EMPTY");
        else if (flag == 1) // flag로 받은 isIncludeCount가 1이면 1개의 Node만을 출력.
            sortedList.head.getNextNode().printNode();
        else {
            Node tempForSort; // 정렬 작업을 위해, sortedList의 첫 Node부터 끝 노드까지
            // walking하는 변수이다.

            while (flag-- > 0) {
                tempForSort = sortedList.head.getNextNode();
                while ((tempForSort.getNextNode()) != null) {
                    String prevNodeGenre = tempForSort.getGenre();
                    String prevNodeTitle = tempForSort.getTitle();
                    String nextNodeGenre = tempForSort.getNextNode().getGenre();
                    String nextNodeTitle = tempForSort.getNextNode().getTitle();
                    String tempGenre, tempTitle;

                    boolean needToSort = false;
                    // flag 변수이며, 두 Node 간 정렬할 필요성이 있을 때 true로 변경된다.

                    // sortedList에 담긴 Node들의 Genre, Title을 compareTo() Method에 의해 비교한다.
                    if (prevNodeGenre.compareTo(nextNodeGenre) == 0) {
                        if (prevNodeTitle.compareTo(nextNodeTitle) > 0)
                            needToSort = true;
                    } else if (prevNodeGenre.compareTo(nextNodeGenre) > 0)
                        needToSort = true;
                    else
                        needToSort = false;

                    if (needToSort == true) {
                        // 두 노드 간 정렬할 필요성이 있을 때(needToSort == true), 두 노드의 Genre와 Title을 서로 바꾼다.
                        tempGenre = nextNodeGenre;
                        tempTitle = nextNodeTitle;
                        tempForSort.getNextNode().modifyGenreAndTitle(prevNodeGenre, prevNodeTitle);
                        tempForSort.modifyGenreAndTitle(tempGenre, tempTitle);
                    }

                    tempForSort = tempForSort.getNextNode();
                }
            }

            // 정렬된 Node를 정렬된 순서로 Print하는 부분이다.
            tempForSort = sortedList.head;
            while (tempForSort.getNextNode() != null) {
                tempForSort = tempForSort.getNextNode();
                tempForSort.printNode();
            }
        }
    }
}

interface ListCommand {
    public void insert(String genre, String title);

    public void delete(String genre, String title);

    public void search(String searchWord);

    public void print();
}