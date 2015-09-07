package DataContract.DataModels;

import java.util.List;

public class UserProfile {
    public String UserId;
    public String PhoneNumber;
    public String Name;
    public boolean IsVerified;
    public List<String> ContactPreference;
    public String PushNotificationUrl;
    public String EmailAddress;
    public String Address;
    public String PaymentDetails;
    public boolean IsAgent;
    public boolean IsManager;
    public int LandingPage;
    public double Rating;
    public int NumberOfRatings;
    public List<String> Tags;
    public String AreaOfService;
}
