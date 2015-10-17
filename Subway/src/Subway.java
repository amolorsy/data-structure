import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;

class Subway {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                String input = br.readLine();
                if (input.compareTo("QUIT") == 0)
                    break;

                command(input, args[0]);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.toString());
            }
        }
    }

    private static void command(String input, String dataTxt) throws IOException {
        String firstCommand = input.substring(0, input.indexOf(' '));
        String secondCommand = input.substring(input.indexOf(' ') + 1);

        BufferedReader inputData = new BufferedReader(new FileReader(dataTxt));
        Dijkstra shortPath = new Dijkstra(getDataNum(inputData), dataTxt);

        shortPath.saveStationInfo();
        shortPath.setInitialWeight();
        shortPath.setTransferStation();
        shortPath.traversal(firstCommand, secondCommand);
        shortPath.printPath(firstCommand, secondCommand);
    }

    private static int getDataNum(BufferedReader inputData) throws IOException {
        String temp;
        int count = 0;

        while ((temp = inputData.readLine()) != null) {
            if (temp.length() == 0)
                break;
            count++;
        }

        return count;
    }
}

class Node {
    private int stationIndex;
    private int time;
    private Node next;

    Node(int stationIndex, int time) {
        this.stationIndex = stationIndex;
        this.time = time;
        next = null;
    }

    public void setNext(Node node) {
        next = node;
    }

    public Node getNext() {
        return next;
    }

    public int getIndex() {
        return stationIndex;
    }

    public int getTime() {
        return time;
    }
}

class LinkedList {
    private Node head;
    private Node newNode;
    private Node tail;

    private int size;

    LinkedList() {
        head = null;
        newNode = null;
        tail = head;

        size = 0;
    }

    public Node getHead() {
        return head;
    }

    public void insert(int stationIndex, int time) {
        newNode = new Node(stationIndex, time);

        if (head == null)
            head = newNode;
        else {
            tail = getTailNode();
            tail.setNext(newNode);
        }
        size++;
    }

    public Node getTailNode() {
        Node tailNode = head;

        if (tailNode != null) {
            while (tailNode.getNext() != null)
                tailNode = tailNode.getNext();
        }

        return tailNode;
    }

    public boolean isAdjacent(int stationIndex) {
        Node walking = head;
        boolean isExist = false;

        while (walking != null) {
            if (walking.getIndex() == stationIndex) {
                isExist = true;
                break;
            }
            walking = walking.getNext();
        }

        return isExist;
    }

    public int getWeight(int stationIndex) {
        Node walking = head;
        int weight = Dijkstra.INFINITE_WEIGHT;

        while (walking != null) {
            if (walking.getIndex() == stationIndex) {
                weight = walking.getTime();
                break;
            }
            walking = walking.getNext();
        }

        return weight;
    }

    public Node getNodeOfIndex(int index) {
        Node walking = head;
        int i = 0;

        while (walking != null && i < index) {
            walking = walking.getNext();
            i++;
        }

        return walking;
    }

    public int size() {
        return size;
    }
}

class Dijkstra {
    private int dataNum;
    private int[] distance;
    private int[] path;
    private boolean[] visited;
    private LinkedList[] edge;
    final static int INFINITE_WEIGHT = Integer.MAX_VALUE;

    private BufferedReader inputData;
    private static HashMap<String, String> station;
    private static HashMap<String, ArrayList<String>> transferStation;
    private static HashMap<String, Integer> vertexIndex;
    private static HashMap<String, String> stationLine;
    private String[] allVertex;

    Dijkstra(int dataNum, String inputFile) {
        this.dataNum = dataNum;
        distance = new int[dataNum];
        path = new int[dataNum];
        station = new HashMap<String, String>();
        transferStation = new HashMap<String, ArrayList<String>>();
        vertexIndex = new HashMap<String, Integer>();
        stationLine = new HashMap<String, String>();
        allVertex = new String[dataNum];
        edge = new LinkedList[dataNum];

        for (int x = 0; x < dataNum; x++)
            edge[x] = new LinkedList();

        try {
            inputData = new BufferedReader(new FileReader(inputFile));
        } catch (IOException e) {
        }
    }

    public void saveStationInfo() throws IOException {
        String temp;
        int i = 0;

        while ((temp = inputData.readLine()) != null) {
            if (temp.length() == 0)
                break;

            String[] splitedInfo = temp.split(" ");
            station.put(splitedInfo[0], splitedInfo[1]);
            vertexIndex.put(splitedInfo[0], i);
            stationLine.put(splitedInfo[0], splitedInfo[2]);

            if (transferStation.containsKey(splitedInfo[1]))
                transferStation.get(splitedInfo[1]).add(splitedInfo[0]);
            else {
                ArrayList<String> list = new ArrayList<String>();
                list.add(splitedInfo[0]);
                transferStation.put(splitedInfo[1], list);
            }

            allVertex[i++] = splitedInfo[0];
        }
    }

    public void setInitialWeight() throws IOException {
        String temp;
        int time;

        while ((temp = inputData.readLine()) != null) {
            String[] splitedInfo = temp.split(" ");
            time = Integer.parseInt(splitedInfo[2]);
            edge[vertexIndex.get(splitedInfo[0])].insert(vertexIndex.get(splitedInfo[1]), time);
        }
    }

