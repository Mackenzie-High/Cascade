package com.mackenziehigh.cascade.internal.messages;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.IntStream;

/**
 * TODO:
 * Add support for spaghetti-stack nodes from other allocators.
 * Finish free logic.
 * Add out-of-memory detection in allocations.
 */
public final class CellularAllocator
{

    /**
     * This is the offset of the LIFETIME header-field,
     * which is simply a counter used to prevent stale
     * pointers from being used to access cells.
     */
    private final int OFFSET_LIFE = 0;

    /**
     * This is the offset of the REF_COUNT header-field,
     * which is used to count the number of references
     * to a cell for memory allocation/deallocation purposes.
     *
     * <p>
     * When the count reaches zero, the cell will be deallocated.
     * </p>
     */
    private final int OFFSET_REFS = 1;

    /**
     * This is the offset of the SIZE header-field,
     * which is used to store the number of data bytes
     * allocated in a single allocation.
     *
     * <p>
     * If the number of bytes exceeds the data capacity of a cell,
     * then the data will be split across multiple cells, as needed.
     * The NEXT header-field will be used to point to the next cell
     * in the linked series of cells.
     * </p>
     */
    private final int OFFSET_SIZE = 2;

    /**
     * This is the offset of the NEXT header-field,
     * which is used to combine a series of cells
     * into a singly linked-list of cells in order
     * to be able to store more data than a single
     * cell is capable of holding on its own.
     */
    private final int OFFSET_NEXT = 3;

    /**
     * This is the offset of the first data-storage slot in a cell.
     * This comes immediately after the last header-field.
     */
    private final int OFFSET_DATA = 4;

    /**
     * This is the number of array elements used to store the header-fields.
     * Note: OFFSET_DATA is not a header-field.
     */
    private final int HEADER_SIZE = 4;

    /**
     * This is the maximum number of cells managed by this allocator at one time.
     */
    private final int cellCount;

    /**
     * This is the maximum number of data bytes that can be stored in one cell.
     */
    private final int maximumBytesPerCell;

    /**
     * This is the number of array elements used to store a single cell.
     */
    private final int slotsPerCell;

    /**
     * This is the number of data-storage slots in each cell.
     */
    private final int dataSlotsPerCell;

    /**
     * This array contains the cells.
     * Each cell maps onto a finite contiguous series of array-elements.
     * The first elements in each cell are the header-fields.
     * The trailing elements are the data-storage slots.
     */
    private final AtomicIntegerArray heap;

    /**
     * This is effectively a pointer to the top cell on
     * a stack of free cells, where the NEXT header-field
     * is used to link each cell to the next one on the stack.
     */
    private volatile int free;

    /**
     * Sole Constructor.
     *
     * @param cellCount is the maximum number of cells.
     * @param cellSize is the maximum amount of data (bytes) per cell.
     */
    public CellularAllocator (final int cellCount,
                              final int cellSize)
    {
        Preconditions.checkArgument(cellCount >= 1);
        Preconditions.checkArgument(cellSize % 4 == 0);

        this.cellCount = cellCount;
        this.maximumBytesPerCell = cellSize;
        this.dataSlotsPerCell = cellSize / 4;
        this.slotsPerCell = HEADER_SIZE + dataSlotsPerCell;
        this.heap = new AtomicIntegerArray(cellCount * slotsPerCell);

        /**
         * All of the cells are initially free.
         * Skip the first cell, since index zero is equivalent to NULL.
         */
        for (int i = cellCount - 2; i > 0; i--)
        {
            heap.set(i * slotsPerCell + OFFSET_NEXT, (i + 1) * slotsPerCell);
        }
        free = 1 * slotsPerCell;
    }

