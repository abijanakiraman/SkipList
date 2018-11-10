package com.ajr;

import java.util.SplittableRandom;

public
class EfficientSplitList<T extends Comparable<T>> {
    private final SplittableRandom randomLevelGenerator;

    // If you change this number, you also need to change the random function.
    private static final int MAX = 5;

    private int size;

    private Node<T> head = null;


    public
    EfficientSplitList() {
        randomLevelGenerator = new SplittableRandom();
    }

    //TODO improve
    private
    int getRandomLevel() {
        return randomLevelGenerator.nextInt(MAX) + 1;
    }

    protected
    boolean add(T value) {
        // First element in list
        if (head == null) {
            head = new Node<>(MAX, value);

            size++;
            return true;
        }

        int level = getRandomLevel();
        Node<T> node = new Node<>(level, value);

        Node<T> prev = head;

        // If new node is before head, swap values
        if (head.data.compareTo(node.data) > 0) {
            swapNode(head, node);
        }

        for (int currentLevel = MAX; currentLevel >= 0; currentLevel--) {
            Node<T> next = prev.getNext(currentLevel);

            while (next != null) {
                int comparison = next.data.compareTo(node.data);
                //If they are the same, we already have this data, so return
                if (comparison == 0) {
                    return false;
                }

                //If we overshot it, go down one level
                if (comparison > 0) {
                    break;
                }

                prev = next;
                next = prev.getNext(currentLevel);
            }
            // If we are at or below the level we are going to insert in, add to linked list
            if (currentLevel <= level) {
                node.setNext(currentLevel, next);
                prev.setNext(currentLevel, node);
            }
        }

        size++;
        return true;
    }

    @Override
    public
    String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("size=").append(size).append("\n");
        Node<T> node = head;
        if (node!=null) {
            int iLevel = node.getLevel();
            for (int i=iLevel; i>=0; i--) {
                builder.append("[").append(i).append("] ");
                node = head;
                while (node != null) {
                    builder.append(node.data);
                    Node<T> next = node.getNext(i);
                    if (next != null)
                        builder.append("->");
                    node = next;
                }
                if (i>0) builder.append("\n");
            }
        }
        builder.append("\n");
        return builder.toString();
    }

    private void swapNode(Node<T> first, Node<T> second) {
        T value = first.data;
        first.data = second.data;
        second.data = value;
    }

    protected static class Node<T extends Comparable<T>> {

        private Node<T>[] next;

        private T data;

        Node(int level, T data) {
            this.next = new Node[level+1];
            this.data = data;
        }

        protected int getLevel() {
            return next.length - 1;
        }

        protected void setNext(int level, Node<T> node) {
            this.next[level] = node;
        }
        protected Node<T> getNext(int level) {
            if (level > this.next.length) {
                return null;
            }
            return this.next[level];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("data=").append(data);
            if (next!=null) {
                builder.append("\n").append("next=[");
                int size = next.length;
                for (int i=0; i<size; i++) {
                    Node<T> n = next[i];
                    if (n!=null) builder.append(n.data);
                    else builder.append("none");
                    if (i!=size-1) builder.append(", ");
                }
                builder.append("]");
            }
            return builder.toString();
        }
    }

    public static void main(String args[]) {
        EfficientSplitList<Integer> list = new EfficientSplitList<>();
        System.out.println(list.toString());
        list.add(1);
        System.out.println(list.toString());
        list.add(2);
        System.out.println(list.toString());
        list.add(3);
        System.out.println(list.toString());
        list.add(4);
        System.out.println(list.toString());
        list.add(5);
        System.out.println(list.toString());
        list.add(32);
        System.out.println(list.toString());
        list.add(6);
        System.out.println(list.toString());
        list.add(6);
        System.out.println(list.toString());
        list.add(6);
        System.out.println(list.toString());
        list.add(6);
        System.out.println(list.toString());
        list.add(6);
        System.out.println(list.toString());

    }
}
