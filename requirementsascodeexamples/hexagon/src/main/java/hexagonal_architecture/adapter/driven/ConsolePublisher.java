package hexagonal_architecture.adapter.driven;

import hexagonal_architecture.hexagon.port.driven.IWriteLines;

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
	public void writeLine(String text) {
		System.out.println(text);
		System.out.println();
		System.out.println();
	}
}