    /**
     * Use this method to allocate a block of memory.
     *
     * @param buffer contains the data to place into the block.
     * @param offset is the start-position of the data in the buffer.
     * @param length is the length of the data in the buffer.
     * @param below is a pointer to the next node on a spaghetti-stack.
     * @return a pointer to the new block of memory,
     * or a negative number. if allocation failed.
     */
    public long alloc (final byte[] buffer,
                       final int offset,
                       final int length,
                       final long below)
    {
        /**
         * This is the number of cells needed to hold the data.
         */
        final long count = (length % maximumBytesPerCell) == 0
                ? (length / maximumBytesPerCell)
                : (length / maximumBytesPerCell) + 1;

        /**
         * This is the pointer that will be returned.
         */
        long ptr = 0;

        /**
         * This is the index of the cell currently being manipulated.
         */
        int base;

        /**
         * This is the index of the next cell that will be manipulated.
         */
        int next = -1;

        /**
         * This is the number of bytes copied from the buffer
         * into the newly allocated cell(s), thus far.
         */
        int written = 0;

        /**
         * Allocate the cells that are needed to store the data, one by one.
         * As the cell(s) are allocated, fill them with the relevant data.
         */
        for (int cellNum = 0; cellNum < count; cellNum++)
        {
            /**
             * Allocate a cell.
             */
            base = (cellNum == 0) ? allocNode() : next;
            Verify.verify(base >= 0);
            final long life = heap.incrementAndGet(base + OFFSET_LIFE);
            heap.set(base + OFFSET_REFS, 1);
            heap.set(base + OFFSET_SIZE, length);

            /**
             * If this is the last cell in the series of cell(s) storing the data,
             * then the cell does not point to another cell.
             */
            if (cellNum + 1 < count)
            {
                next = allocNode();
            }
            else
            {
                next = -1;
            }
            heap.set(base + OFFSET_NEXT, next);

            /**
             * If this is the first cell in the series of cell(s),
             * then construct the pointer that will be returned by this method.
             */
            ptr = (cellNum == 0) ? (life << 32 | base) : ptr;

            /**
             * Each cell consists of a finite series of slots.
             * Each slot stores up to four bytes of the data.
             * Fill the slots with the data.
             */
            for (int slotIndex = 0; slotIndex < dataSlotsPerCell; slotIndex++)
            {
                /**
                 * This is the number of bytes that still need transferred.
                 */
                final int remaining = length - written;

                if (remaining == 0)
                {
                    break;
                }

                /**
                 * These will be the four bytes placed into the slot.
                 */
                final int slot;

                /**
                 * Combine up to four bytes into a single integer,
                 * for storage in the slot.
                 */
                switch (remaining)
                {
                    case 1:
                        slot = ((int) buffer[offset + written + 0]) << 24;
                        written += 1;
                        break;
                    case 2:
                        slot = ((int) buffer[offset + written + 0]) << 24 | ((int) buffer[offset + written + 1]) << 16;
                        written += 2;
                        break;
                    case 3:
                        slot = ((int) buffer[offset + written + 0]) << 24 | ((int) buffer[offset + written + 1]) << 16 | ((int) buffer[offset + written + 2]) << 8;
                        written += 3;
                        break;
                    default:
                        slot = ((int) buffer[offset + written + 0]) << 24 | ((int) buffer[offset + written + 1]) << 16 | ((int) buffer[offset + written + 2]) << 8 | ((int) buffer[offset + written + 3]);
                        written += 4;
                        break;
                }

                /**
                 * Place the byte(s) into the slot.
                 */
                heap.set(base + OFFSET_DATA + slotIndex, slot);
            }
        }

        checkPointer(ptr);

        return ptr;
    }

    /**
     * Use this method to read the data contained inside of a previously
     * allocated block of memory into a given buffer.
     *
     * @param ptr identifies the block of memory.
     * @param buffer will receive the data.
     * @param offset is where the data will be written in the buffer.
     * @param limit is the maximum number of bytes to read.
     * @return the number of bytes read, or a negative number,
     * if the read fails for some reason.
     */
    public int read (final long ptr,
                     final byte[] buffer,
                     final int offset,
                     final int limit)
    {
        checkPointer(ptr);

        /**
         * Extract the index of the first cell from the pointer.
         * The index is the low four bytes.
         */
        int p = (int) (ptr & 0xFFFFFFFFL);

        /**
         * Get the number of data bytes in the allocation.
         * The bytes may span multiple cells.
         */
        final int size = heap.get(p + OFFSET_SIZE);

        int bytesRead = 0;

        /**
         * Iterate across all of the cells in the allocation.
         */
        while (p >= 0)
        {
            /**
             * Iterate across the data-storage slots in the cell.
             * Copy the data in each slot into the buffer.
             * Note: The last data-storage slots may contain less than four bytes.
             */
            for (int slotIndex = 0; slotIndex < dataSlotsPerCell; slotIndex++)
            {
                final int remaining = size - bytesRead;

                if (remaining == 0)
                {
                    break;
                }

                final int bytes = heap.get(p + OFFSET_DATA + slotIndex);

                switch (remaining)
                {
                    case 1:
                        buffer[offset + bytesRead + 0] = (byte) ((bytes & 0xFF000000) >> 24);
                        bytesRead += 1;
                        break;
                    case 2:
                        buffer[offset + bytesRead + 0] = (byte) ((bytes & 0xFF000000) >> 24);
                        buffer[offset + bytesRead + 1] = (byte) ((bytes & 0x00FF0000) >> 16);
                        bytesRead += 2;
                        break;
                    case 3:
                        buffer[offset + bytesRead + 0] = (byte) ((bytes & 0xFF000000) >> 24);
                        buffer[offset + bytesRead + 1] = (byte) ((bytes & 0x00FF0000) >> 16);
                        buffer[offset + bytesRead + 2] = (byte) ((bytes & 0x0000FF00) >> 8);
                        bytesRead += 3;
                        break;
                    default:
                        buffer[offset + bytesRead + 0] = (byte) ((bytes & 0xFF000000) >> 24);
                        buffer[offset + bytesRead + 1] = (byte) ((bytes & 0x00FF0000) >> 16);
                        buffer[offset + bytesRead + 2] = (byte) ((bytes & 0x0000FF00) >> 8);
                        buffer[offset + bytesRead + 3] = (byte) (bytes & 0x000000FF);
                        bytesRead += 4;
                        break;
                }
            }

            /**
             * Go to the next cell in the series of cell(s).
             */
            p = heap.get(p + OFFSET_NEXT);
        }

        return size;
    }

