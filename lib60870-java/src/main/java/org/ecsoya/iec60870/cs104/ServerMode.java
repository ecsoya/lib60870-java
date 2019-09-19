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
package org.ecsoya.iec60870.cs104;

/// <summary>
/// Server mode (redundancy group support)
/// </summary>
public enum ServerMode {
	/// <summary>
	/// There is only one redundancy group. There can only be one active
	/// connections.
	/// All other connections are standy connections.
	/// </summary>
	SINGLE_REDUNDANCY_GROUP,
	/// <summary>
	/// Every connection is an own redundancy group. This enables simple
	/// multi-client server.
	/// </summary>
	CONNECTION_IS_REDUNDANCY_GROUP
}
