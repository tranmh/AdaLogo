package adalogo.visitor;

import java.util.Random;

//TODO Tested enough?
/**
 * You need this for creating and using arrays of integer, boolean and float.
 * An array has a number of dimension, the bounds of every dimension, the length
 * of every dimension. the value of the array has one dimension and is a String.
 * The Array is "zeilenweise orientiert"
 */
public class Array {
	
    /**
     * array exceptions.
     * nothing special here.
     */
    public class ArrayException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ArrayException(String message) {
            super(message);
        }
    }
    
    private static final String BOOLEAN = "boolean";
    private static final String INTEGER = "integer";
    private static final String FLOAT = "float";
    
    private Random random = new Random();
	
	private int dimension;
	private String type;
	private int[] bound;
	private int[] arrayDimensionLength;
	
	private String arrayValue = "";
	
	/**
	 * Creating an array without default value for the items of the array.
	 * Like Ada the value of every item is randomized!
	 * @param dimension
	 * @param type
	 * @param bound
	 * @throws ArrayException
	 */
	public Array(int dimension, String type, int[] bound) throws ArrayException{
		if (!checkConstructor(dimension,type,bound,"", false)) {
			// TODO write a better description
			throw new ArrayException("The constructor of the class Array has got the wrong parameter!");
		}
		
		this.random.setSeed(System.currentTimeMillis());
		this.dimension = dimension;
		this.type = type;
		this.bound = bound;
		setArrayDimensionLength(bound);
		
		for (int i = 0; i < numOfCell(bound); i++) {
			if (type.equals(INTEGER)) {
				arrayValue = arrayValue + (new Integer(random.nextInt())).toString() + ";";
			} 
			else if (type.equals(BOOLEAN)) {
				arrayValue = arrayValue + (new Boolean(random.nextBoolean())).toString() + ";";
			} 
			else if (type.equals(FLOAT)) {
				arrayValue = arrayValue + (new Float(random.nextFloat())).toString() + ";";
			} 
			else {
				throw new ArrayException("This should never happen in class Array!");
			}
		}
	}
	
	/**
	 * Creating the array with the default value for every item of the array
	 * @param dimension
	 * @param type
	 * @param bound
	 * @param defaultValue
	 * @throws ArrayException
	 */
	public Array(int dimension, String type, int[] bound, String defaultValue) throws ArrayException {
		if (!checkConstructor(dimension,type,bound,defaultValue, true)) {
			// TODO write a better description
			throw new ArrayException("The constructor of the class Array has got the wrong parameter!");
		}
		this.random.setSeed(System.currentTimeMillis());
		this.dimension = dimension;
		this.type = type;
		this.bound = bound;
		setArrayDimensionLength(bound);
		
		for (int i = 0; i < numOfCell(bound); i++) {
			if (type.equals(INTEGER)) {
				arrayValue = arrayValue + (new Integer(defaultValue)).toString() + ";";
			} 
			else if (type.equals(BOOLEAN)) {
				arrayValue = arrayValue + (new Boolean(defaultValue)).toString() + ";";
			} 
			else if (type.equals(FLOAT)) {
				arrayValue = arrayValue + (new Float(defaultValue)).toString() + ";";
			} 
			else {
				throw new ArrayException("This should never happen in class Array!");
			}
		}
	}
	
	/**
	 * Checking for intern use.
	 * dimension is not negative or 0, dimension * 2 = bound.length
	 * only integer, boolean and float.
	 * every left bound is smaller or equal the right bound. 
	 * @param dimension
	 * @param type
	 * @param bound
	 * @param defaultValue
	 * @param hasDefaultValue
	 * @return
	 */
	private boolean checkConstructor(int dimension, String type, int[] bound, String defaultValue, boolean hasDefaultValue) {
		if (dimension < 1 || dimension * 2 != bound.length) {
			return false;
		}
		if (!type.equals(INTEGER) && !type.equals(BOOLEAN) && !type.equals(FLOAT)) {
			return false;
		}
		for (int i = 0; i < bound.length/2; i++) {
			if (bound[i*2] > bound[i*2+1]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * How many cells in the 1 dimension String arrayValue do we need.
	 * @param bound
	 * @return
	 */
	private int numOfCell(int[] bound) {
		int cells = 1;
		for (int i = 0; i < bound.length/2; i++) {
			cells = cells * (bound[i*2+1] - bound[i*2] + 1);
		}
		return cells;
	}
	
	/**
	 * set it
	 * @param bound
	 */
	private void setArrayDimensionLength(int[] bound) {
		this.arrayDimensionLength = new int[bound.length/2];
		for (int i = 0; i < bound.length/2; i++) {
			arrayDimensionLength[i] = bound[i*2+1] - bound[i*2] + 1;
		}
	}
	
	/**
	 * Get the value of the array.
	 * @param indexes
	 * @return
	 * @throws ArrayException
	 */
	public String getValue(int[] indexes) throws ArrayException {
		if (!checkIndexes(indexes)) {
			throw new ArrayException("Array has wrong dimension or index(es) is out of bound!");
		}
		
		int cellNr = getCellNr(indexes);
		
		return arrayValue.split(";")[cellNr];
	}
	
	/**
	 * Set the value of the array
	 * @param indexes
	 * @param value
	 * @throws ArrayException
	 */
	public void setValue(int[] indexes, String value) throws ArrayException {
		if (!checkIndexes(indexes)) {
			throw new ArrayException("Array has wrong dimension or index(es) is out of bound!");
		}
		
		int cellNr = getCellNr(indexes);
		
		String[] tmpArrayValue = arrayValue.split(";");
		
		if (type.equals(INTEGER)) {
			tmpArrayValue[cellNr] = (new Integer(value)).toString();
		} 
		else if (type.equals(BOOLEAN)) {
			tmpArrayValue[cellNr] = (new Boolean(value)).toString();
		} 
		else if (type.equals(FLOAT)) {
			tmpArrayValue[cellNr] = (new Float(value)).toString();
		} 
		else {
			throw new ArrayException("This should never happen. Fix setValue in class Array.");
		}
		
		arrayValue = "";
		for (int i = 0; i < tmpArrayValue.length; i++) {
			arrayValue = arrayValue + tmpArrayValue[i] + ";";
		}
	}
	
	/**
	 * Get the CellNr in the flat 1 dimension arrayValue.
	 * @param indexes
	 * @return
	 */
	private int getCellNr(int[] indexes) {
		int cellNr = 0;
		int tmp = 1; 
		for (int i = indexes.length-1; i >= 0; i--) {
			tmp = 1;
			for (int j = 0; j < indexes.length-i-1; j++) {
				tmp = tmp * arrayDimensionLength[j];
			}
			cellNr = cellNr + ((indexes[i]-bound[i*2])*tmp);  
		}
		return cellNr;
	}
	
	/**
	 * Check the indexes before get or set something from the array
	 * @param indexes
	 * @return
	 */
	private boolean checkIndexes(int[] indexes) {
		if (indexes.length != dimension) {
			return false;
		}
		for (int i = 0; i < indexes.length; i++) {
			if (indexes[i] < bound[i*2] || indexes[i] > bound[i*2+1]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Get Dimension
	 * @return
	 */
	public int getDimension() {
		return dimension;
	}
	
	/**
	 * Get Type of the Array
	 * @return
	 */
	public String getType() {
		return type;
	}
	
	// TODO write this better to protect ArrayIndexOutOfBoundsException
	public int getBound(int i) {
		return bound[i];
	} 
	
	// TODO write this better to protect ArrayIndexOutOfBoundsException
	public int getArrayDimensionLength(int i) {
		return arrayDimensionLength[i];
	}
	
	// TODO write this better
	public String getArrayValue() {
		return arrayValue;
	}
	
	/**
	 * To the console for testing
	 * @param indexes
	 * @throws ArrayException
	 */
	public void toString(int[] indexes) throws ArrayException {
		System.out.println(getValue(indexes));
	}
	
	/**
	 * To the console for testing 
	 */
	public void arrayToString() {
		System.out.println("Dimension: " + dimension);
		System.out.println("Type: " + type);
		System.out.print("Bound: ");
		for (int i = 0; i < bound.length; i++) {
			System.out.print(bound[i] + " ");
		}
		System.out.println();
		System.out.println("ArrayValue: " + arrayValue);
	}
	
	//-------------------------------------------------------------------------
	/***
	 * The main method is using for doing some tests etc.
	 * @param args
	 */
	public static void main(String[] args) {
		int[] myIntegerBound = new int[6];
		myIntegerBound[0] = 1;
		myIntegerBound[1] = 2;
		myIntegerBound[2] = 1;
		myIntegerBound[3] = 3;
		myIntegerBound[4] = 2;
		myIntegerBound[5] = 3;
		
		int[] myIndexes = new int[3];
		myIndexes[0] = 1;
		myIndexes[1] = 2;
		myIndexes[2] = 2;
		                           
		Array myIntegerArray = new Array(3, INTEGER, myIntegerBound, "1");
		myIntegerArray.arrayToString();
		myIntegerArray.setValue(myIndexes,"4");
		myIntegerArray.toString(myIndexes);
		myIntegerArray.arrayToString();
	}
}
