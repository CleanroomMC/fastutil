package {{ typePackagePath }};

import it.unimi.dsi.fastutil.{{ primitiveTypeName }}.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.*;

/** A type-specific concurrent array-based list; provides some additional methods that use polymorphism to avoid (un)boxing.

  *

  * <p>This class implements a lightweight, fast, open, optimized,

  * reuse-oriented version of array-based lists. Instances of this class

  * represent a list with an array that is enlarged as needed when new entries

  * are created (by increasing its current length by 50%), but is

  * <em>never</em> made smaller (even on a {@link #clear()}). A family of

  * {@linkplain #trim() trimming methods} lets you control the size of the

  * backing array; this is particularly useful if you reuse instances of this class.

  * Range checks are equivalent to those of {@code java.util}'s classes, but

  * they are delayed as much as possible. The backing array is exposed by the

  * {@link #elements()} method.

  *

  * <p>This class implements the bulk methods {@code removeElements()},

  * {@code addElements()} and {@code getElements()} using

  * high-performance system calls (e.g., {@link

  * System#arraycopy(Object,int,Object,int,int) System.arraycopy()}) instead of

  * expensive loops.

  *

  * @see java.util.ArrayList

  */
public class {{ className }} extends Abstract{{ capitalizedPrimitiveTypeName }}List implements RandomAccess, Cloneable, Serializable {
    /** The locks. */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final WriteLock wlock = lock.writeLock();
    private final ReadLock rlock = lock.readLock();
    /** The initial default capacity of an array list. */
    public static final int DEFAULT_INITIAL_CAPACITY = 10;
    /** The backing array. */
    protected transient {{ primitiveTypeName }} a[];
    /** The current actual size of the list (never greater than the backing-array length). */
    protected int size;
    /** Ensures that the component type of the given array is the proper type.

     * This is irrelevant for primitive types, so it will just do a trivial copy.

     * But for Reference types, you can have a {@code String[]} masquerading as an {@code Object[]},

     * which is a case we need to prepare for because we let the user give an array to use directly

     * with {@link #wrap}.

     */

    private static final {{ primitiveTypeName }}[] copyArraySafe({{ primitiveTypeName }}[] a, int length) {
    	if (length == 0) return {{ capitalizedPrimitiveTypeName }}Arrays.EMPTY_ARRAY;
    	return java.util.Arrays.copyOf(a, length);
    }

    private static final {{ primitiveTypeName }}[] copyArrayFromSafe({{ className }} l) {
    	return copyArraySafe(l.a, l.size);
    }

    /** Creates a new array list using a given array.

     *

     * <p>This constructor is only meant to be used by the wrapping methods.

     *

     * @param a the array that will be used to back this array list.

     */
    protected {{ className }}(final {{ primitiveTypeName }} a[], @SuppressWarnings("unused") boolean wrapped) {
    	this.a = a;
    }

    private void initArrayFromCapacity(final int capacity) {
    	if (capacity < 0) throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
    	if (capacity == 0) a = {{ capitalizedPrimitiveTypeName }}Arrays.EMPTY_ARRAY;
    	else a = new {{ primitiveTypeName }}[capacity];
    }

    /** Creates a new array list with given capacity.

     *

     * @param capacity the initial capacity of the array list (may be 0).

     */
	public {{ className }}(final int capacity) {
	    initArrayFromCapacity(capacity);
	}

    /** Creates a new array list with {@link #DEFAULT_INITIAL_CAPACITY} capacity. */
	public {{ className }}() {
	    a = {{ capitalizedPrimitiveTypeName }}Arrays.DEFAULT_EMPTY_ARRAY; // We delay allocation
	}

    /** Creates a new array list and fills it with a given collection.

     *

     * @param c a collection that will be used to fill the array list.

     */
	public {{ className }}(final Collection<? extends {{ wrapperClassName }}> c) {
        if (c instanceof {{ capitalizedPrimitiveTypeName }}ArrayList) {
            a = copyArrayFromSafe(({{ capitalizedPrimitiveTypeName }}ArrayList) c);
            size = a.length;
	    } else {
            initArrayFromCapacity(c.size());
            if (c instanceof {{ capitalizedPrimitiveTypeName }}List) {
                (({{ capitalizedPrimitiveTypeName }}List) c).getElements(0, a, 0, size = c.size());
            } else {
                size = {{ capitalizedPrimitiveTypeName }}Iterators.unwrap({{ capitalizedPrimitiveTypeName }}Iterators.as{{ capitalizedPrimitiveTypeName }}Iterator(c.iterator()), a);
            }
	    }
	}

