public class TransactionException extends Exception {
public TransactionException(String message)
{
	super(message);
}
}
class NegativeMoneyException extends Exception {
public NegativeMoneyException(String message)
{
	super(message);
}
}
class NegativeStockException extends Exception {
public NegativeStockException(String message)
{
	super(message);
}
}
