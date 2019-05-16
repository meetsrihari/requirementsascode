package poem.simple.drivenadapter;

import poem.hexagon.boundary.drivenport.IWriteLines;

/**
 * Right-side, driven adapter for writing text to the console.
 * 
 * Inspired by a talk by A. Cockburn and T. Pierrain on hexagonal architecture:
 * https://www.youtube.com/watch?v=th4AgBcrEHA
 * 
 * @author b_muth
 *
 */
public class ConsolePublisher implements IWriteLines {
	public void writeLines(String text) {
		System.out.println(text);
		System.out.println();
		System.out.println();
	}
}