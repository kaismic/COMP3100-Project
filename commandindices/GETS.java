package commandindices;

public enum GETS {
    serverType(1),
    serverID(2),
    state(3),
    curStartTime(4),
    core(4),
    memory(5),
    disk(6),
    waitingJobNum(7),
    runningJobNum(8);

    public final int idx;

    private GETS(int idx) {
        this.idx = idx;
    }
}