    public void setTransferStation() {
        Object[] transferStationSet = transferStation.keySet().toArray();

        for (int i = 0; i < transferStationSet.length; i++) {
            ArrayList<String> list = transferStation.get((String) transferStationSet[i]);

            for (int x = 0; x < list.size(); x++) {
                for (int y = x + 1; y < list.size(); y++) {
                    edge[vertexIndex.get(list.get(x))].insert(vertexIndex.get(list.get(y)), 5);
                    edge[vertexIndex.get(list.get(y))].insert(vertexIndex.get(list.get(x)), 5);
                }
            }
        }
    }

    public void traversal(String start, String end) {
        visited = new boolean[dataNum];
        for (int i = 0; i < dataNum; i++)
            distance[i] = INFINITE_WEIGHT;

        ArrayList<String> list = transferStation.get(start);
        int[] starter = new int[list.size()];
        for (int i = 0; i < starter.length; i++) {
            starter[i] = vertexIndex.get(list.get(i));
            path[starter[i]] = starter[i];
        }

        Comparator<Node> vertexComparator = new VertexComparator();
        PriorityQueue<Node> pQueue = new PriorityQueue<Node>(dataNum, vertexComparator);
        setWeightAdjacentVertex(starter, pQueue);

        int temp = -1;
        boolean isDijkstraStarted = false;

        while (!pQueue.isEmpty()) {
            Node top = pQueue.peek();
            int midDestination = top.getIndex();
            int minDistance = pQueue.poll().getTime();
            visited[midDestination] = true;

            if (!isDijkstraStarted) {
                int startIndex = 0;
                for (int i = 0; i < starter.length; i++) {
                    if (stationLine.get(allVertex[midDestination]).equals(stationLine.get(allVertex[starter[i]])))
                        startIndex = starter[i];
                }
                if (startIndex != 0)
                    path[midDestination] = startIndex;
            } else {
                int startIndex = 0;
                for (int i = 0; i < starter.length; i++) {
                    if (stationLine.get(allVertex[midDestination]).equals(stationLine.get(allVertex[starter[i]])))
                        startIndex = starter[i];
                }
                if (startIndex != 0) {
                    if (path[midDestination] != temp && edge[startIndex].isAdjacent(midDestination) != false)
                        path[midDestination] = startIndex;
                }
            }

            for (int i = 0; i < edge[midDestination].size(); i++) {
                int index = edge[midDestination].getNodeOfIndex(i).getIndex();
                int weight = edge[midDestination].isAdjacent(index) == true ? edge[midDestination].getWeight(index)
                        : INFINITE_WEIGHT;
                if (!visited[index] && (distance[index] > minDistance + weight)) {
                    distance[index] = minDistance + weight;
                    path[index] = midDestination;
                    pQueue.add(new Node(index, distance[index]));
                }
            }
            isDijkstraStarted = true;
            temp = midDestination;
        }
    }

    public void setWeightAdjacentVertex(int[] starter, PriorityQueue<Node> pQueue) {
        for (int i = 0; i < starter.length; i++) {
            int startIndex = starter[i];
            Node walking = edge[startIndex].getHead();
            visited[startIndex] = true;

            while (walking != null) {
                distance[walking.getIndex()] = walking.getTime();
                pQueue.add(new Node(walking.getIndex(), distance[walking.getIndex()]));
                walking = walking.getNext();
            }
        }
    }

    public void printPath(String start, String end) {
        if (start.equals(end)) {
            System.out.println(start);
            System.out.println(0);
        } else {
            ArrayList<String> listS = transferStation.get(start);
            ArrayList<String> listE = transferStation.get(end);

            int[] starter = new int[listS.size()];
            int[] endSet = new int[listE.size()];

            for (int i = 0; i < starter.length; i++)
                starter[i] = vertexIndex.get(listS.get(i));
            for (int i = 0; i < endSet.length; i++)
                endSet[i] = vertexIndex.get(listE.get(i));

            int startIndex = starter[0];
            int endIndex = min(endSet);

            ArrayList<String> shortestPath = new ArrayList<String>();
            shortestPath.add(station.get(allVertex[endIndex]));
            String fromStartToEnd = new String();

            if (station.get(allVertex[endIndex]).equals(station.get(allVertex[path[endIndex]]))) {
                endIndex = path[endIndex];
                distance[min(endSet)] -= 5;
            }

            while (!isArrived(starter, endIndex)) {
                if (station.get(allVertex[path[path[endIndex]]]).equals(station.get(allVertex[path[endIndex]]))) {
                    endIndex = path[endIndex];
                    if (!isArrived(starter, endIndex))
                        shortestPath.add("[" + station.get(allVertex[path[endIndex]]) + "]");
                } else
                    shortestPath.add(station.get(allVertex[path[endIndex]]));
                endIndex = path[endIndex];
            }
            shortestPath.add(station.get(allVertex[startIndex]));

            for (int i = shortestPath.size() - 1; i >= 0; i--)
                fromStartToEnd += shortestPath.get(i) + " ";

            System.out.println(fromStartToEnd = fromStartToEnd.trim());
            System.out.println(distance[min(endSet)]);
        }
    }

    public boolean isArrived(int[] starter, int endIndex) {
        boolean arrived = false;

        for (int i = 0; i < starter.length; i++) {
            if (starter[i] == endIndex) {
                arrived = true;
                break;
            }
        }

        return arrived;
    }

    public int min(int[] arr) {
        int temp = arr[0];

        for (int i = 0; i < arr.length; i++)
            temp = distance[temp] > distance[arr[i]] ? arr[i] : temp;

        return temp;
    }

    class VertexComparator implements Comparator<Node> {
        public int compare(Node a, Node b) {
            if (a.getTime() < b.getTime())
                return -1;
            else if (a.getTime() > b.getTime())
                return 1;
            else
                return 0;
        }
    }
}