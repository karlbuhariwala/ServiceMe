package DataContract;

import android.util.Pair;

import java.util.List;

public class GetChatRoomDetailsReturnContainer extends BaseReturnContainer {
    public String caseId;
    public int userType;
    public String chatRoomTitle;
    public List<Pair<String, String>> participantsInfo;
    public List<Pair<String, String>> userIdNamePairs;
}