    /** Creates a new array list and fills it with a given type-specific collection.

     *

     * @param c a type-specific collection that will be used to fill the array list.

     */
	public {{ className }}(final {{ capitalizedPrimitiveTypeName }}Collection c) {
	    if (c instanceof {{ capitalizedPrimitiveTypeName }}ArrayList) {
	        a = copyArrayFromSafe(({{ capitalizedPrimitiveTypeName }}ArrayList) c);
	        size = a.length;
	    } else {
            initArrayFromCapacity(c.size());
            if (c instanceof {{ capitalizedPrimitiveTypeName }}List) {
                (({{ capitalizedPrimitiveTypeName }}List) c).getElements(0, a, 0, size = c.size());
            } else {
                size = {{ capitalizedPrimitiveTypeName }}Iterators.unwrap(c.iterator(), a);
            }
	    }
	}

    /** Creates a new array list and fills it with a given type-specific list.

     *

     * @param l a type-specific list that will be used to fill the array list.

     */
	public {{ className }}(final {{ capitalizedPrimitiveTypeName }}List l) {
	    if (l instanceof {{ capitalizedPrimitiveTypeName }}ArrayList) {
	        a = copyArrayFromSafe(({{ capitalizedPrimitiveTypeName }}ArrayList) l);
	        size = a.length;
	    } else {
	        initArrayFromCapacity(l.size());
	        l.getElements(0, a, 0, size = l.size());
	    }
	}

    /** Creates a new array list and fills it with the elements of a given array.

     *

     * @param a an array whose elements will be used to fill the array list.

     */
	public {{ className }}(final {{ primitiveTypeName }} a[]) {
	    this(a, 0, a.length);
	}

    /** Creates a new array list and fills it with the elements of a given array.

     *

     * @param a an array whose elements will be used to fill the array list.

     * @param offset the first element to use.

     * @param length the number of elements to use.

     */
	public {{ className }}(final {{ primitiveTypeName }} a[], final int offset, final int length) {
         this(length);
         System.arraycopy(a, offset, this.a, 0, length);
         size = length;
	}

    /** Creates a new array list and fills it with the elements returned by an iterator..

     *

     * @param i an iterator whose returned elements will fill the array list.

     */
	public {{ className }}(final Iterator<? extends {{ wrapperClassName }}> i) {
         this();
         while(i.hasNext()) this.add((i.next()).{{ primitiveTypeName }}Value());
	}

    /** Creates a new array list and fills it with the elements returned by a type-specific iterator..

     *

     * @param i a type-specific iterator whose returned elements will fill the array list.

     */
	public {{ className }}(final {{ capitalizedPrimitiveTypeName }}Iterator i) {
         this();
         while(i.hasNext()) this.add(i.next{{ wrapperClassName }}());
	}

	/** Returns the snapshot of backing array of this list.

     *

     * @return the backing array.

     */
	public {{ primitiveTypeName }}[] elements() {
	    rlock.lock();
	    try {
            return Arrays.copyOf(a, size);
	    } finally {
	        rlock.unlock();
	    }
	}

	/** Wraps a given array into an array list of given size.

     *

     * <p>Note it is guaranteed

     * that the type of the array returned by {@link #elements()} will be the same

     * (see the comments in the class documentation).

     *

     * @param a an array to wrap.

     * @param length the length of the resulting array list.

     * @return a new array list of the given size, wrapping the given array.

     */
	public static {{ className }} wrap(final {{ primitiveTypeName }} a[], final int length) {
	    if (length > a.length) throw new IllegalArgumentException("The specified length (" + length + ") is greater than the array size (" + a.length + ")");
	    final {{ className }} l = new {{ className }}(a, true);
	    l.size = length;
	    return l;
	}

	/** Wraps a given array into an array list.

     *

     * <p>Note it is guaranteed

     * that the type of the array returned by {@link #elements()} will be the same

     * (see the comments in the class documentation).

     *

     * @param a an array to wrap.

     * @return a new array list wrapping the given array.

     */
	public static {{ className }} wrap(final {{ primitiveTypeName }} a[]) {
	    return wrap(a, a.length);
	}

