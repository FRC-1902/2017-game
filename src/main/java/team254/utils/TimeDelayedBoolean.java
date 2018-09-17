package team254.utils;

/**
 * This class contains a boolean value and a timer. It can set its boolean value
 * and return whether the timer is within a set timeout. This returns true if
 * the stored value is true and the timeout has expired.
 */
public class TimeDelayedBoolean {

    //TODO: re-enable with WPILib
    /*
    private Timer t = new Timer();
    private boolean m_old = false;

    public boolean update(boolean value, double timeout) {
        if (!m_old && value) {
            t.reset();
            t.start();
        }
        m_old = value;
        return value && t.get() >= timeout;
    }*/
}
