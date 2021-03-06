package com.ajr;

import javax.naming.*;
import java.util.*;

public
class EfficientSplitList<E extends Comparable<E>> extends AbstractSet<E> {
    private final Class<E> cl;
    private final SplittableRandom randomLevelGenerator;

    // If you change this number, you also need to change the random function.
    private static final int MAX = 31;

    private int size;

    private Node<E> head = null;


    public
    EfficientSplitList(Class<E> cl) {
        this.cl = cl;
        randomLevelGenerator = new SplittableRandom();
    }

    //TODO improve
    private
    int getRandomLevel() {
        return randomLevelGenerator.nextInt(MAX) + 1;
    }

    @Override
    public
    boolean add(E e) {
        // First element in list
        if (head == null) {
            head = new Node<>(MAX, e);

            size++;
            return true;
        }

        int level = getRandomLevel();
        Node<E> node = new Node<>(level, e);

        Node<E> prev = head;

        // If new node is before head, swap values
        if (head.data.compareTo(node.data) > 0) {
            swapNode(head, node);
        }

        for (int currentLevel = MAX; currentLevel >= 0; currentLevel--) {
            Node<E> next = prev.getNext(currentLevel);

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
    void clear() {
        head = null;
    }

    @Override
    public boolean contains(Object o) {
        E e = cl.cast(o);

        if (head == null) {
            return false;
        }
        if (head.data.compareTo(e) == 0) {
            return true;
        }

        Node<E> prev = head;

        for (int currentLevel = MAX; currentLevel >= 0; currentLevel--) {
            Node<E> next = prev.getNext(currentLevel);

            while (next != null) {
                int comparision = next.data.compareTo(e);

                if (comparision == 0) {
                    return true;
                }

                if (comparision > 0) {
                    break;
                }

                prev = next;
                next = prev.getNext(currentLevel);
            }
        }

        return false;
    }

    @Override
    public
    boolean isEmpty() {
        return size == 0;
    }

    @Override
    public
    Iterator<E> iterator() {
        return new SkipListIterator<>(this);
    }

    @Override
    public
    boolean remove(Object o) {
        E e = cl.cast(o);

        if (head == null) {
            return false;
        }

        Node<E> node = null;
        int level = MAX;

        if (head.data.compareTo(e) == 0) {
            Node<E> next = head.getNext(0);

            if (next != null) {
                swapNode(head, next);

                node = next;    //node is the old head that needs to be removed

                for (int i = 0; i <= node.getLevel(); i++) {
                    next = node.getNext(i);
                    if (next != null || node.data == e) {
                        head.setNext(i, next);
                    }
                }
            }
            else {
                head = null;
            }
        }
        else {
            Node<E> prev = head;
            search:
            for (; level >= 0; level--) {
                Node<E> next = prev.getNext(level);

                while (next != null) {
                    int comparision = next.data.compareTo(e);

                    if (comparision == 0) {
                        node = next;
                        break search;
                    }

                    if (comparision > 0) {
                        break;
                    }

                    prev = next;
                    next = prev.getNext(level);
                }
            }

            // Can't delete value that was not found
            if (node == null) {
                return false;
            }

            for (int i = level; i >= 0; i--) {
                Node<E> next = node.getNext(i);
                prev.setNext(i, next);
                if (i > 0) {
                    Node<E> temp = prev.getNext(i - 1);
                    while (temp != null && temp.data.compareTo(e) != 0) {
                        prev = temp;
                        temp = temp.getNext(i - 1);
                    }
                }
            }
        }

        size--;
        return true;
    }

    @Override
    public
    int size() {
        return size;
    }

    @Override
    public
    String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("size=").append(size).append("\n");
        Node<E> node = head;
        if (node!=null) {
            int iLevel = node.getLevel();
            for (int i=iLevel; i>=0; i--) {
                builder.append("[").append(i).append("] ");
                node = head;
                while (node != null) {
                    builder.append(node.data);
                    Node<E> next = node.getNext(i);
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

    private void swapNode(Node<E> first, Node<E> second) {
        E value = first.data;
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

        int getLevel() {
            return next.length - 1;
        }

        void setNext(int level, Node<T> node) {
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

    private static class SkipListIterator<T extends Comparable<T>> implements ListIterator<T> {
        private EfficientSplitList<T> list;
        private Node<T> next;
        private Node<T> last;

        private SkipListIterator(EfficientSplitList<T> list) {
            this.list = list;
            this.next = list.head;
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public T next() {
            if (next == null) {
                throw new NoSuchElementException();
            }

            last = next;
            next = next.getNext(0);
            return last.data;
        }

        /**
         * Returns {@code true} if this list iterator has more elements when
         * traversing the list in the reverse direction.  (In other words,
         * returns {@code true} if {@link #previous} would return an element
         * rather than throwing an exception.)
         *
         * @return {@code true} if the list iterator has more elements when
         * traversing the list in the reverse direction
         */
        @Override
        public boolean hasPrevious() {
            return false;
        }

        /**
         * Returns the previous element in the list and moves the cursor
         * position backwards.  This method may be called repeatedly to
         * iterate through the list backwards, or intermixed with calls to
         * {@link #next} to go back and forth.  (Note that alternating calls
         * to {@code next} and {@code previous} will return the same
         * element repeatedly.)
         *
         * @return the previous element in the list
         * @throws NoSuchElementException if the iteration has no previous
         *                                element
         */
        @Override
        public T previous() {
            return null;
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to {@link #next}. (Returns list size if the list
         * iterator is at the end of the list.)
         *
         * @return the index of the element that would be returned by a
         * subsequent call to {@code next}, or list size if the list
         * iterator is at the end of the list
         */
        @Override
        public int nextIndex() {
            return 0;
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to {@link #previous}. (Returns -1 if the list
         * iterator is at the beginning of the list.)
         *
         * @return the index of the element that would be returned by a
         * subsequent call to {@code previous}, or -1 if the list
         * iterator is at the beginning of the list
         */
        @Override
        public int previousIndex() {
            return 0;
        }

        /**
         * Removes from the underlying collection the last element returned
         * by this iterator (optional operation).  This method can be called
         * only once per call to {@link #next}.  The behavior of an iterator
         * is unspecified if the underlying collection is modified while the
         * iteration is in progress in any way other than by calling this
         * method.
         *
         * @throws UnsupportedOperationException if the {@code remove}
         *                                       operation is not supported by this iterator
         * @throws IllegalStateException         if the {@code next} method has not
         *                                       yet been called, or the {@code remove} method has already
         *                                       been called after the last call to the {@code next}
         *                                       method
         * @implSpec The default implementation throws an instance of
         * {@link UnsupportedOperationException} and performs no other action.
         */
        @Override
        public void remove() {
            if (last == null) {
                return;
            }

            list.remove(last.data);

        }

        /**
         * Replaces the last element returned by {@link #next} or
         * {@link #previous} with the specified element (optional operation).
         * This call can be made only if neither {@link #remove} nor {@link
         * #add} have been called after the last call to {@code next} or
         * {@code previous}.
         *
         * @param t the element with which to replace the last element returned by
         *          {@code next} or {@code previous}
         * @throws UnsupportedOperationException if the {@code set} operation
         *                                       is not supported by this list iterator
         * @throws ClassCastException            if the class of the specified element
         *                                       prevents it from being added to this list
         * @throws IllegalArgumentException      if some aspect of the specified
         *                                       element prevents it from being added to this list
         * @throws IllegalStateException         if neither {@code next} nor
         *                                       {@code previous} have been called, or {@code remove} or
         *                                       {@code add} have been called after the last call to
         *                                       {@code next} or {@code previous}
         */
        @Override
        public void set(T t) {

        }

        /**
         * Inserts the specified element into the list (optional operation).
         * The element is inserted immediately before the element that
         * would be returned by {@link #next}, if any, and after the element
         * that would be returned by {@link #previous}, if any.  (If the
         * list contains no elements, the new element becomes the sole element
         * on the list.)  The new element is inserted before the implicit
         * cursor: a subsequent call to {@code next} would be unaffected, and a
         * subsequent call to {@code previous} would return the new element.
         * (This call increases by one the value that would be returned by a
         * call to {@code nextIndex} or {@code previousIndex}.)
         *
         * @param t the element to insert
         * @throws UnsupportedOperationException if the {@code add} method is
         *                                       not supported by this list iterator
         * @throws ClassCastException            if the class of the specified element
         *                                       prevents it from being added to this list
         * @throws IllegalArgumentException      if some aspect of this element
         *                                       prevents it from being added to this list
         */
        @Override
        public void add(T t) {

        }
    }

    private boolean validate() {
        if (head == null) return true;

        int level = MAX;
        for (int i = level; i >= 0; i--) {
            Node<E> prev = head;
            Node<E> node = prev.getNext(i);
            while (node != null) {
                // The list should be ordered
                if (node.data.compareTo(prev.data) < 1)
                    return false;
                prev = node;
                node = prev.getNext(i);
            }
        }
        return true;
    }

    public static void main(String args[]) {
        for (int i = 0; i < 1; i++) {
            EfficientSplitList<Integer> list = new EfficientSplitList<>(Integer.class);
            Collection<Integer> set = new HashSet<>();

            SplittableRandom random = new SplittableRandom();
            for (int x = 0; x < 500; x++) {
                set.add(random.nextInt());
            }

            list.addAll(set);

            if (!list.validate()) {
                System.out.println(list.toString());
                continue;
            }

            list.removeAll(set);

            if (!list.validate()) {
                System.out.println(list.toString());
            }
        }

    }
}
