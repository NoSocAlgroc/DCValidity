package approxcover;

import ch.javasoft.bitset.LongBitSet;

public class HyperEdge {

    LongBitSet vertices;
    long count;

    HyperEdge(LongBitSet vertices, long count) {
        this.vertices = vertices;
        this.count = count;
    }

    long getCount() {
        return count;
    }

    boolean get(int i) {
        return vertices.get(i);
    }
}
