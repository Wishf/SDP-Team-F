package sdp.strategy.interfaces;

import sdp.vision.interfaces.WorldStateReceiver;

public interface Strategy extends WorldStateReceiver{
	
	public void startControlThread();
	public void stopControlThread();

}
