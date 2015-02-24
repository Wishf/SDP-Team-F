package sdp.sdp9.strategy.interfaces;

import sdp.sdp9.vision.interfaces.WorldStateReceiver;

public interface Strategy extends WorldStateReceiver{
	
	public void startControlThread();
	public void stopControlThread();

}
