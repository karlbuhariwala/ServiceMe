package DataContract;

import android.util.Pair;

import java.util.List;

public class AddAgentTagsRequestContainer extends BaseRequestContainer {
    public List<Pair<String, Integer>> tagCodeList;
}