	/** Creates a new empty array list.

     *

     * @return a new empty array list.

     */
	public static {{ className }} of() {
	    return new {{ className }}();
	}

	/** Creates an array list using an array of elements.

     *

     * @param init a the array the will become the new backing array of the array list.

     * @return a new array list backed by the given array.

     * @see #wrap

     */
	public static {{ className }} of(final {{ primitiveTypeName }}... init) {
	    return wrap(init);
	}

	/** Ensures that this array list can contain the given number of entries without resizing.

	 *

	 * @param capacity the new minimum capacity for this array list.

     */
	public void ensureCapacity(final int capacity) {
	    wlock.lock();
	    try {
	        if (capacity <= a.length || (a == {{ capitalizedPrimitiveTypeName }}Arrays.DEFAULT_EMPTY_ARRAY && capacity <= DEFAULT_INITIAL_CAPACITY)) return;
	        a = {{ capitalizedPrimitiveTypeName }}Arrays.ensureCapacity(a, capacity, size);
        } finally {
            assert size <= a.length;
            wlock.unlock();
	    }
	}

	/** Grows this array list, ensuring that it can contain the given number of entries without resizing,

	  * and in case increasing the current capacity at least by a factor of 50%.

	  *

	  * This method assumes the list is being locked by write lock.

	  *

	  * @param capacity the new minimum capacity for this array list.

	  */
	private void grow(int capacity) {
	    if (capacity <= a.length) return;
        if (a != {{ capitalizedPrimitiveTypeName }}Arrays.DEFAULT_EMPTY_ARRAY)
            capacity = (int)Math.max(Math.min((long)a.length + (a.length >> 1), it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE), capacity);
        else if (capacity < DEFAULT_INITIAL_CAPACITY) capacity = DEFAULT_INITIAL_CAPACITY;
        a = {{ capitalizedPrimitiveTypeName }}Arrays.forceCapacity(a, capacity, size);
        assert size <= a.length;
	}

	@Override
	public void add(final int index, final {{ primitiveTypeName }} k) {
	    wlock.lock();
	    try {
            ensureIndex(index);
            grow(size + 1);
            if (index != size) System.arraycopy(a, index, a, index + 1, size - index);
            a[index] = k;
            size++;
	    } finally {
	        assert size <= a.length;
	        wlock.unlock();
	    }
	}

	@Override
	public boolean add(final {{ primitiveTypeName }} k) {
	    wlock.lock();
	    try {
	        grow(size + 1);
	        a[size++] = k;
	        return true;
	    } finally {
	        assert size <= a.length;
	        wlock.unlock();
	    }
	}

	@Override
	public {{ primitiveTypeName }} get{{ capitalizedPrimitiveTypeName }}(final int index) {
	    rlock.lock();
	    try {
            if (index >= size) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
            return a[index];
        } finally {
            rlock.unlock();
        }
	}

	@Override
	public int indexOf(final {{ primitiveTypeName }} k) {
	    rlock.lock();
        try {
	        for(int i = 0; i < size; i++) if (( (k) == (a[i]) )) return i;
            return -1;
	    } finally {
	        rlock.unlock();
	    }
	}

	@Override
	public int lastIndexOf(final {{ primitiveTypeName }} k) {
	    rlock.lock();
        try {
	        for(int i = size; i-- != 0;) if (( (k) == (a[i]) )) return i;
	        return -1;
        } finally {
            rlock.unlock();
        }
	}

	private {{ primitiveTypeName }} remove(final int index) {
	    if (index >= size) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        final {{ primitiveTypeName }} old = a[index];
        size--;
        if (index != size) System.arraycopy(a, index + 1, a, index, size - index);
        assert size <= a.length;
        return old;
	}

	@Override
	public {{ primitiveTypeName }} remove{{ capitalizedPrimitiveTypeName }}(final int index) {
	    wlock.lock();
	    try {
	        return remove(index);
	    } finally {
	        wlock.unlock();
	    }
	}

	@Override
	public boolean rem(final {{ primitiveTypeName }} k) {
        wlock.lock();
        try {
            int index = indexOf(k);
            if (index == -1) return false;
            remove(index);
            return true;
	    } finally {
	        assert size <= a.length;
	        wlock.unlock();
	    }
	}

