import java.util.Collections;
import java.util.List;

public class Servers {
    public List<String> types;
    public List<Integer> limits;
    public List<Integer> bootupTimes;
    public List<Float> hourlyRates;
    public List<Integer> cores;
    public List<Integer> memories;
    public List<Integer> disks;

    public String getLargestServerType() {
        int maxCore = Collections.max(cores);
        int idx = cores.indexOf(maxCore);
        return types.get(idx);
    }

    public int getLimit(String type) {
        int idx = types.indexOf(type);
        return limits.get(idx);
    }
}