    public int incrementRefCount (final long ptr)
    {
        checkPointer(ptr);
        final int cell = (int) (0x00000000FFFFFFFFL & ptr);
        final int refCount = heap.incrementAndGet(cell + OFFSET_REFS);
        Verify.verify(refCount < Integer.MAX_VALUE);
        return refCount;
    }

    public int decrementRefCount (final long ptr)
    {
        checkPointer(ptr);
        final int cell = (int) (0x00000000FFFFFFFFL & ptr);
        final int refCount = heap.decrementAndGet(cell + OFFSET_REFS);

        if (refCount == 0)
        {
            free(ptr);
        }

        return refCount;
    }

    /**
     * Use this method to free a block of memory that was previously allocated.
     *
     * @param ptr identifies the block of memory.
     */
    private void free (final long ptr)
    {
        checkPointer(ptr);

        if (ptr == 0)
        {
            return;
        }

        /**
         * Extract the index of the first cell from the pointer.
         * The index is the low four bytes.
         */
        int p = (int) (ptr & 0xFFFFFFFFL);

        /**
         * Iterate across all of the cells in the allocation.
         * Free each cell as you traverse the series of cell(s).
         */
        while (p > 0)
        {
            final int next = heap.get(p + OFFSET_NEXT);
            freeCell(p);
            p = next;
        }
    }

    private synchronized void freeCell (final int ptr)
    {
        Verify.verify(ptr % slotsPerCell == 0);
        Verify.verify(ptr >= 1 * slotsPerCell);

        final int oldFree = free;
        Verify.verify(oldFree > 0);
        heap.set(ptr + OFFSET_NEXT, oldFree);
        heap.set(ptr + OFFSET_REFS, 0);
        free = ptr;
    }

    private synchronized int allocNode ()
    {
        final int ptr = free;

        if (ptr <= 0)
        {
            throw new RuntimeException("OutOfMem");
        }

        free = heap.get(ptr + OFFSET_NEXT);

        Verify.verify(ptr % slotsPerCell == 0);
        Verify.verify(ptr >= 1 * slotsPerCell);

        return ptr;
    }

    private void checkPointer (final long ptr)
    {
        if (ptr != 0)
        {
            final int life = (int) ((0xFFFFFFFF00000000L & ptr) >> 32);
            final int cell = (int) (0x00000000FFFFFFFFL & ptr);
            Preconditions.checkArgument(cell > 0);
            Preconditions.checkArgument(cell < heap.length());
            Preconditions.checkArgument(cell % slotsPerCell == 0);
            Preconditions.checkArgument(life == heap.get(cell + OFFSET_LIFE));
        }
    }

    public static void main (String[] args)
    {
        final CellularAllocator allocator = new CellularAllocator(1000, 16);

        final Runnable r = () ->
        {
            for (int i = 0; i < 1_000_000; i++)
            {
                final byte[] dataIn = "Vulcan".getBytes();
                final long p = allocator.alloc(dataIn, 0, dataIn.length, 0);
                final byte[] dataOut = new byte[dataIn.length];
                allocator.read(p, dataOut, 0, dataIn.length);
                allocator.free(p);
                //System.out.println("X = " + new String(dataOut));
            }
        };

        IntStream.range(0, 16).forEach(i -> new Thread(r).start());
    }
}