	@Override
	public {{ primitiveTypeName }} set(final int index, final {{ primitiveTypeName }} k) {
        wlock.lock();
        try {
            if (index >= size) throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
            {{ primitiveTypeName }} old = a[index];
            a[index] = k;
            return old;
	    } finally {
	        wlock.unlock();
	    }
	}

	@Override
	public void clear() {
	    wlock.lock();
	    try {
	        size = 0;
	    } finally {
	        assert size <= a.length;
	        wlock.unlock();
	    }
	}

	@Override
	public int size() {
	    rlock.lock();
	    try {
	        return size;
        } finally {
            rlock.unlock();
        }
	}

	@Override
	public void size(final int size) {
	    wlock.lock();
	    try {
	        if (size > a.length) a = {{ capitalizedPrimitiveTypeName }}Arrays.forceCapacity(a, size, this.size);
            if (size > this.size) Arrays.fill(a, this.size, size, (false));
            this.size = size;
	    } finally {
            wlock.unlock();
	    }
	}

	@Override
	public boolean isEmpty() {
	    rlock.lock();
	    try {
	        return size == 0;
	    } finally {
	        rlock.unlock();
	    }
	}

	private void trimArray(final int n) {
	    if (n >= a.length || size == a.length) return;
        final {{ primitiveTypeName }} t[] = new {{ primitiveTypeName }}[Math.max(n, size)];
        System.arraycopy(a, 0, t, 0, size);
        a = t;
        assert size <= a.length;
	}

	/** Trims this array list so that the capacity is equal to the size.

	  *

	  * @see java.util.ArrayList#trimToSize()

	  */
	public void trim() {
	    wlock.lock();
	    try {
	        trimArray(0);
	    } finally {
	        wlock.unlock();
	    }
	}

	/** Trims the backing array if it is too large.

	  *

	  * If the current array length is smaller than or equal to

	  * {@code n}, this method does nothing. Otherwise, it trims the

	  * array length to the maximum between {@code n} and {@link #size()}.

	  *

	  * <p>This method is useful when reusing lists.  {@linkplain #clear() Clearing a

	  * list} leaves the array length untouched. If you are reusing a list

	  * many times, you can call this method with a typical

	  * size to avoid keeping around a very large array just

	  * because of a few large transient lists.

	  *

	  * @param n the threshold for the trimming.

	  */
	public void trim(final int n) {
	    wlock.lock();
	    try {
	        trimArray(n);
	    } finally {
	        wlock.unlock();
	    }
	}

    private class ConcurrentSubList extends AbstractConcurrent{{ capitalizedPrimitiveTypeName }}List.Concurrent{{ capitalizedPrimitiveTypeName }}RandomAccessSubList {
		/** The locks. */
		private final ReadWriteLock lock = new ReentrantReadWriteLock();
		private final WriteLock wlock = lock.writeLock();
		private final ReadLock rlock = lock.readLock();

        protected ConcurrentSubList(int from, int to) {
            super({{ className }}.this.elements(), from, to);
        }

        // Most of the inherited methods should be fine, but we can override a few of them for performance.
        // Needed because we can't access the parent class' instance variables directly in a different instance of SubList.
        private boolean[] getParentArray() {
            rlock.lock();
            try {
                return a;
            } finally {
                rlock.unlock();
            }
        }

        @Override
        public {{ primitiveTypeName }} get{{ capitalizedPrimitiveTypeName }}(int i) {
            rlock.lock();
            try {
                ensureRestrictedIndex(i);
                return a[i + from];
            } finally {
                rlock.unlock();
            }
        }

		private final class ConcurrentSubListIterator extends {{ captilizedPrimitiveTypeName }}Iterators.AbstractIndexBasedListIterator {
            // We are using pos == 0 to be 0 relative to SubList.from (meaning you need to do a[from + i] when accessing array).
            SubListIterator(int index) {
                super(0, index);
            }

            @Override
            protected boolean get(int i) {
                return a[from + i];
            }

            @Override
            protected void add(int i, boolean k) {
				ConcurrentSubList.wlock.lock();
				try {
					ConcurrentSubList.this.add(i, k);
				} finally {
					ConcurrentSubList.this.wlock.unlock();
				}
            }

