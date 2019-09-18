/*******************************************************************************
 * Copyright (C) 2019 Ecsoya (jin.liu@soyatec.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.ecsoya.iec60870;

/// <summary>
/// Common information functions about the library
/// </summary>
public class LibraryCommon {
	/// <summary>
	/// Library major version number
	/// </summary>
	public static final int VERSION_MAJOR = 2;

	/// <summary>
	/// Library minor version number
	/// </summary>
	public static final int VERSION_MINOR = 1;

	/// <summary>
	/// Library patch number
	/// </summary>
	public static final int VERSION_PATCH = 0;

	/// <summary>
	/// Gets the library version as string {major}.{minor}.{patch}.
	/// </summary>
	/// <returns>The library version as string.</returns>
	public static String getLibraryVersionString() {
		return "" + VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH;
	}
}
