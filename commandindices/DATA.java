package commandindices;

public enum DATA {
    nRecs(1),
    recLen(2);

    public final int idx;

    private DATA(int idx) {
        this.idx = idx;
    }
}
