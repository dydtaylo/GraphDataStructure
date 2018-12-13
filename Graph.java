package FinalExamPractice;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Random;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Graph extends HashMap<Integer, List<DistanceTo>> {
	ArrayList<Integer> components;
	ArrayList<int[]> shortestDistances;
	boolean dijkstraBuffer;
	
	public Graph(List<Vertex> vertices) {
		shortestDistances = new ArrayList<int[]>();
		for(int i = 0; i < vertices.size() + 1; i++)
			shortestDistances.add(new int[0]);
		dijkstraBuffer = true;
		for(int i = 0; i < vertices.size(); i++)
			this.addConnection(vertices.get(i));

		dijkstraBuffer = false;
		buildShortestPaths();
	}
	public Graph() {
		super();
		dijkstraBuffer = false;
	}
	
	public static void main(String[] args) {
		Random gen = new Random();
		
		Graph g;
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		int numVertices = 50;
		int connectionChances = numVertices/2;
		int connectionProbability = 50;
		int nextConnectionId = -1;
		int maxDistance = 10;
		
		ArrayList<DistanceTo> connections;
		for(int i = 0; i < numVertices; i++) {
			connections = new ArrayList<DistanceTo>();
			for(int j = 0; j < connectionChances; j++) {
				if(gen.nextInt(100) < connectionProbability) {
					while(nextConnectionId == -1 || nextConnectionId == i || Graph.contains(connections, nextConnectionId))
						nextConnectionId = gen.nextInt(numVertices) + 1;
					
					connections.add(new DistanceTo(nextConnectionId, gen.nextInt(maxDistance) + 1));
				}
			}
			
			if(connections.size() == 0)
				i--;
			else
				vertices.add(new Vertex(i, connections));
		}
		
		g = new Graph(vertices);	
		g.connectGraph();
		
		
		// for debugging use
		System.out.println(g);
		int[] distances = g.dijkstra(4);
		System.out.println(Arrays.toString(distances));
	}
	
	public void buildShortestPaths() {
		for(Integer value: this.keySet())
			addShortestPaths(value);
	}
	public void addShortestPaths(int id) {
		while(shortestDistances.size() < id)
			shortestDistances.add(new int[0]);
		
		shortestDistances.set(id, dijkstra(id));
	}
	public int getShortestDistance(int start, int finish) {
		if(!this.containsKey(start) || !this.containsKey(finish))
			return -1;
		else
			return shortestDistances.get(start)[finish];
	}
	
	public int[] dijkstra(int origin) {
		int[] distances = new int[this.size()];
		dijkstra(origin, distances);
		return distances;
	}
	public void dijkstra(int origin, int[] distances) {
		boolean[] visited = new boolean[this.size()];
		for(int i = 0; i < distances.length; i++)
			distances[i] = Integer.MAX_VALUE;
		distances[origin] = 0;
		
		PriorityQueue<DistanceTo> queue = new PriorityQueue<DistanceTo>();
		queue.add(new DistanceTo(origin, 0));
		DistanceTo current;
		ArrayList<DistanceTo> neighbors;
		while(!queue.isEmpty()) {
			current = queue.poll();
			visited[current.getTarget()] = true;
			neighbors = new ArrayList<DistanceTo>(this.get(current.getTarget()));
			for(DistanceTo n: neighbors) {
				if(!visited[n.getTarget()]) {
					distances[n.getTarget()] = Math.min(distances[current.getTarget()] + n.getDistance(), distances[n.getTarget()]);
					queue.add(n);
				}
			}
		}
	}
	
	public boolean allTrue(boolean[] flags) {
		for(int i = 0; i < flags.length; i++) {
			if(!flags[i])
				return false;
		}
		
		return true;
	}
	
	public void connectGraph() {
		connectGraph(new Random());
	}
	public void connectGraph(Random gen) {
		if(components == null)
			components = connectedComponents();
		
		ArrayList<DistanceTo> connections;
		for(int i = 0; i < components.size() - 1; i++) {
				connections = new ArrayList<DistanceTo>();			
				connections.add(new DistanceTo(i + 1, 10));
				this.addConnection(new Vertex(i, connections));
		}
		
		components = connectedComponents();
	}

	public ArrayList<Integer> connectedComponents(){
		ArrayList<Integer> djSet = new ArrayList<Integer>();
		int[] disjointSet = new DisjointSet(this).getParent();
		for(int i = 0; i < disjointSet.length; i++)
			djSet.add(disjointSet[i]);
		
		return djSet;
	}
	public void printComponentIds() {
		HashSet<Integer> numbers = new HashSet<Integer>();
			components = connectedComponents();
		
		for(int i = 0; i < components.size(); i++)
			numbers.add(components.get(i));
		
		for(Integer i: numbers)
			System.out.print(i + " ");
		
		System.out.println();
	}
	
	public void addConnections(List<Vertex> vertices) {
		dijkstraBuffer = true;
		for(int i = 0; i < vertices.size(); i++)
			addConnection(vertices.get(i));
		
		dijkstraBuffer = false;
		buildShortestPaths();
	}
	public void addConnection(Vertex v) {
		ArrayList<DistanceTo> connections;
		ArrayList<DistanceTo> newConnections = v.getConnections();
		ArrayList<DistanceTo> backwardsConnections = new ArrayList<DistanceTo>();
		
		if(!this.containsKey(v.getID()))
			this.put(v.getID(), v.getConnections());
		
		connections = (ArrayList<DistanceTo>) this.get(v.getID());
		for(int i = 0; i < newConnections.size(); i++) {
			if(!contains(connections, newConnections.get(i).getTarget())) {
				this.get(v.getID()).add(newConnections.get(i));
			}
			
			if(!contains(this.get(newConnections.get(i).getTarget()), v.getID())) {
				backwardsConnections.add(new DistanceTo(v.getID(), newConnections.get(i).getDistance()));
				this.addConnection(new Vertex(newConnections.get(i).getTarget(), backwardsConnections));
			}
		}
		
		if(!dijkstraBuffer)
			buildShortestPaths();
	}
	public static boolean contains(List<DistanceTo> list, int id) {
		if(list == null)
			return false;
		
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).getTarget() == id)
				return true;
		}
		
		return false;
	}
}

