package DataContract.DataModels;

import java.util.List;

public class CaseDetails {
    public String CaseId;
    public String UserId;
    public String UserName;
    public String Title;
    public List<String> ContactPreference;
    public List<String> Tags;
    public String RequestDetails;
    public int Budget;
    public String AssignedAgentId;
    public String AssignedAgentName;
    public String LastUpdateDateTimeString;
    public Boolean NewMessage;
    public Boolean NewEmail;
    public Boolean NewPhoneCall;
    public Boolean IsEnterpriseTag;
    public Boolean IsMetadataComplete;
}
