package DataContract;

import java.util.List;

import DataContract.DataModels.CaseDetails;

public class SaveNewCaseRequestContainer extends BaseRequestContainer {
    public CaseDetails caseInfo;
    public List<String> agentIds;
}
