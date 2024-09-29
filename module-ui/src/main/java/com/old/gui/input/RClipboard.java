/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

	 Copyright (c) 2008 Peter Lager

  The actual code to create the clipbaord, copy and paste were 
  taken taken from a similar GUI library Interfascia ALPHA 002 -- 
  http://superstable.net/interfascia/  produced by Brenden Berg 
  The main change is to provide static copy and paste methods to 
  separate the clipboard logic from the component logic and provide
  global access.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package com.old.gui.input;

import java.awt.*;
import java.awt.datatransfer.*;

/*
 * I wanted to implement copying and pasting to the clipboard using static
 * methods to simplify the sharing of a single clipboard over all classes.
 * The need to implement the ClipboardOwner interface requires an object so
 * this class creates an object the first time an attempt to copy or paste
 * is used.
 * 
 * All methods are private except copy() and paste() - lostOwnership()
 * has to be public because of the Clipboard owner interface.
 * 
 * @author Peter Lager
 *
 */

/**
 * Clipboard functionality for plain text <br>
 * 
 * This provides clipboard functionality for text and is currently only used by
 * the GTextField and GTextArea classes.
 * 
 * @author Peter Lager
 *
 */
public class RClipboard implements ClipboardOwner {

	/**
	 * Static reference to enforce singleton pattern
	 */
	private static RClipboard rclip = null;

	/**
	 * Class attribute to reference the programs clipboard
	 */
	private Clipboard clipboard = null;

	/**
	 * Copy a string to the clipboard
	 *
	 * @param chars the characters to be stored on the clipboard
	 * @return true for a successful copy to clipboard
	 */
	public static boolean copy(String chars) {
		if (rclip == null)
			rclip = new RClipboard();
		return rclip.copyString(chars);
	}

	/**
	 * Get a string from the clipboard
	 *
	 * @return the string on the clipboard
	 */
	public static String paste() {
		if (rclip == null)
			rclip = new RClipboard();
		return rclip.pasteString();
	}

	/**
	 * Ctor is private so clipboard is only created when a copy or paste is
	 * attempted and one does not exist already.
	 */
	private RClipboard() {
		if (clipboard == null) {
			makeClipboardObject();
		}
	}

	/**
	 * If security permits use the system clipboard otherwise create our own
	 * application clipboard.
	 */
	private void makeClipboardObject() {
			try {
				clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			} catch (SecurityException e) {
				clipboard = new Clipboard("Application Clipboard");
			}
	}

	/**
	 * Copy a string to the clipboard. If the Clipboard has not been created then
	 * create it.
	 * 
	 * @param chars the characters to be stored on the clipboard
	 * @return true for a successful copy to clipboard
	 */
	private boolean copyString(String chars) {
		if (clipboard == null)
			makeClipboardObject();
		if (clipboard != null) {
			StringSelection fieldContent = new StringSelection(chars);
			clipboard.setContents(fieldContent, this);
			return true;
		}
		return false;
	}

	/**
	 * Gets a string from the clipboard. If there is no Clipboard then create it.
	 * 
	 * @return if possible the string on the clipboard else an empty string
	 */
	private String pasteString() {
		// If there is no clipboard then there is nothing to paste
		if (clipboard == null) {
			makeClipboardObject();
			return "";
		}
		// We have a clipboard so get the string if we can
		Transferable clipboardContent = clipboard.getContents(this);

		if ((clipboardContent != null) && (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
			try {
				String tempString;
				tempString = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
				return tempString;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * Reqd by ClipboardOwner interface
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

}
