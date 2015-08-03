package serversideapp;

import java.awt.Frame;
import javax.swing.JOptionPane;

/**
 * Class that diaplays dialogs.
 */
public abstract class Dialogs {

    /**
     * Displays an error dialog.
     *
     * @param frame determines the Frame in which the dialog is displayed;
     *              if null, or if the parentComponent has no Frame, a default Frame is used.
     * @param msg   The object to display.
     * @param title The title string for the dialog.
     */
    public static void ErrorDialog(Frame frame, String msg, String title) {
        JOptionPane.showMessageDialog(frame,
                msg,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays a success dialog.
     *
     * @param frame determines the Frame in which the dialog is displayed;
     *              if null, or if the parentComponent has no Frame, a default Frame is used.
     * @param msg   The object to display.
     * @param title The title string for the dialog.
     */
    public static void SucessDialog(Frame frame, String msg, String title) {
        JOptionPane.showMessageDialog(frame,
                msg,
                title,
                JOptionPane.INFORMATION_MESSAGE);
    }
}
