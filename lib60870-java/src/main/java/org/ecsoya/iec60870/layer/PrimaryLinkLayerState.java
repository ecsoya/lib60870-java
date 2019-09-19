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
package org.ecsoya.iec60870.layer;

public enum PrimaryLinkLayerState {
	IDLE,
	EXECUTE_REQUEST_STATUS_OF_LINK,
	EXECUTE_RESET_REMOTE_LINK,
	LINK_LAYERS_AVAILABLE,
	EXECUTE_SERVICE_SEND_CONFIRM,
	EXECUTE_SERVICE_REQUEST_RESPOND,
	SECONDARY_LINK_LAYER_BUSY /* Only required in balanced link layer */
}