            @Override
            protected void set(int i, boolean k) {
				ConcurrentSubList.wlock.lock();
				try {
					ConcurrentSubList.this.set(i, k);
				} finally {
					ConcurrentSubList.this.wlock.unlock();
				}
            }

            @Override
            protected void remove(int i) {
                ConcurrentSubList.wlock.lock();
				try {
					ConcurrentSubList.this.remove(i);
				} finally {
					ConcurrentSubList.this.wlock.unlock();
				}
            }

            @Override
            protected int getMaxPos() {
                return to - from;
            }

            @Override
            public boolean nextBoolean() {
                if (!hasNext()) throw new NoSuchElementException();
                return a[from + (lastReturned = pos++)];
            }

            @Override
            public boolean previousBoolean() {
                if (!hasPrevious()) throw new NoSuchElementException();
                return a[from + (lastReturned = --pos)];
            }

            @Override
            public void forEachRemaining(final BooleanConsumer action) {
                final int max = to - from;
                while (pos < max) {
                    action.accept(a[from + (lastReturned = pos++)]);
                }
            }
        }

        @Override
        public BooleanListIterator listIterator(int index) {
            return new SubListIterator(index);
        }

        private final class SubListSpliterator extends BooleanSpliterators.LateBindingSizeIndexBasedSpliterator {
            // We are using pos == 0 to be 0 relative to real array 0
            SubListSpliterator() {
                super(from);
            }

            private SubListSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            protected int getMaxPosFromBackingStore() {
                return to;
            }

            @Override
            protected boolean get(int i) {
                return a[i];
            }

            @Override
            protected SubListSpliterator makeForSplit(int pos, int maxPos) {
                return new SubListSpliterator(pos, maxPos);
            }

            @Override
            public boolean tryAdvance(final BooleanConsumer action) {
                if (pos >= getMaxPos()) return false;
                action.accept(a[pos++]);
                return true;
            }

            @Override
            public void forEachRemaining(final BooleanConsumer action) {
                final int max = getMaxPos();
                while (pos < max) {
                    action.accept(a[pos++]);
                }
            }
        }

        @Override
        public BooleanSpliterator spliterator() {
            return new SubListSpliterator();
        }

        boolean contentsEquals(boolean[] otherA, int otherAFrom, int otherATo) {
            if (a == otherA && from == otherAFrom && to == otherATo) return true;
            if (otherATo - otherAFrom != size()) {
                return false;
            }
            int pos = from, otherPos = otherAFrom;
            // We have already assured that the two ranges are the same size, so we only need to check one bound.
            // TODO When minimum version of Java becomes Java 9, use the Arrays.equals which takes bounds, which is vectorized.
            // Make sure to split out the reference equality case when you do this.
            while (pos < to) if (a[pos++] != otherA[otherPos++]) return false;
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null) return false;
            if (!(o instanceof java.util.List)) return false;
            if (o instanceof BooleanArrayList other) {

                return contentsEquals(other.a, 0, other.size());
            }
            if (o instanceof SubList other) {

                return contentsEquals(other.getParentArray(), other.from, other.to);
            }
            return super.equals(o);
        }

        int contentsCompareTo(boolean[] otherA, int otherAFrom, int otherATo) {
            if (a == otherA && from == otherAFrom && to == otherATo) return 0;
            // TODO When minimum version of Java becomes Java 9, use Arrays.compare, which vectorizes.
            boolean e1, e2;
            int r, i, j;
            for (i = from, j = otherAFrom; i < to && i < otherATo; i++, j++) {
                e1 = a[i];
                e2 = otherA[j];
                if ((r = (Boolean.compare((e1), (e2)))) != 0) return r;
            }
            return i < otherATo ? -1 : (i < to ? 1 : 0);
        }

        @Override
        public int compareTo(final java.util.List<? extends Boolean> l) {
            if (l instanceof BooleanArrayList other) {

                return contentsCompareTo(other.a, 0, other.size());
            }
            if (l instanceof SubList other) {

                return contentsCompareTo(other.getParentArray(), other.from, other.to);
            }
            return super.compareTo(l);
        }
        // We don't override subList as we want AbstractList's "sub-sublist" nesting handling,
        // which would be tricky to do here.
        // TODO Do override it so array access isn't sent through N indirections.
        // This will likely mean making this class static.
    }
}
