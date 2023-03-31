package commandindices;

public enum GETS {
    serverType     (0),
    serverID       (1),
    state          (2),
    curStartTime   (3),
    core           (4),
    memory         (5),
    disk           (6),
    waitingJobNum  (7),
    runningJobNum  (8);

    public final int idx;

    private GETS(int idx) {
        this.idx = idx;
    }
}
