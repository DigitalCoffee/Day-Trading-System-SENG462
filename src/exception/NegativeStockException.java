/**
 * 
 */
package exception;

/**
 * @author andrew
 *
 */
public class NegativeStockException extends Exception {

	private static final long serialVersionUID = 7855319411780363500L;

	/**
	 * @param message
	 */
	public NegativeStockException(String message) {
		super(message);
	}
}
