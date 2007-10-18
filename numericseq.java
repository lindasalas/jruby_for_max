package ajm;

import com.cycling74.max.Atom;

/**
 * 
 * @author adam
 * @nodoc
 */
public abstract class numericseq extends seq {

	public numericseq(Atom[] args) {
		super(args);
	}

	protected abstract Atom[] coerceToNumber(Atom[] list);

	@Override
	public void set(Atom[] list) {
		super.set(coerceToNumber(list));
	}

	@Override
	public void append(Atom[] newVals) {
		super.append(coerceToNumber(newVals));
	}

	@Override
	protected void insert(int position, Atom[] newVals) {
		super.insert(coerceToNumber(newVals));
	}

	@Override
	public void prepend(Atom[] newVals) {
		super.prepend(coerceToNumber(newVals));
	}

	@Override
	protected int compare(Atom a1, Atom a2) {
		return a1.compareTo(a2);
	}

	@Override
	public void anything(String msg, Atom[] args) {
		error(getClass() + " doesn't understand " + msg);
	}

	protected abstract Atom add(Atom n, Atom m);

	// TODO: make these methods work generically and phase out this class
	// adding should ignore any non numeric values, and handle int and float as expected(?)
	public void add(Atom n) {
		for (int i = 0; i < values.size(); i++) {
			values.set(i, add(n, values.get(i)));
		}
		handleListChange();
		outputVals();
	}

	public void add(Atom n, int left, int right) {
		if (left > right) {
			int tmp = left;
			left = right;
			right = tmp;
		}
		if (left < 0)
			left = 0;
		if (right >= values.size())
			right = values.size() - 1;

		for (int i = left; i <= right; i++) {
			values.set(i, add(n, values.get(i)));
		}
		handleListChange();
		outputVals();
	}

	protected abstract Atom multiply(Atom n, Atom m);

	public void multiply(Atom n) {
		for (int i = 0; i < values.size(); i++) {
			values.set(i, multiply(n, values.get(i)));
		}
		handleListChange();
		outputVals();
	}

	public void multiply(Atom n, int left, int right) {
		if (left > right) {
			int tmp = left;
			left = right;
			right = tmp;
		}
		if (left < 0)
			left = 0;
		if (right >= values.size())
			right = values.size() - 1;

		for (int i = left; i <= right; i++) {
			values.set(i, multiply(n, values.get(i)));
		}
		handleListChange();
		outputVals();
	}

	// todo: arbitrary expression? can I eval javascript?
}
