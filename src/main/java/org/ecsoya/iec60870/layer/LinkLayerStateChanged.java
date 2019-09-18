/**
 * 
 */
package org.ecsoya.iec60870.layer;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public interface LinkLayerStateChanged {

	void performStateChanged(Object parameter, int address, LinkLayerState newState);
}
