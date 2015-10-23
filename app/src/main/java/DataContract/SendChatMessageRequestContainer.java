package DataContract;

import android.util.Pair;

import java.util.List;

public class SendChatMessageRequestContainer extends BaseRequestContainer {
    public String caseId;
    public String senderId;
    public String senderName;
    public int typeOfMessage;
    public String message;
    public List<Pair<String, String>> participantsInfo;
}
