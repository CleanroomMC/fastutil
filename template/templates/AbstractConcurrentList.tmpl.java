package {{ typePackagePath }};

import it.unimi.dsi.fastutil.{{ primitiveTypeName }}s.*;
import java.io.Serializable;
import java.util.concurrent.locks.*;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * An abstract class providing basic methods for lists implementing a type-specific list interface.
 *
 *
 *
 * <p>As an additional bonus, this class implements on top of the list operations a type-specific stack.
 *
 *
 *
 * <p>Most of the methods in this class are optimized with the assumption that the List will have
 * <p>
 * {@link java.util.RandomAccess have constant-time random access}. If this is not the case, you
 * <p>
 * should probably <em>at least</em> override {@link #listIterator(int)} and the {@code xAll()} methods
 * <p>
 * (such as {@link #addAll}) with a more appropriate iteration scheme. Note the {@link #subList(int, int)}
 * <p>
 * method is cognizant of random-access or not, so that need not be reimplemented.
 */
public abstract class {{ className }} extends Abstract{{ capitalizedPrimitiveTypeName }}Collection implements {{ capitalizedPrimitiveTypeName }}List, {{ capitalizedPrimitiveTypeName }}Stack {
    /** The locks. */
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Lock wlock = lock.writeLock();
    protected final Lock rlock = lock.readLock();

    protected {{ className }}() {
    }

    /**
     * Ensures that the given index is nonnegative and not greater than the list size.
     *
     * @param index an index.
     * @throws IndexOutOfBoundsException if the given index is negative or greater than the list size.
     */
    protected void ensureIndex(final int index) {
        // TODO When Java 9 becomes the minimum java, use Objects#checkIndex(index, size() + 1) (as can be an intrinsic)
        if (index < 0) throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
        if (index > size())
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + (size()) + ")");
    }

    /**
     * Ensures that the given index is nonnegative and smaller than the list size.
     *
     * @param index an index.
     * @throws IndexOutOfBoundsException if the given index is negative or not smaller than the list size.
     */
    protected void ensureRestrictedIndex(final int index) {
        // TODO When Java 9 becomes the minimum java, use Objects#checkIndex (as can be an intrinsic)
        if (index < 0) throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
        if (index >= size())
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + (size()) + ")");
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation always throws an {@link UnsupportedOperationException}.
     */
    @Override
    public void add(final int index, final {{ primitiveTypeName }} k) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to the type-specific version of {@link List#add(int, Object)}.
     */
    @Override
    public boolean add(final {{ primitiveTypeName }} k) {
        add(size(), k);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation always throws an {@link UnsupportedOperationException}.
     */
    @Override
    public {{ primitiveTypeName }} remove{{ capitalizedPrimitiveTypeName }}(final int i) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation always throws an {@link UnsupportedOperationException}.
     */
    @Override
    public {{ primitiveTypeName }} set(final int index, final {{ primitiveTypeName }} k) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds all of the elements in the specified collection to this list (optional operation).
     */
    @Override
    public boolean addAll(int index, final Collection<? extends {{ wrapperClassName }}> c) {
        if (c instanceof {{ capitalizedPrimitiveTypeName }}Collection) {
            return addAll(index, ({{ capitalizedPrimitiveTypeName }}Collection) c);
        }
        ensureIndex(index);
        final Iterator<? extends {{ wrapperClassName }}> i = c.iterator();
        final boolean retVal = i.hasNext();
        while (i.hasNext()) add(index++, (i.next()).{{ primitiveTypeName }}Value());
        return retVal;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to the type-specific version of {@link List#addAll(int, Collection)}.
     */
    @Override
    public boolean addAll(final Collection<? extends {{ wrapperClassName }}> c) {
        return addAll(size(), c);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to {@link #listIterator()}.
     */
    @Override
    public {{ capitalizedPrimitiveTypeName }}ListIterator iterator() {
        return listIterator();
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to {@link #listIterator(int) listIterator(0)}.
     */
    @Override
    public {{ capitalizedPrimitiveTypeName }}ListIterator listIterator() {
        return listIterator(0);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation is based on the random-access methods.
     */
    @Override
    public {{ capitalizedPrimitiveTypeName }}ListIterator listIterator(final int index) {
        ensureIndex(index);
        return new {{ capitalizedPrimitiveTypeName }}Iterators.AbstractIndexBasedListIterator(0, index) {
            @Override
            protected {{ primitiveTypeName }} get(int i) {
                return {{ className }}.this.get{{ capitalizedPrimitiveTypeName }}(i);
            }

            @Override
            protected void add(int i, {{ primitiveTypeName }} k) {
                {{ className }}.this.add(i, k);
            }

            @Override
            protected void set(int i, {{ primitiveTypeName }} k) {
                {{ className }}.this.set(i, k);
            }

            @Override
            protected void remove(int i) {
                {{ className }}.this.remove{{ capitalizedPrimitiveTypeName }}(i);
            }

            @Override
            protected int getMaxPos() {
                return {{ className }}.this.size();
            }
        };
    }

    static final class IndexBasedSpliterator extends {{ capitalizedPrimitiveTypeName }}Spliterators.LateBindingSizeIndexBasedSpliterator {
        final {{ capitalizedPrimitiveTypeName }}List l;

        IndexBasedSpliterator({{ capitalizedPrimitiveTypeName }}List l, int pos) {
            super(pos);
            this.l = l;
        }

        IndexBasedSpliterator({{ capitalizedPrimitiveTypeName }}List l, int pos, int maxPos) {
            super(pos, maxPos);
            this.l = l;
        }

        @Override
        protected int getMaxPosFromBackingStore() {
            return l.size();
        }

        @Override
        protected {{ primitiveTypeName }} get(int i) {
            return l.get{{ capitalizedPrimitiveTypeName }}(i);
        }

        @Override
        protected IndexBasedSpliterator makeForSplit(int pos, int maxPos) {
            return new IndexBasedSpliterator(l, pos, maxPos);
        }
    }

    /**
     * Returns true if this list contains the specified element.
     *
     * @implSpec This implementation delegates to {@code indexOf()}.
     * @see List#contains(Object)
     */
    @Override
    public boolean contains(final {{ primitiveTypeName }} k) {
        return indexOf(k) >= 0;
    }

    @Override
    public int indexOf(final {{ primitiveTypeName }} k) {
        rlock.lock();
        try {
            final {{ capitalizedPrimitiveTypeName }}ListIterator i = listIterator();
            {{ primitiveTypeName }} e;
            while(i.hasNext()){
                e = i.next{{ capitalizedPrimitiveTypeName }}();
                if (((k)==(e))) return i.previousIndex();
            }
            return-1;
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public int lastIndexOf(final {{ primitiveTypeName }} k) {
        rlock.lock();
        try {
            {{ capitalizedPrimitiveTypeName }}ListIterator i = listIterator(size());
            {{ primitiveTypeName }} e;
            while (i.hasPrevious()) {
                e = i.previous{{ capitalizedPrimitiveTypeName }}();
                if (((k) == (e))) return i.nextIndex();
            }
            return -1;
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public void size(final int size) {
        rlock.lock();
        try {
            int i = size();
            if (size > i) while (i++ < size) add(({{ defaultValue }}));
            else while (i-- != size) remove{{ capitalizedPrimitiveTypeName }}(i);
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public {{ capitalizedPrimitiveTypeName }}List subList(final int from, final int to) {
        rlock.lock();
        try {
            ensureIndex(from);
            ensureIndex(to);
            if (from > to)
                throw new IndexOutOfBoundsException("Start index (" + from + ") is greater than end index (" + to + ")");
            return this instanceof java.util.RandomAccess ? new Concurrent{{ capitalizedPrimitiveTypeName }}RandomAccessSubList(this, from, to) : new Concurrent{{ capitalizedPrimitiveTypeName }}SubList(this, from, to);
        } finally {
            rlock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec If this list is {@link java.util.RandomAccess}, will iterate using a for
     * <p>
     * loop and the type-specific {@link List#get(int)} method. Otherwise it will fallback
     * <p>
     * to using the iterator based loop implementation from the superinterface.
     */
    @Override
    public void forEach(final {{ capitalizedPrimitiveTypeName }}Consumer action) {
        rlock.lock();
        try {
            if (this instanceof java.util.RandomAccess) {
                for (int i = 0, max = size(); i < max; ++i) {
                    action.accept(get{{ capitalizedPrimitiveTypeName }}(i));
                }
            } else {
                {{ capitalizedPrimitiveTypeName }}List.super.forEach(action);
            }
        } finally {
            rlock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     *
     *
     * <p>This is a trivial iterator-based based implementation. It is expected that
     * <p>
     * implementations will override this method with a more optimized version.
     */
    @Override
    public void removeElements(final int from, final int to) {
        wlock.lock();
        try {
            ensureIndex(to);
            // Always use the iterator based implementation even for RandomAccess so we don't have to worry about shifting indexes.
            {{ capitalizedPrimitiveTypeName }}ListIterator i = listIterator(from);
            int n = to - from;
            if (n < 0)
                throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
            while (n-- != 0) {
                i.next{{ capitalizedPrimitiveTypeName }}();
                i.remove();
            }
        } finally {
            wlock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     *
     *
     * <p>This is a trivial iterator-based implementation. It is expected that
     * <p>
     * implementations will override this method with a more optimized version.
     */
    @Override
    public void addElements(int index, final {{ primitiveTypeName }}[] a, int offset, int length) {
        wlock.lock();
        try {
            ensureIndex(index);
            {{ capitalizedPrimitiveTypeName }}Arrays.ensureOffsetLength(a, offset, length);
            if (this instanceof java.util.RandomAccess) {
                while (length-- != 0) add(index++, a[offset++]);
            } else {
                {{ capitalizedPrimitiveTypeName }}ListIterator iter = listIterator(index);
                while (length-- != 0) iter.add(a[offset++]);
            }
        } finally {
            wlock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to the analogous method for array fragments.
     */
    @Override
    public void addElements(final int index, final {{ primitiveTypeName }}[] a) {
        wlock.lock();
        try {
            addElements(index, a, 0, a.length);
        } finally {
            wlock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     *
     *
     * <p>This is a trivial iterator-based implementation. It is expected that
     * <p>
     * implementations will override this method with a more optimized version.
     */
    @Override
    public void getElements(final int from, final {{ primitiveTypeName }}[] a, int offset, int length) {
        rlock.lock();
        try {
            ensureIndex(from);
            {{ capitalizedPrimitiveTypeName }}Arrays.ensureOffsetLength(a, offset, length);
            if (from + length > size())
                throw new IndexOutOfBoundsException("End index (" + (from + length) + ") is greater than list size (" + size() + ")");
            if (this instanceof java.util.RandomAccess) {
                int current = from;
                while (length-- != 0) a[offset++] = get{{ capitalizedPrimitiveTypeName }}(current++);
            } else {
                {{ capitalizedPrimitiveTypeName }}ListIterator i = listIterator(from);
                while (length-- != 0) a[offset++] = i.next{{ capitalizedPrimitiveTypeName }}();
            }
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public void setElements(int index, {{ primitiveTypeName }}[] a, int offset, int length) {
        wlock.lock();
        try {
            ensureIndex(index);
            {{ capitalizedPrimitiveTypeName }}Arrays.ensureOffsetLength(a, offset, length);
            if (index + length > size())
                throw new IndexOutOfBoundsException("End index (" + (index + length) + ") is greater than list size (" + size() + ")");
            if (this instanceof java.util.RandomAccess) {
                for (int i = 0; i < length; ++i) {
                    set(i + index, a[i + offset]);
                }
            } else {
                {{ capitalizedPrimitiveTypeName }}ListIterator iter = listIterator(index);
                int i = 0;
                while (i < length) {
                    iter.next{{ capitalizedPrimitiveTypeName }}();
                    iter.set(a[offset + i++]);
                }
            }
        } finally {
            wlock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to {@link #removeElements(int, int)}.
     */
    @Override
    public void clear() {
        wlock.lock();
        try {
            removeElements(0, size());
        } finally {
            wlock.unlock();
        }
    }

    /**
     * Returns the hash code for this list, which is identical to {@link java.util.List#hashCode()}.
     *
     * @return the hash code for this list.
     */
    @Override
    public int hashCode() {
        rlock.lock();
        try {
            {{ capitalizedPrimitiveTypeName }}Iterator i = iterator();
            int h = 1, s = size();
            while (s-- != 0) {
                {{ primitiveTypeName }} k = i.next{{ capitalizedPrimitiveTypeName }}();
                h = 31 * h + {{ wrapperClassName }}.hashCode(k);
            }
            return h;
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public boolean equals(final Object o) {
        rlock.lock();
        try {
            if (o == this) return true;
            if (!(o instanceof final List<?> l)) return false;
            int s = size();
            if (s != l.size()) return false;
            if (l instanceof {{ capitalizedPrimitiveTypeName }}List) {
                final {{ capitalizedPrimitiveTypeName }}ListIterator i1 = listIterator(), i2 = (({{ capitalizedPrimitiveTypeName }}List) l).listIterator();
                while (s-- != 0) if (i1.next{{ capitalizedPrimitiveTypeName }}() != i2.next{{ capitalizedPrimitiveTypeName }}()) return false;
                return true;
            }
            final ListIterator<?> i1 = listIterator(), i2 = l.listIterator();
            while (s-- != 0) if (!java.util.Objects.equals(i1.next(), i2.next())) return false;
            return true;
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Compares this list to another object. If the
     * <p>
     * argument is a {@link java.util.List}, this method performs a lexicographical comparison; otherwise,
     * <p>
     * it throws a {@code ClassCastException}.
     *
     * @param l a list.
     * @return if the argument is a {@link java.util.List}, a negative integer,
     * <p>
     * zero, or a positive integer as this list is lexicographically less than, equal
     * <p>
     * to, or greater than the argument.
     * @throws ClassCastException if the argument is not a list.
     */

    @Override
    public int compareTo(final List<? extends {{ wrapperClassName }}> l) {
        rlock.lock();
        try {
            if (l == this) return 0;
            if (l instanceof {{ capitalizedPrimitiveTypeName }}List) {
                final {{ capitalizedPrimitiveTypeName }}ListIterator i1 = listIterator(), i2 = (({{ capitalizedPrimitiveTypeName }}List) l).listIterator();
                int r;
                {{ primitiveTypeName }} e1, e2;
                while (i1.hasNext() && i2.hasNext()) {
                    e1 = i1.next{{ capitalizedPrimitiveTypeName }}();
                    e2 = i2.next{{ capitalizedPrimitiveTypeName }}();
                    if ((r = ({{ wrapperClassName }}.compare((e1), (e2)))) != 0) return r;
                }
                return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
            }
            ListIterator<? extends {{ wrapperClassName }}> i1 = listIterator(), i2 = l.listIterator();
            int r;
            while (i1.hasNext() && i2.hasNext()) {
                if ((r = ((Comparable<? super {{ wrapperClassName }}>) i1.next()).compareTo(i2.next())) != 0) return r;
            }
            return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public void push(final {{ primitiveTypeName }} o) {
        wlock.lock();
        try {
            add(o);
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public {{ primitiveTypeName }} pop{{ capitalizedPrimitiveTypeName }}() {
        wlock.lock();
        try {
            if (isEmpty()) throw new NoSuchElementException();
            return remove{{ capitalizedPrimitiveTypeName }}(size() - 1);
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public {{ primitiveTypeName }} top{{ capitalizedPrimitiveTypeName }}() {
        rlock.lock();
        try {
            if (isEmpty()) throw new NoSuchElementException();
            return get{{ capitalizedPrimitiveTypeName }}(size() - 1);
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public {{ primitiveTypeName }} peek{{ capitalizedPrimitiveTypeName }}(final int i) {
        rlock.lock();
        try {
            return get{{ capitalizedPrimitiveTypeName }}(size() - 1 - i);
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Removes a single instance of the specified element from this collection, if it is present (optional operation).
     *
     * @implSpec This implementation delegates to {@code indexOf()}.
     * @see List#remove(Object)
     */
    @Override
    public boolean rem(final {{ primitiveTypeName }} k) {
        wlock.lock();
        try {
            int index = indexOf(k);
            if (index == -1) return false;
            remove{{ capitalizedPrimitiveTypeName }}(index);
            return true;
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public {{ primitiveTypeName }}[] to{{ capitalizedPrimitiveTypeName }}Array() {
        rlock.lock();
        try {
            final int size = size();
            if (size == 0) return {{ capitalizedPrimitiveTypeName }}Arrays.EMPTY_ARRAY;
            {{ primitiveTypeName }}[] ret = new {{ primitiveTypeName }}[size];
            getElements(0, ret, 0, size);
            return ret;
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public {{ primitiveTypeName }}[] toArray({{ primitiveTypeName }}[] a) {
        rlock.lock();
        try {
            final int size = size();
            if (a.length < size) {
                a = Arrays.copyOf(a, size);
            }
            getElements(0, a, 0, size);
            return a;
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public boolean addAll(int index, final {{ capitalizedPrimitiveTypeName }}Collection c) {
        wlock.lock();
        try {
            ensureIndex(index);
            final {{ capitalizedPrimitiveTypeName }}Iterator i = c.iterator();
            final boolean retVal = i.hasNext();
            while (i.hasNext()) add(index++, i.next{{ capitalizedPrimitiveTypeName }}());
            return retVal;
        } finally {
            wlock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to the type-specific version of {@link List#addAll(int, Collection)}.
     */
    @Override
    public boolean addAll(final {{ capitalizedPrimitiveTypeName }}Collection c) {
        wlock.lock();
        try {
            return addAll(size(), c);
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public String toString() {
        rlock.lock();
        try {
            final StringBuilder s = new StringBuilder();
            final {{ capitalizedPrimitiveTypeName }}Iterator i = iterator();
            int n = size();
            {{ primitiveTypeName }} k;
            boolean first = true;
            s.append("[");
            while (n-- != 0) {
                if (first) first = false;
                else s.append(", ");
                k = i.next{{ capitalizedPrimitiveTypeName }}();
                s.append(String.valueOf(k));
            }
            s.append("]");
            return s.toString();
        } finally {
            rlock.unlock();
        }
    }

    /**
     * A class implementing a sublist view.
     */
    public static class Concurrent{{ capitalizedPrimitiveTypeName }}SubList extends AbstractConcurrent{{ capitalizedPrimitiveTypeName }}List implements Serializable {
        /** The locks. */
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final Lock wlock = lock.writeLock();
        private final Lock rlock = lock.readLock();
        /**
         * The list this sublist restricts.
         */
        protected final {{ capitalizedPrimitiveTypeName }}List l;
        /**
         * Initial (inclusive) index of this sublist.
         */
        protected final int from;
        /**
         * Final (exclusive) index of this sublist.
         */
        protected int to;

        public Concurrent{{ capitalizedPrimitiveTypeName }}SubList(final {{ capitalizedPrimitiveTypeName }}List l, final int from, final int to) {
            this.l = l;
            this.from = from;
            this.to = to;
        }

        private boolean assertRange() {
            rlock.lock();
            try {
                assert from <= l.size();
                assert to <= l.size();
                assert to >= from;
                return true;
            } finally {
                rlock.unlock();
            }
        }

        @Override
        public boolean add(final {{ primitiveTypeName }} k) {
            wlock.lock();
            try {
                l.add(to, k);
                to++;
                assert assertRange();
                return true;
            } finally {
                wlock.unlock();
            }
        }

        @Override
        public void add(final int index, final {{ primitiveTypeName }} k) {
            wlock.lock();
            try {
                ensureIndex(index);
                l.add(from + index, k);
                to++;
                assert assertRange();
            } finally {
                wlock.unlock();
            }
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends {{ wrapperClassName }}> c) {
            wlock.lock();
            try {
                ensureIndex(index);
                to += c.size();
                return l.addAll(from + index, c);
            } finally {
                wlock.unlock();
            }
        }

        @Override
        public {{ primitiveTypeName }} get{{ capitalizedPrimitiveTypeName }}(final int index) {
            wlock.lock();
            try {
                ensureRestrictedIndex(index);
                return l.get{{ capitalizedPrimitiveTypeName }}(from + index);
            } finally {
                wlock.unlock();
            }
        }

        @Override
        public {{ primitiveTypeName }} remove{{ capitalizedPrimitiveTypeName }}(final int index) {
            wlock.lock();
            try {
                ensureRestrictedIndex(index);
                to--;
                return l.remove{{ capitalizedPrimitiveTypeName }}(from + index);
            } finally {
                wlock.unlock();
            }
        }

        @Override
        public {{ primitiveTypeName }} set(final int index, final {{ primitiveTypeName }} k) {
            wlock.lock();
            try {
                ensureRestrictedIndex(index);
                return l.set(from + index, k);
            } finally {
                wlock.unlock();
            }
        }

        @Override
        public int size() {
            rlock.lock();
            try {
                return to - from;
            } finally{
                rlock.unlock();
            }
        }

        @Override
        public void getElements(final int from, final {{ primitiveTypeName }}[] a, final int offset, final int length) {
            rlock.lock();
            try {
                ensureIndex(from);
                if (from + length > size())
                    throw new IndexOutOfBoundsException("End index (" + from + length + ") is greater than list size (" + size() + ")");
                l.getElements(this.from + from, a, offset, length);
            } finally {
                rlock.unlock();
            }
        }

        @Override
        public void removeElements(final int from, final int to) {
            wlock.lock();
            try {
                ensureIndex(from);
                ensureIndex(to);
                l.removeElements(this.from + from, this.from + to);
                this.to -= (to - from);
                assert assertRange();
            } finally {
                wlock.unlock();
            }
        }

        @Override
        public void addElements(int index, final {{ primitiveTypeName }}[] a, int offset, int length) {
            wlock.lock();
            try {
                ensureIndex(index);
                l.addElements(this.from + index, a, offset, length);
                this.to += length;
                assert assertRange();
            } finally {
                wlock.unlock();
            }
        }

        @Override
        public void setElements(int index, final {{ primitiveTypeName }}[] a, int offset, int length) {
            wlock.lock();
            try {
                ensureIndex(index);
                l.setElements(this.from + index, a, offset, length);
                assert assertRange();
            } finally {
                wlock.unlock();
            }
        }

        private final class RandomAccessIter extends {{ capitalizedPrimitiveTypeName }}Iterators.AbstractIndexBasedListIterator {
            // We don't set the minPos to be "from" because we need to call our containing class'
            // add, set, and remove methods with 0 relative to the start of the sublist, not the
            // start of the original list.
            // Thus pos is relative to the start of the SubList, not the start of the original list.
            RandomAccessIter(int pos) {
                super(0, pos);
            }

            @Override
            protected {{ primitiveTypeName }} get(int i) {
                return l.get{{ capitalizedPrimitiveTypeName }}(from + i);
            }

            // Remember, these are calling SUBLIST's methods, meaning 0 is the start of the sublist for these.
            @Override
            protected void add(int i, {{ primitiveTypeName }} k) {
                Concurrent{{ capitalizedPrimitiveTypeName }}SubList.this.add(i, k);
            }

            @Override
            protected void set(int i, {{ primitiveTypeName }} k) {
                Concurrent{{ capitalizedPrimitiveTypeName }}SubList.this.set(i, k);
            }

            @Override
            protected void remove(int i) {
                Concurrent{{ capitalizedPrimitiveTypeName }}SubList.this.remove{{ capitalizedPrimitiveTypeName }}(i);
            }

            @Override
            protected int getMaxPos() {
                rlock.lock();
                try {
                    return to - from;
                } finally {
                    rlock.unlock();
                }
            }

            @Override
            public void add({{ primitiveTypeName }} k) {
                super.add(k);
                assert assertRange();
            }

            @Override
            public void remove() {
                super.remove();
                assert assertRange();
            }
        }

        private class ParentWrappingIter implements {{ capitalizedPrimitiveTypeName }}ListIterator {
            private final {{ capitalizedPrimitiveTypeName }}ListIterator parent;

            ParentWrappingIter({{ capitalizedPrimitiveTypeName }}ListIterator parent) {
                this.parent = parent;
            }

            @Override
            public int nextIndex() {
                return parent.nextIndex() - from;
            }

            @Override
            public int previousIndex() {
                return parent.previousIndex() - from;
            }

            @Override
            public boolean hasNext() {
                return parent.nextIndex() < to;
            }

            @Override
            public boolean hasPrevious() {
                return parent.previousIndex() >= from;
            }

            @Override
            public {{ primitiveTypeName }} next{{ capitalizedPrimitiveTypeName }}() {
                if (!hasNext()) throw new NoSuchElementException();
                return parent.next{{ capitalizedPrimitiveTypeName }}();
            }

            @Override
            public {{ primitiveTypeName }} previous{{ capitalizedPrimitiveTypeName }}() {
                if (!hasPrevious()) throw new NoSuchElementException();
                return parent.previous{{ capitalizedPrimitiveTypeName }}();
            }

            @Override
            public void add({{ primitiveTypeName }} k) {
                parent.add(k);
            }

            @Override
            public void set({{ primitiveTypeName }} k) {
                parent.set(k);
            }

            @Override
            public void remove() {
                parent.remove();
            }

            @Override
            public int back(int n) {
                if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                int currentPos = parent.previousIndex();
                int parentNewPos = currentPos - n;
                // Remember, the minimum acceptable previousIndex is not from but (from - 1), since (from - 1)
                // means this subList is at the beginning of our sub range.
                // Same reason why previousIndex()'s minimum for the full list is not 0 but -1.
                if (parentNewPos < (from - 1)) parentNewPos = (from - 1);
                int toSkip = parentNewPos - currentPos;
                return parent.back(toSkip);
            }

            @Override
            public int skip(int n) {
                if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                int currentPos = parent.nextIndex();
                int parentNewPos = currentPos + n;
                if (parentNewPos > to) parentNewPos = to;
                int toSkip = parentNewPos - currentPos;
                return parent.skip(toSkip);
            }
        }

        @Override
        public {{ capitalizedPrimitiveTypeName }}ListIterator listIterator(final int index) {
            ensureIndex(index);
            // If this class wasn't public, then RandomAccessIter would live in SUBLISTRandomAccess,
            // and the switching would be done in sublist(int, int). However, this is a public class
            // that may have existing implementors, so to get the benefit of RandomAccessIter class for
            // for existing uses, it has to be done in this class.
            return l instanceof java.util.RandomAccess ? new RandomAccessIter(index) : new ParentWrappingIter(l.listIterator(index + from));
        }

        @Override
        public {{ capitalizedPrimitiveTypeName }}Spliterator spliterator() {
            return l instanceof java.util.RandomAccess ? new IndexBasedSpliterator(l, from, to) : super.spliterator();
        }

        @Override
        public {{ capitalizedPrimitiveTypeName }}List subList(final int from, final int to) {
            ensureIndex(from);
            ensureIndex(to);
            if (from > to)
                throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
            // Sadly we have to rewrap this, because if there is a sublist of a sublist, and the
            // subsublist adds, both sublists need to update their "to" value.
            return new Concurrent{{ capitalizedPrimitiveTypeName }}SubList(this, from, to);
        }

        @Override
        public boolean rem(final {{ primitiveTypeName }} k) {
            int index = indexOf(k);
            if (index == -1) return false;
            to--;
            l.remove{{ capitalizedPrimitiveTypeName }}(from + index);
            assert assertRange();
            return true;
        }

        @Override
        public boolean addAll(final int index, final {{ capitalizedPrimitiveTypeName }}Collection c) {
            ensureIndex(index);
            return super.addAll(index, c);
        }

        @Override
        public boolean addAll(final int index, final {{ capitalizedPrimitiveTypeName }}List l) {
            ensureIndex(index);
            return super.addAll(index, l);
        }
    }

    public static class Concurrent{{ capitalizedPrimitiveTypeName }}RandomAccessSubList extends Concurrent{{ capitalizedPrimitiveTypeName }}SubList implements RandomAccess {
        public Concurrent{{ capitalizedPrimitiveTypeName }}RandomAccessSubList(final {{ capitalizedPrimitiveTypeName }}List l, final int from, final int to) {
            super(l, from, to);
        }

        @Override
        public {{ capitalizedPrimitiveTypeName }}List subList(final int from, final int to) {
            ensureIndex(from);
            ensureIndex(to);
            if (from > to)
                throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
            // Sadly we have to rewrap this, because if there is a sublist of a sublist, and the
            // subsublist adds, both sublists need to update their "to" value.
            return new Concurrent{{ capitalizedPrimitiveTypeName }}RandomAccessSubList(this, from, to);
        }
    }
}
