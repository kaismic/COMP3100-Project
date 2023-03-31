package commandindices;

public enum GETS {
    serverType     (1),
    serverID       (2),
    state          (3),
    curStartTime   (4),
    core           (5),
    memory         (6),
    disk           (7),
    waitingJobNum  (8),
    runningJobNum  (9);

    public final int idx;

    private GETS(int idx) {
        this.idx = idx;
    }
}