/*class DisjointSet extends ArrayList{
	
	public DisjointSet(HashMap<Integer, ArrayList<DistanceTo>> g) {
		super();
		ArrayList<Integer> ids = new ArrayList<Integer>(g.keySet());
		ArrayList<DistanceTo> connections;
		
		for(int i = 0; i < g.size(); i++)
			this.add(i);
		
		this.add(g.size() + 1);
		
		for(int i = 0; i < ids.size(); i++) {
			connections = g.get(ids.get(i));
			for(int j = 0; j < connections.size(); j++)
				union(connections.get(j).getTarget(), ids.get(i));
		}
	}
	
	public void union(int x, int y) {
		int r1 = find(x);
		int r2 = find(y);
		if(r1 != r2)
			this.set(r1, r2);
	}
	
	public int find(int x) {
		if(this.size() <= 0 || this.size() >= x)
			return x;
		
		int current = x;
		if((int) this.get(current) == current)
			return current;
		
		this.set(current, find((int) this.get(current)));
		return (int) this.get(current);
	}
}*/

class DisjointSet{
	int[] parent;
	
	public DisjointSet(Graph graph) {
		ArrayList<Integer> ids = new ArrayList<Integer>(graph.keySet());
		ArrayList<List<DistanceTo>> values = new ArrayList<List<DistanceTo>>();
		for(List<DistanceTo> AL: graph.values())
			values.add(AL);
		int max = getMax(ids);
		
		if(max > 0)
			parent = new int[max + 1];
		else
			parent = new int[0];

		for(int i = 0; i < parent.length; i++)
			parent[i] = i;
		
		for(Integer id: ids) {
			for(DistanceTo neighbor: values.get(id))
				union(neighbor.getTarget(), id);
		}
		// displayParent(); // for debugging
	}
	
	public int[] getParent() {
		return parent;
	}
	
	public void union(int x, int y) {
		int r1 = find(x);
		int r2 = find(y);
		if(r1 != r2)
			parent[r1] = r2;
	}
	
	public int find(int x) {
		if(parent.length == 0)
			return x;
		if(parent.length <= x)
			return x;
		
		int current = x;
		if(parent[current] == current)
			return current;
		parent[current] = find(parent[current]);
		return parent[current];
	}
	
	public int getMax(ArrayList<Integer> nums) {
		int max = Integer.MIN_VALUE;
		for(int i = 0; i < nums.size(); i++) {
			if(nums.get(i) > max)
				max = nums.get(i);
		}
		
		return max;
	}
	
	public void displayParent() {
		System.out.println();
		String indexStr = "{";
		String arrStr = "{";
		for(int i = 0; i < parent.length; i++) {
			indexStr += i + ", ";
			arrStr += parent[i] + ", ";
		}
		indexStr = indexStr.substring(0, indexStr.length() - 2) + "}";
		arrStr = arrStr.substring(0, arrStr.length() - 2) + "}";
		
		System.out.println(indexStr);
		System.out.println(arrStr);
	}
}

class Vertex{
	int id;
	ArrayList<DistanceTo> connections;
	
	public Vertex(int id, ArrayList<DistanceTo> connections){
		this.id = id;
		this.connections = connections;
	}
	public Vertex(int id) {
		this.id = id;
	}
	
	public void addConnection(DistanceTo d) {
		connections.add(d);
	}
	
	public int getID() {
		return id;
	}
	public ArrayList<DistanceTo> getConnections(){
		return connections;
	}
	
	public String toString() {
		return id + "->" + connections;
	}
}

class DistanceTo implements Comparable<DistanceTo>{
	private int target, distance;
	
	public DistanceTo(int target, int distance) {
		this.target = target;
		this.distance = distance;
	}
	
	public int getTarget() {
		return target;
	}
	public int getDistance() {
		return distance;
	}
	
	public int compareTo(DistanceTo other) {
		if(this.distance == other.distance)
			return this.target - other.target;
		else
			return this.distance - other.distance;
	}
	
	public String toString() {
		return distance + " units from " + target + "\n";
	}
}