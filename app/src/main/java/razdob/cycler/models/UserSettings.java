package razdob.cycler.models;

/**
 * Created by Raz on 03/06/2018, for project: PlacePicker2
 */
public class UserSettings {

    private User user;
    private UserAccountSettings settings;

    public UserSettings(User user, UserAccountSettings settings) {
        this.user = user != null ? user : new User();
        this.settings = settings != null ? settings : new UserAccountSettings();

        validateDetails();
    }

    /**
     * Puts the same name and the same id between User & UserAccountSettings objects,
     * if one of them is null...
     */
    private void validateDetails() {
        // User Name
        if (user.getName() != null && settings.getUserName() == null)
            settings.setUserName(user.getName());
        else if (user.getName() == null && settings.getUserName() != null)
            user.setName(settings.getUserName());

        // User ID
        if (user.getUser_id() == null && settings.getUser_id() != null)
            user.setUser_id(settings.getUser_id());
        if (user.getUser_id() != null && settings.getUser_id() == null)
            settings.setUser_id(user.getUser_id());
    }

    public UserSettings() { }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserAccountSettings getSettings() {
        return settings;
    }

    public void setSettings(UserAccountSettings settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "user=" + user +
                ", settings=" + settings +
                '}';
    }
}
