package org.ctoolkit.services.identity;

/**
 * The listener to receive reset password notification or change email notification.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface IdentityTroubleListener
{
    /**
     * Gets notification about request to reset password.
     *
     * @param email     the email of the user wants to reset password
     * @param resetLink the confirmation link to handle reset password
     */
    void resetPassword( String email, String resetLink );

    /**
     * Gets notification about request to change email.
     *
     * @param localId  the localId of the user wants to change password
     * @param email    the old email to be changed
     * @param newEmail the new email
     */
    void changeEmail( String localId, String email, String newEmail );
}
