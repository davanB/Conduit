package ca.uwaterloo.fydp.conduit;

/**
 * Created by davanb on 2018-02-08.
 */

public abstract class AppConstants {
    public static final String GROUP_NAME_KEY = "GroupName";
    public static final String USER_NAME_KEY = "UserName";
    public static final String PASSWORD_KEY = "Password";
    public static final String GROUP_SIZE = "GroupSize";
    public static boolean PUPPET_MASTER_ENABLED = false;
    public static boolean USE_REAL_HARDWARE = !PUPPET_MASTER_ENABLED;
    public static final boolean TRANSFORMATIONS_ENABLED = false;
}
