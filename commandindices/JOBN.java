package commandindices;

public enum JOBN {
    submitTim    (1),
    jobID        (2),
    estRunTime   (3),
    core         (4),
    memory       (5);

    public final int idx;

    private JOBN(int idx) {
        this.idx = idx;
    }
}
