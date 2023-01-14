package com.cleanroommc.fastutil.bytes;

import it.unimi.dsi.fastutil.bytes.*;
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
public abstract class AbstractConcurrentByteList extends AbstractByteCollection implements ByteList, ByteStack {
    /** The locks. */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock wlock = lock.writeLock();
    private final Lock rlock = lock.readLock();

    protected AbstractConcurrentByteList() {
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
    public void add(final int index, final byte k) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to the type-specific version of {@link List#add(int, Object)}.
     */
    @Override
    public boolean add(final byte k) {
        add(size(), k);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation always throws an {@link UnsupportedOperationException}.
     */
    @Override
    public byte removeByte(final int i) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation always throws an {@link UnsupportedOperationException}.
     */
    @Override
    public byte set(final int index, final byte k) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds all of the elements in the specified collection to this list (optional operation).
     */
    @Override
    public boolean addAll(int index, final Collection<? extends Byte> c) {
        if (c instanceof ByteCollection) {
            return addAll(index, (ByteCollection) c);
        }
        ensureIndex(index);
        final Iterator<? extends Byte> i = c.iterator();
        final boolean retVal = i.hasNext();
        while (i.hasNext()) add(index++, (i.next()).byteValue());
        return retVal;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to the type-specific version of {@link List#addAll(int, Collection)}.
     */
    @Override
    public boolean addAll(final Collection<? extends Byte> c) {
        return addAll(size(), c);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to {@link #listIterator()}.
     */
    @Override
    public ByteListIterator iterator() {
        return listIterator();
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation delegates to {@link #listIterator(int) listIterator(0)}.
     */
    @Override
    public ByteListIterator listIterator() {
        return listIterator(0);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation is based on the random-access methods.
     */
    @Override
    public ByteListIterator listIterator(final int index) {
        ensureIndex(index);
        return new ByteIterators.AbstractIndexBasedListIterator(0, index) {
            @Override
            protected byte get(int i) {
                return AbstractConcurrentByteList.this.getByte(i);
            }

            @Override
            protected void add(int i, byte k) {
                AbstractConcurrentByteList.this.add(i, k);
            }

            @Override
            protected void set(int i, byte k) {
                AbstractConcurrentByteList.this.set(i, k);
            }

            @Override
            protected void remove(int i) {
                AbstractConcurrentByteList.this.removeByte(i);
            }

            @Override
            protected int getMaxPos() {
                return AbstractConcurrentByteList.this.size();
            }
        };
    }

    static final class IndexBasedSpliterator extends ByteSpliterators.LateBindingSizeIndexBasedSpliterator {
        final ByteList l;

        IndexBasedSpliterator(ByteList l, int pos) {
            super(pos);
            this.l = l;
        }

        IndexBasedSpliterator(ByteList l, int pos, int maxPos) {
            super(pos, maxPos);
            this.l = l;
        }

        @Override
        protected int getMaxPosFromBackingStore() {
            return l.size();
        }

        @Override
        protected byte get(int i) {
            return l.getByte(i);
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
    public boolean contains(final byte k) {
        return indexOf(k) >= 0;
    }

    @Override
    public int indexOf(final byte k) {
        rlock.lock();
        try {
            final ByteListIterator i = listIterator();
            byte e;
            while(i.hasNext()){
                e = i.nextByte();
                if (((k)==(e))) return i.previousIndex();
            }
            return-1;
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public int lastIndexOf(final byte k) {
        rlock.lock();
        try {
            ByteListIterator i = listIterator(size());
            byte e;
            while (i.hasPrevious()) {
                e = i.previousByte();
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
            if (size > i) while (i++ < size) add(((byte)0));
            else while (i-- != size) removeByte(i);
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public ByteList subList(final int from, final int to) {
        rlock.lock();
        try {
            ensureIndex(from);
            ensureIndex(to);
            if (from > to)
                throw new IndexOutOfBoundsException("Start index (" + from + ") is greater than end index (" + to + ")");
            return this instanceof java.util.RandomAccess ? new ConcurrentByteRandomAccessSubList(this, from, to) : new ConcurrentByteSubList(this, from, to);
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
    public void forEach(final ByteConsumer action) {
        rlock.lock();
        try {
            if (this instanceof java.util.RandomAccess) {
                for (int i = 0, max = size(); i < max; ++i) {
                    action.accept(getByte(i));
                }
            } else {
                ByteList.super.forEach(action);
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
            ByteListIterator i = listIterator(from);
            int n = to - from;
            if (n < 0)
                throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
            while (n-- != 0) {
                i.nextByte();
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
    public void addElements(int index, final byte[] a, int offset, int length) {
        wlock.lock();
        try {
            ensureIndex(index);
            ByteArrays.ensureOffsetLength(a, offset, length);
            if (this instanceof java.util.RandomAccess) {
                while (length-- != 0) add(index++, a[offset++]);
            } else {
                ByteListIterator iter = listIterator(index);
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
    public void addElements(final int index, final byte[] a) {
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
    public void getElements(final int from, final byte[] a, int offset, int length) {
        rlock.lock();
        try {
            ensureIndex(from);
            ByteArrays.ensureOffsetLength(a, offset, length);
            if (from + length > size())
                throw new IndexOutOfBoundsException("End index (" + (from + length) + ") is greater than list size (" + size() + ")");
            if (this instanceof java.util.RandomAccess) {
                int current = from;
                while (length-- != 0) a[offset++] = getByte(current++);
            } else {
                ByteListIterator i = listIterator(from);
                while (length-- != 0) a[offset++] = i.nextByte();
            }
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public void setElements(int index, byte[] a, int offset, int length) {
        wlock.lock();
        try {
            ensureIndex(index);
            ByteArrays.ensureOffsetLength(a, offset, length);
            if (index + length > size())
                throw new IndexOutOfBoundsException("End index (" + (index + length) + ") is greater than list size (" + size() + ")");
            if (this instanceof java.util.RandomAccess) {
                for (int i = 0; i < length; ++i) {
                    set(i + index, a[i + offset]);
                }
            } else {
                ByteListIterator iter = listIterator(index);
                int i = 0;
                while (i < length) {
                    iter.nextByte();
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
            ByteIterator i = iterator();
            int h = 1, s = size();
            while (s-- != 0) {
                byte k = i.nextByte();
                h = 31 * h + Byte.hashCode(k);
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
            if (l instanceof ByteList) {
                final ByteListIterator i1 = listIterator(), i2 = ((ByteList) l).listIterator();
                while (s-- != 0) if (i1.nextByte() != i2.nextByte()) return false;
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
    public int compareTo(final List<? extends Byte> l) {
        rlock.lock();
        try {
            if (l == this) return 0;
            if (l instanceof ByteList) {
                final ByteListIterator i1 = listIterator(), i2 = ((ByteList) l).listIterator();
                int r;
                byte e1, e2;
                while (i1.hasNext() && i2.hasNext()) {
                    e1 = i1.nextByte();
                    e2 = i2.nextByte();
                    if ((r = (Byte.compare((e1), (e2)))) != 0) return r;
                }
                return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
            }
            ListIterator<? extends Byte> i1 = listIterator(), i2 = l.listIterator();
            int r;
            while (i1.hasNext() && i2.hasNext()) {
                if ((r = ((Comparable<? super Byte>) i1.next()).compareTo(i2.next())) != 0) return r;
            }
            return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public void push(final byte o) {
        wlock.lock();
        try {
            add(o);
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public byte popByte() {
        wlock.lock();
        try {
            if (isEmpty()) throw new NoSuchElementException();
            return removeByte(size() - 1);
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public byte topByte() {
        rlock.lock();
        try {
            if (isEmpty()) throw new NoSuchElementException();
            return getByte(size() - 1);
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public byte peekByte(final int i) {
        rlock.lock();
        try {
            return getByte(size() - 1 - i);
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
    public boolean rem(final byte k) {
        wlock.lock();
        try {
            int index = indexOf(k);
            if (index == -1) return false;
            removeByte(index);
            return true;
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public byte[] toByteArray() {
        rlock.lock();
        try {
            final int size = size();
            if (size == 0) return ByteArrays.EMPTY_ARRAY;
            byte[] ret = new byte[size];
            getElements(0, ret, 0, size);
            return ret;
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public byte[] toArray(byte[] a) {
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
    public boolean addAll(int index, final ByteCollection c) {
        wlock.lock();
        try {
            ensureIndex(index);
            final ByteIterator i = c.iterator();
            final boolean retVal = i.hasNext();
            while (i.hasNext()) add(index++, i.nextByte());
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
    public boolean addAll(final ByteCollection c) {
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
            final ByteIterator i = iterator();
            int n = size();
            byte k;
            boolean first = true;
            s.append("[");
            while (n-- != 0) {
                if (first) first = false;
                else s.append(", ");
                k = i.nextByte();
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
    public static class ConcurrentByteSubList extends AbstractConcurrentByteList implements Serializable {
        /** The locks. */
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final Lock wlock = lock.writeLock();
        private final Lock rlock = lock.readLock();
        /**
         * The list this sublist restricts.
         */
        protected final ByteList l;
        /**
         * Initial (inclusive) index of this sublist.
         */
        protected final int from;
        /**
         * Final (exclusive) index of this sublist.
         */
        protected int to;

        public ConcurrentByteSubList(final ByteList l, final int from, final int to) {
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
        public boolean add(final byte k) {
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
        public void add(final int index, final byte k) {
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
        public boolean addAll(final int index, final Collection<? extends Byte> c) {
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
        public byte getByte(final int index) {
            wlock.lock();
            try {
                ensureRestrictedIndex(index);
                return l.getByte(from + index);
            } finally {
                wlock.unlock();
            }
        }

        @Override
        public byte removeByte(final int index) {
            wlock.lock();
            try {
                ensureRestrictedIndex(index);
                to--;
                return l.removeByte(from + index);
            } finally {
                wlock.unlock();
            }
        }

        @Override
        public byte set(final int index, final byte k) {
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
        public void getElements(final int from, final byte[] a, final int offset, final int length) {
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
        public void addElements(int index, final byte[] a, int offset, int length) {
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
        public void setElements(int index, final byte[] a, int offset, int length) {
            wlock.lock();
            try {
                ensureIndex(index);
                l.setElements(this.from + index, a, offset, length);
                assert assertRange();
            } finally {
                wlock.unlock();
            }
        }

        private final class RandomAccessIter extends ByteIterators.AbstractIndexBasedListIterator {
            // We don't set the minPos to be "from" because we need to call our containing class'
            // add, set, and remove methods with 0 relative to the start of the sublist, not the
            // start of the original list.
            // Thus pos is relative to the start of the SubList, not the start of the original list.
            RandomAccessIter(int pos) {
                super(0, pos);
            }

            @Override
            protected byte get(int i) {
                return l.getByte(from + i);
            }

            // Remember, these are calling SUBLIST's methods, meaning 0 is the start of the sublist for these.
            @Override
            protected void add(int i, byte k) {
                ConcurrentByteSubList.this.add(i, k);
            }

            @Override
            protected void set(int i, byte k) {
                ConcurrentByteSubList.this.set(i, k);
            }

            @Override
            protected void remove(int i) {
                ConcurrentByteSubList.this.removeByte(i);
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
            public void add(byte k) {
                super.add(k);
                assert assertRange();
            }

            @Override
            public void remove() {
                super.remove();
                assert assertRange();
            }
        }

        private class ParentWrappingIter implements ByteListIterator {
            private final ByteListIterator parent;

            ParentWrappingIter(ByteListIterator parent) {
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
            public byte nextByte() {
                if (!hasNext()) throw new NoSuchElementException();
                return parent.nextByte();
            }

            @Override
            public byte previousByte() {
                if (!hasPrevious()) throw new NoSuchElementException();
                return parent.previousByte();
            }

            @Override
            public void add(byte k) {
                parent.add(k);
            }

            @Override
            public void set(byte k) {
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
        public ByteListIterator listIterator(final int index) {
            ensureIndex(index);
            // If this class wasn't public, then RandomAccessIter would live in SUBLISTRandomAccess,
            // and the switching would be done in sublist(int, int). However, this is a public class
            // that may have existing implementors, so to get the benefit of RandomAccessIter class for
            // for existing uses, it has to be done in this class.
            return l instanceof java.util.RandomAccess ? new RandomAccessIter(index) : new ParentWrappingIter(l.listIterator(index + from));
        }

        @Override
        public ByteSpliterator spliterator() {
            return l instanceof java.util.RandomAccess ? new IndexBasedSpliterator(l, from, to) : super.spliterator();
        }

        @Override
        public ByteList subList(final int from, final int to) {
            ensureIndex(from);
            ensureIndex(to);
            if (from > to)
                throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
            // Sadly we have to rewrap this, because if there is a sublist of a sublist, and the
            // subsublist adds, both sublists need to update their "to" value.
            return new ConcurrentByteSubList(this, from, to);
        }

        @Override
        public boolean rem(final byte k) {
            int index = indexOf(k);
            if (index == -1) return false;
            to--;
            l.removeByte(from + index);
            assert assertRange();
            return true;
        }

        @Override
        public boolean addAll(final int index, final ByteCollection c) {
            ensureIndex(index);
            return super.addAll(index, c);
        }

        @Override
        public boolean addAll(final int index, final ByteList l) {
            ensureIndex(index);
            return super.addAll(index, l);
        }
    }

    public static class ConcurrentByteRandomAccessSubList extends ConcurrentByteSubList implements RandomAccess {
        public ConcurrentByteRandomAccessSubList(final ByteList l, final int from, final int to) {
            super(l, from, to);
        }

        @Override
        public ByteList subList(final int from, final int to) {
            ensureIndex(from);
            ensureIndex(to);
            if (from > to)
                throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
            // Sadly we have to rewrap this, because if there is a sublist of a sublist, and the
            // subsublist adds, both sublists need to update their "to" value.
            return new ConcurrentByteRandomAccessSubList(this, from, to);
        }
    }
}
