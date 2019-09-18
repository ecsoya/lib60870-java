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
