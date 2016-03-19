/**
 * 
 */
package exception;

/**
 * @author andrew
 *
 */
public class NegativeMoneyException extends Exception {

	private static final long serialVersionUID = 4614039558400101747L;

	/**
	 * @param message
	 */
	public NegativeMoneyException(String message) {
		super(message);
	}

}
