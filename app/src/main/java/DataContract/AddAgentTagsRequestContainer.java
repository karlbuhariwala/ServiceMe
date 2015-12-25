package DataContract;

import android.util.Pair;

import java.util.List;

import DataContract.DataModels.UserProfile;

public class AddAgentTagsRequestContainer extends BaseRequestContainer {
    public List<Pair<String, Integer>> tagCodeList;
    public UserProfile agentProfile;
}
