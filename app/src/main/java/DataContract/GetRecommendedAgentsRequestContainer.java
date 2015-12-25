package DataContract;

import DataContract.DataModels.CaseDetails;
import DataContract.DataModels.UserProfile;

public class GetRecommendedAgentsRequestContainer extends BaseRequestContainer {
    public CaseDetails caseDetails;
    public UserProfile userProfile;
}
