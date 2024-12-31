/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2008-12 Peter Lager

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
package com.szadowsz.gui.component.text;

import processing.core.PConstants;

/**
 * This class provides an enumeration that is used to control the alignment
 * of text and images. <br>
 *
 * It also defines the constants for the position of the icon relative to
 * the text.
 *
 * @author Peter Lager
 *
 */
public enum RAlign {

	INVALID			( 0x0000, 	-1,		"INVALID", "Invalid alignment" ),

	// Horizontal alignment constants
	LEFT 			( 0x0001,	PConstants.LEFT,	"LEFT", 	"Left align text" ),
	CENTER 			( 0x0002,	PConstants.CENTER,	"CENTER", 	"Centre text horizontally" ),
	RIGHT			( 0x0004,	PConstants.RIGHT, 	"RIGHT", 	"Right align text" ),
	JUSTIFY  		( 0x0008,	-1,		"JUSTIFY", "Justify text" ),

	// Vertical alignment constants
	TOP 			(0x0010,		PConstants.TOP,		"TOP",		"Align text to to top" ),
	MIDDLE	 		(0x0020,		PConstants.CENTER,	"MIDDLE",	"Centre text vertically" ),
	BOTTOM 			(0x0040,		PConstants.BOTTOM,	"BOTTOM",	"Align text to bottom" );

	private final int alignID;
    private final int pConstant;
    private final String alignText;
	private final String description;
	/**
	 * Get an alignment based from its textual ID.
	 *
	 * @param textID the text ID to search for
	 * @return the alignment or INVALID if not found
	 */
	public static RAlign getFromText(String textID) {
		return switch (textID.toUpperCase()) {
			case "LEFT" -> LEFT;
			case "CENTER" -> CENTER;
			case "RIGHT" -> RIGHT;
			case "JUSTIFY" -> JUSTIFY;
			case "TOP" -> TOP;
			case "MIDDLE" -> MIDDLE;
			default -> INVALID;
		};
	}

	/**
	 * A private constructor to prevent alignments being create outside this class.
	 *
	 * @param id numeric ID
	 * @param text textual ID
	 * @param desc verbose description of alignment
	 */
	private RAlign(int id, int pConstants, String text, String desc ){
		alignID = id;
        pConstant = pConstants;
        alignText = text;
		description = desc;
	}
	
	
	/**
	 * @return the textual ID of this alignment.
	 */
	public String getTextID(){
		return alignText;
	}

	/**
	 * @return the textual verbose description of this alignment e.g. "Right align text"
	 */
	public String getDesc(){
		return description;
	}

	/**
	 * Is this a horizontal alignment constant?
	 * @return true if horizontally aligned else false.
	 */
	public boolean isHorzAlign(){
		return (alignID & 0x000F) != 0;
	}

	/**
	 * Is this a vertical alignment constant?
	 * @return true if vertically aligned else false.
	 */
	public boolean isVertAlign(){
		return (alignID & 0x00F0) != 0;
	}

	/**
	 * @return a full description of this alignment constant
	 */
	public String toString(){
		return "ID = " + alignText + " {" + alignID + "}  " + description;
	}

}
