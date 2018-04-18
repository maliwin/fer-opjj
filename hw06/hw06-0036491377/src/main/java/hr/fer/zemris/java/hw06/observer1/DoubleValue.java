package hr.fer.zemris.java.hw06.observer1;

/**
 * Observer class which prints the doubled value stored in the storage on every change.
 *
 * @author matej
 */
public class DoubleValue implements IntegerStorageObserver {
    /**
     * Number of times DoubleValue will call valueChanged before unsubscribing.
     */
    private int n;
    /**
     * Number of times valueChanged was called so far
     */
    private int counter;

    public DoubleValue(int n) {
        this.n = n;
    }

    @Override
    public void valueChanged(IntegerStorage istorage) {
        counter++;
        if (counter <= this.n) {
            System.out.printf("Double value: %d\n", istorage.getValue() * 2);
        } else {
            istorage.removeObserver(this);
        }
    }
}
