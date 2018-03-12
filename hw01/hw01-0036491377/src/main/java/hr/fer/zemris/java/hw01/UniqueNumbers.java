package hr.fer.zemris.java.hw01;

import java.util.Scanner;

/**
 * @author matej
 *
 */
public class UniqueNumbers {

	/**
	 * Representation of a node in a binary search tree
	 */
	static class TreeNode {
		TreeNode left;
		TreeNode right;
		int value;
	}

	/**
	 * Invoked when the program is executed.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		TreeNode head = null;
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.print("Unesite broj > ");
			String input = scanner.nextLine();

			// Loop exit condition
			if (input.equals("kraj")) {
				break;
			}

			int value;
			try {
				value = Integer.parseInt(input);
			} catch (NumberFormatException ex) {
				System.out.format("'%s' nije cijeli broj.\n", input);
				continue;
			}

			if (!containsValue(head, value)) {
				head = addNode(head, value);
				System.out.println("Dodano.");
			} else {
				System.out.println("Broj već postoji. Preskačem.");
			}

		}

		printSorted(head, false);
		printSorted(head, true);

		scanner.close();
	}

	/**
	 * Inserts a node into the given binary search tree .
	 * 
	 * @param head head node of the binary search tree
	 * @param value value to be inserted in the tree
	 * @return head of the binary search tree
	 */
	public static TreeNode addNode(TreeNode head, int value) {
		if (head == null) {
			head = new TreeNode();
			head.value = value;
		} else if (value < head.value) {
			head.left = addNode(head.left, value);
		} else if (value > head.value) {
			head.right = addNode(head.right, value);
		}

		return head;
	}

	/**
	 * Calculates the size of the given binary search tree.
	 * 
	 * @param head head of the tree
	 * @return size of the tree
	 */
	public static int treeSize(TreeNode head) {
		if (head != null) {
			return treeSize(head.left) + 1 + treeSize(head.right);
		} else {
			return 0;
		}
	}

	/**
	 * Returns <code>true</code> if a given value is in the tree.
	 * 
	 * @param head head of the tree
	 * @param value value to be found
	 * @return <code>true</code> if value is in tree, <code>false</code> otherwise
	 */
	public static boolean containsValue(TreeNode head, int value) {
		if (head == null) {
			return false;
		} else if (head.value == value) {
			return true;
		} else if (value < head.value && head.left != null && containsValue(head.left, value)) {
			return true;
		} else if (value > head.value && head.right != null && containsValue(head.right, value)) {
			return true;
		}

		return false;
	}

	/**
	 * Prints binary search tree in a specified order.
	 * 
	 * @param head head of the tree to be printed
	 * @param ascending specifies the direction to be sorted in
	 */
	private static void printSorted(TreeNode head, boolean ascending) {
		String sorted = sort(head, "", ascending);
		System.out.format("Ispis od %s: %s\n", ascending ? "najmanjeg" : "najvećeg", sorted);
	}

	/**
	 * Returns a string representation of a binary search tree in a specified order.
	 * 
	 * @param head head of the binary search tree
	 * @param string starting string to which the values will be appended to
	 * @param ascending specifies the direction to be sorted in
	 * @return string representation of the tree
	 */
	public static String sort(TreeNode head, String string, boolean ascending) {
		if (head == null) {
			return string;
		}

		string = sort(ascending ? head.left : head.right, string, ascending);
		string += head.value + " ";
		string = sort(ascending ? head.right : head.left, string, ascending);

		return string;
	}
}
