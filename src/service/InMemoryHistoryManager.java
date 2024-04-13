package service;

import model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> linkedHistory;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryHistoryManager that = (InMemoryHistoryManager) o;
        return Objects.equals(linkedHistory, that.linkedHistory) &&
                Objects.equals(first, that.first) &&
                Objects.equals(last, that.last);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkedHistory, first, last);
    }

    public InMemoryHistoryManager() {
        linkedHistory = new HashMap<>();
    }

    private static class Node {

        Task item;
        Node next;
        Node prev;

        public Node(Node prev, Task item, Node next) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(item, node.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item);
        }
    }

    Node first;
    Node last;

    public void linkLast(Task task) {
        final Node l = last;
        final Node newNode = new Node(l, task, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        linkedHistory.put(task.getId(), newNode);
    }


    @Override
    public void add(Task task) {
        Node node = linkedHistory.get(task.getId());
        removeNode(node);
        linkLast(task);
    }


    public void removeNode(Node node) {
        if (node != null) {
            Node nodePrev = node.prev;
            Node nodeNext = node.next;
            if (nodePrev != null && nodeNext != null) {
                nodePrev.next = nodeNext;
                nodeNext.prev = nodePrev;
                linkedHistory.put(nodePrev.item.getId(), nodePrev);
                linkedHistory.put(nodeNext.item.getId(), nodeNext);
            }
            if (nodePrev == null && nodeNext != null) {
                nodeNext.prev = null;
                linkedHistory.put(nodeNext.item.getId(), nodeNext);
                first = nodeNext;
            }
            if (nodeNext == null && nodePrev != null) {
                nodePrev.next = null;
                linkedHistory.put(nodePrev.item.getId(), nodePrev);
                last = nodePrev;
            }
            if (nodePrev == null && nodeNext == null) {
                first = null;
                last = null;
            }
            linkedHistory.remove(node.item.getId());
        }
    }

    @Override
    public void remove(int id) {
        Node node = linkedHistory.getOrDefault(id,
                new Node(null, null, null));
        if (node.item != null) {
            removeNode(node);
        }
    }


    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node temp = last;
        while (temp != null) {
            history.add(linkedHistory.get(temp.item.getId()).item);
            temp = temp.prev;
        }
        return history;
    }
}
